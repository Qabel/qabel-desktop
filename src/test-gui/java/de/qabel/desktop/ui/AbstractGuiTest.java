package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import com.sun.javafx.robot.impl.BaseFXRobot;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.service.query.PointQuery;

public abstract class AbstractGuiTest<T> extends AbstractControllerTest {
	protected final FxRobot robot = new FxRobot();
	protected T controller;
	protected Stage stage;
	protected Scene scene;
	private BaseFXRobot baseFXRobot;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Platform.setImplicitExit(false);
		Object presenter = launchNode(getView());
		controller = (T) presenter;
	}

	protected abstract FXMLView getView();

	private Object launchNode(FXMLView view) {
		Parent node = view.getView();
		scene = new Scene(node, getWidth(), getHeight());
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
		baseFXRobot = new BaseFXRobot(scene);
		baseFXRobot.waitForIdle();
		Node sceneNode = robot.rootNode(scene);
		waitUntil(() -> {
			int a = (int) Math.round(sceneNode.computeAreaInScreen());
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
			Platform.runLater(() -> stage.close());
		}
		super.tearDown();
	}

	protected FxRobot clickOn(String query) {
		baseFXRobot.waitForIdle();
		Node node = getFirstNode(query);
		double x = -1;
		double y = -1;
		while (hasMoved(node, x, y)) {
			Point2D position = robot.point(node).getPosition();
			x = position.getX();
			y = position.getY();
			robot.moveTo(query);
		}
		return robot.moveTo(query).clickOn(query);
	}

	private boolean hasMoved(Node node, double x, double y) {
		PointQuery point = robot.point(node);
		return point.getPosition().getX() == x && point.getPosition().getY() == y;
	}

	protected FxRobot clickOn(Node node) {
		return robot.moveTo(node).clickOn(node);
	}

	protected Node getFirstNode(String query) {
		return robot.lookup(query).tryQueryFirst().get();
	}
}
