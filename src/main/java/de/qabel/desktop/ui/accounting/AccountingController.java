package de.qabel.desktop.ui.accounting;

import com.google.gson.Gson;
import de.qabel.core.config.Identity;
import de.qabel.desktop.config.ClientConfiguration;
import de.qabel.desktop.config.factory.IdentityBuilderFactory;
import de.qabel.desktop.exceptions.QblStorageException;
import de.qabel.desktop.repository.IdentityRepository;
import de.qabel.desktop.repository.exception.PersistenceException;
import de.qabel.desktop.ui.AbstractController;
import de.qabel.desktop.ui.accounting.item.AccountingItemView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
	ResourceBundle resourceBundle;

	@Inject
	private IdentityRepository identityRepository;

	@Inject
	private IdentityBuilderFactory identityBuilderFactory;

	@Inject
	private ClientConfiguration clientConfiguration;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadIdentities();
		this.resourceBundle = resources;
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
		dialog = new TextInputDialog(resourceBundle.getString("newIdentity"));
		dialog.setHeaderText(null);
		dialog.setTitle(resourceBundle.getString("newIdentity"));
		dialog.setContentText(resourceBundle.getString("newIdentity"));
		Optional<String> result = dialog.showAndWait();
		result.ifPresent(this::addIdentityWithAlias);
	}

	protected void addIdentityWithAlias(String alias) {
		Identity identity = identityBuilderFactory.factory().withAlias(alias).build();
		try {
			identityRepository.save(identity);
		} catch (PersistenceException e) {
			alert(resourceBundle.getString("saveIdentityFail"), e);
		}
		loadIdentities();
		if (clientConfiguration.getSelectedIdentity() == null) {
			clientConfiguration.selectIdentity(identity);
		}
	}

	@FXML
	protected void handleImportIdentityButtonAction(ActionEvent event) {

		FileChooser chooser = new FileChooser();
		chooser.setTitle(resourceBundle.getString("downloadFolder"));
		File file = chooser.showOpenDialog(identityList.getScene().getWindow());
		try {
			importIdentity(file);
		} catch (IOException | PersistenceException e) {
			e.printStackTrace();
		}
	}

	void importIdentity(File file) throws IOException, PersistenceException {
		String content = readFile(file);
		Identity i = gson.fromJson(content, Identity.class);
		identityRepository.save(i);
	}

	@FXML
	protected void handleExportIdentityButtonAction(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(resourceBundle.getString("downloadFolder"));
		File dir = chooser.showDialog(identityList.getScene().getWindow());
		Identity i = clientConfiguration.getSelectedIdentity();
		try {
			exportIdentity(i, dir);
			loadIdentities();
		} catch (IOException | QblStorageException e) {
			e.printStackTrace();
		}
	}

	void exportIdentity(Identity i, File dir) throws IOException, QblStorageException {

		String json = gson.toJson(i);
		InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);

		File targetFile = new File(dir.getPath() + "/" + i.getAlias() + ".json");
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
				line = br.readLine();
				if (line != null) {
					sb.append("\n");
				}
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	ResourceBundle getRessource(){
		return resourceBundle;
	}
}
