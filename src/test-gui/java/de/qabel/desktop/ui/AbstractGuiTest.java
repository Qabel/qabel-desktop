package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import com.sun.javafx.robot.impl.BaseFXRobot;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;

public abstract class AbstractGuiTest<T> extends AbstractControllerTest {
	protected final FxRobot robot = new FxRobot();
	protected T controller;
	private Stage stage;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Platform.setImplicitExit(false);
		Object presenter = launchNode(getView());
		controller = (T)presenter;
	}

	protected abstract FXMLView getView();

	private Object launchNode(FXMLView view) {
		Parent node = view.getView();
		Scene scene = new Scene(node, getWidth(), getHeight());
		Object presenter = view.getPresenter();
		robot.target(scene);

		runLaterAndWait(() ->
				{
					stage = new Stage();
					stage.setScene(scene);
					stage.show();
					robot.target(stage);
				}
		);
		BaseFXRobot baseFXRobot = new BaseFXRobot(scene);
		baseFXRobot.waitForIdle();
		Node sceneNode = robot.rootNode(scene);
		waitUntil(() -> {
			int a = (int)Math.round(sceneNode.computeAreaInScreen());
			int b = Math.round(getWidth() * getHeight());
			return a >= b;
		});
		return presenter;
	}

	protected int getHeight() {
		return 500;
	}

	protected int getWidth() {
		return 500;
	}

	@Override
	public void tearDown() {
		if (stage != null) {
			Platform.runLater(()-> stage.close());
		}
		super.tearDown();
	}

}
