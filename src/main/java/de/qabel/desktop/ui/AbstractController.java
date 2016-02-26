package de.qabel.desktop.ui;

import com.google.gson.Gson;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import javafx.application.Platform;
import de.qabel.desktop.crashReports.CrashReportHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;



public class AbstractController {


	@Inject
	private CrashReportHandler reportHandler;

	protected Alert alert;
	protected Label exceptionLabel;
	protected Gson gson;
	int statusCode;
	TextArea inputArea;

	protected void alert(Exception e) {
		alert(e.getMessage(), e);
	}

	protected void tryOrAlert(CheckedRunnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			alert(e);
		}
	}

	protected String getString(ResourceBundle resources, String message, Object... params) {
		return MessageFormat.format(resources.getString(message), params);
	}

	@FunctionalInterface
	public interface CheckedRunnable {
		void run() throws Exception;
	}

	protected void alert(String message, Exception e) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> alert(message, e));
		}
		LoggerFactory.getLogger(getClass().getSimpleName()).error(message, e);
		e.printStackTrace();

		alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(message);
		Label feedbackLabel = new Label("Feedback");
		Label stackTraceLabel = new Label("Stack Trace");

		inputArea = new TextArea();
		inputArea.getStyleClass().add("feedback");
		VBox.setMargin(inputArea, new Insets(10, 0, 5, 0));

		TextArea textArea = new TextArea(getTraceAsString(e));
		VBox.setMargin(textArea, new Insets(10, 0, 5, 0));
		textArea.setEditable(false);
		textArea.setWrapText(false);

		Button sendButton = new Button();
		sendButton.setText("send");
		sendButton.getStyleClass().add("send");

		sendButton.setOnAction(e1 -> {
			sendStackTrace(inputArea.getText(), textArea.getText());
			inputArea.setText("");
		});

		alert.getDialogPane().getChildren().add(sendButton);
		Button close = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
		close.setText("close");
		ButtonBar buttonBar = (ButtonBar)alert.getDialogPane().lookup(".button-bar");
		buttonBar.getButtons().add(sendButton);

		VBox.setVgrow(inputArea, Priority.ALWAYS);
		VBox.setVgrow(textArea, Priority.ALWAYS);
		VBox.setVgrow(sendButton, Priority.ALWAYS);

		exceptionLabel = new Label(e.getMessage());
		VBox expansion = new VBox();

		expansion.getChildren().add(exceptionLabel);
		expansion.getChildren().add(feedbackLabel);
		expansion.getChildren().add(inputArea);
		expansion.getChildren().add(stackTraceLabel);
		expansion.getChildren().add(textArea);

		alert.getDialogPane().setContent(expansion);
		alert.setResizable(true);
		alert.showAndWait();
	}

	private void sendStackTrace(String feedback, String stacktrace) {
		try {
			statusCode = reportHandler.sendStacktrace(feedback, stacktrace);
		} catch (URISyntaxException | IOException e) {
			alert("CrashReport count not send", e);
		}
	}

	private String getTraceAsString(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	protected void writeStringInFile(String json, File dir) throws IOException {
		File targetFile = new File(dir.getPath());
		targetFile.createNewFile();
		OutputStream outStream = new FileOutputStream(targetFile);
		outStream.write(json.getBytes());
	}

	public String readFile(File f) throws IOException {
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


	private ArrayList<DropURL> generateDropURLs(List<String> drops) throws URISyntaxException, QblDropInvalidURL {
		ArrayList<DropURL> collection = new ArrayList<>();

		for (String uri : drops) {
			DropURL dropURL = new DropURL(uri);

			collection.add(dropURL);
		}
		return collection;
	}

}
