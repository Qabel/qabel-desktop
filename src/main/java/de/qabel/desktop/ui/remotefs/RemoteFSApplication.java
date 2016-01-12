package de.qabel.desktop.ui.remotefs;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;


public class RemoteFSApplication extends Application {

	private static final String TITLE = "Qabel Desktop Client";

	public static void main(String[] args) throws Exception {
		Application.launch(RemoteFSApplication.class, args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setTitle(TITLE);

		if (SystemTray.isSupported()) {
			Platform.setImplicitExit(false);
			setTrayIcon(primaryStage);
		}

		new RemoteFSView().getViewAsync(parent -> {
			final Scene scene = new Scene(parent, 600, 600);
			primaryStage.setScene(scene);
		});
		primaryStage.show();
	}

	private void setTrayIcon(Stage primaryStage) {

		SystemTray sTray = SystemTray.getSystemTray();

		ActionListener listenerShow = e -> Platform.runLater(() -> primaryStage.show());
		ActionListener listenerClose = e -> System.exit(0);
		primaryStage.setOnCloseRequest(arg0 -> primaryStage.hide());

		PopupMenu popup = new PopupMenu();
		MenuItem showItem = new MenuItem("Öffnen");
		MenuItem exitItem = new MenuItem("Beenden");

		showItem.addActionListener(listenerShow);
		exitItem.addActionListener(listenerClose);

		popup.add(showItem);
		popup.add(exitItem);

		URL url = System.class.getResource("/logo.png");
		Image img = Toolkit.getDefaultToolkit().getImage(url);
		TrayIcon icon = new TrayIcon(img, "Qabel", popup);
		icon.setImageAutoSize(true);

		try {
			sTray.add(icon);
		} catch (AWTException e) {
			System.err.println(e);
		}

	}
}
