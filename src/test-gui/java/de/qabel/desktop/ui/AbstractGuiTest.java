package de.qabel.desktop.ui;

import com.airhacks.afterburner.views.FXMLView;
import com.google.common.base.Optional;
import com.sun.javafx.robot.impl.BaseFXRobot;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.service.locator.BoundsLocatorException;
import org.testfx.service.query.PointQuery;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static de.qabel.desktop.AsyncUtils.waitUntil;

public abstract class AbstractGuiTest<T> extends AbstractControllerTest {
	protected final FxRobot robot = new FxRobot();
	protected T controller;
	protected Stage stage;
	protected Scene scene;
	protected BaseFXRobot baseFXRobot;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		Platform.setImplicitExit(false);
		runLaterAndWait(() -> {
			stage = new Stage();
			diContainer.put("primaryStage", stage);
		});


		Object presenter = launchNode(getView());
		controller = (T) presenter;
	}

	protected abstract FXMLView getView();

	protected Object launchNode(FXMLView view) {
		Parent node = view.getView();
		scene = new Scene(node, getWidth(), getHeight());
		Object presenter = view.getPresenter();
		robot.targetWindow(scene);

		runLaterAndWait(() ->
				{

					stage.setScene(scene);
					stage.show();
					robot.targetWindow(stage);
				}
		);
		baseFXRobot = new BaseFXRobot(scene);
		baseFXRobot.waitForIdle();
		waitTillTheEnd(robot.rootNode(scene));
		return presenter;
	}

	protected void waitTillTheEnd(Node sceneNode) {
		waitUntil(() -> {
			try {
				int a = (int) Math.round(sceneNode.computeAreaInScreen());
				int b = Math.round(getWidth() * getHeight());
				return a >= b;
			} catch (Exception e) {
				return false;
			}
		}, 10000L);
	}

	protected int getHeight() {
		return 600;
	}

	protected int getWidth() {
		return 500;
	}

	@Override
	public void tearDown() throws Exception {
		if (stage != null) {
			Platform.runLater(() -> stage.close());
		}
		super.tearDown();
	}

	protected FxRobot clickOn(String query) {
		baseFXRobot.waitForIdle();
		Node node = waitForNode(query);
		return clickOn(node);
	}

	private boolean hasMoved(Node node, double x, double y) {
		PointQuery point = robot.point(node);
		return point.getPosition().getX() == x && point.getPosition().getY() == y;
	}

	protected FxRobot clickOn(Node node) {
		moveTo(node);
		FxRobot fxRobot;
		try {
			fxRobot = robot.moveTo(node).clickOn(node);
		} catch (BoundsLocatorException e) {
			fxRobot = robot.moveTo(node).clickOn(node);
		}
		baseFXRobot.waitForIdle();
		return fxRobot;
	}

	protected FxRobot moveTo(String query) {
		return moveTo(getFirstNode(query));
	}

	protected FxRobot moveTo(Node node) {
		baseFXRobot.waitForIdle();
		double x = -1;
		double y = -1;
		while (hasMoved(node, x, y)) {
			Point2D position = robot.point(node).getPosition();
			x = position.getX();
			y = position.getY();
			robot.moveTo(node);
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {
			}
		}
		return robot.moveTo(node);
	}

	protected Node waitForNode(String query) {
		Node[] nodes = new Node[1];
		waitUntil(() -> {
			Optional<Node> node = robot.lookup(query).tryQueryFirst();
			boolean present = node.isPresent();
			if (present) {
				nodes[0] = node.get();
			}
			return present;
		});
		return nodes[0];
	}

	protected Node getFirstNode(String query) {
		return robot.lookup(query).tryQueryFirst().get();
	}

	protected List<Node> getNodes(String query) {
		List<Node> nodes = new LinkedList<>();
		nodes.addAll(robot.lookup(query).queryAll());
		return nodes;
	}
}
