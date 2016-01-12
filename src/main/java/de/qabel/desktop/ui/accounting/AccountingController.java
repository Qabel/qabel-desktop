package de.qabel.desktop.ui.accounting;

import com.amazonaws.util.json.JSONException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import de.qabel.core.config.Identity;
import de.qabel.core.drop.DropURL;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilder;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.storage.BoxFile;
import de.qabel.desktop.storage.BoxFolder;
import de.qabel.desktop.storage.BoxNavigation;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AccountingController extends AbstractController implements Initializable {
	private Identity selectedIdentity;
	private Gson gson = new Gson();

	@FXML
	VBox identityList;

	List<AccountingItemView> itemViews = new LinkedList<>();

	TextInputDialog dialog;

	@Inject
	private IdentityRepository identityRepository;

	@Inject
	protected IdentityBuilderFactory identityBuilderFactory;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadIdentities();
	}

	private void loadIdentities() {
		try {
			identityList.getChildren().clear();
			for (Identity identity : identityRepository.findAll()) {
				final Map<String, Object> injectionContext = new HashMap<>();
				injectionContext.put("identity", identity);
				AccountingItemView itemView = new AccountingItemView(injectionContext::get);
				identityList.getChildren().add(itemView.getView());
				itemViews.add(itemView);
			}

		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void addIdentity(ActionEvent actionEvent) {
		addIdentity();
	}

	public void addIdentity() {
		dialog = new TextInputDialog("My Name");
		dialog.setHeaderText(null);
		dialog.setTitle("New Identity");
		dialog.setContentText("Please specify an avatar for your new Identity");
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::addIdentityWithAlias);
	}

	protected void addIdentityWithAlias(String alias) {
		Identity identity = identityBuilderFactory.factory().withAlias(alias).build();
		try {
			identityRepository.save(identity);
		} catch (PersistenceException e) {
			alert("Failed to save new identity", e);
		}
		loadIdentities();
		if (clientConfiguration.getSelectedIdentity() == null) {
			clientConfiguration.selectIdentity(identity);
		}
	}

	@FXML
	protected void handleImportIdentityButtonAction(ActionEvent event) {

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Download Folder");
		File file = chooser.showOpenDialog(identityList.getScene().getWindow());
		try {
			String content = readFile(file);
			Identity i = gson.fromJson(content, Identity.class);
			identityRepository.save(i);
		} catch (IOException | PersistenceException e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void handleExportIdentityButtonAction(ActionEvent event)  {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Download Folder");
		File dir = chooser.showDialog(identityList.getScene().getWindow());
		Identity i = clientConfiguration.getSelectedIdentity();
		String json = gson.toJson(i);
		try {
			saveFile(json,i.getAlias(), dir);
			loadIdentities();
		} catch (IOException | QblStorageException e) {
			e.printStackTrace();
		}
	}

	void saveFile(String json,String name, File dir) throws IOException, QblStorageException {

		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);

		File targetFile = new File(dir.getPath() + "/" + name + ".json");
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(buffer);
	}



	String readFile(File f) throws IOException {
		FileReader fileReader = new FileReader(f);
		BufferedReader br = new BufferedReader(fileReader);

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}
}
