package de.qabel.desktop.ui;

import com.google.common.base.Optional;
import com.sun.javafx.robot.FXRobot;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import org.testfx.service.locator.BoundsLocatorException;
import org.testfx.service.query.PointQuery;

import javafx.scene.Node;
import org.testfx.api.FxRobot;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.fail;

public class AbstractPage {
	protected FXRobot baseFXRobot;
	protected FxRobot robot;

	public AbstractPage(FXRobot baseFXRobot, FxRobot robot) {
		this.baseFXRobot = baseFXRobot;
		this.robot = robot;
	}

	protected FxRobot clickOn(String query) {
		try {
			baseFXRobot.waitForIdle();
			Node node = waitForNode(query);
			return clickOn(node);
		} catch (NullPointerException e) {
			baseFXRobot.waitForIdle();
			Node node = waitForNode(query);
			return clickOn(node);
		}
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

	public static void waitUntil(Callable<Boolean> evaluate) {
		waitUntil(evaluate, 10000L);
	}

	protected Node waitForNode(String query) {
		Node[] nodes = new Node[1];
		waitUntil(() -> {
			Optional<Node> node = robot.lookup(query).tryQueryFirst();
			boolean present = node.isPresent() && node.get() != null;
			if (present) {
				nodes[0] = node.get();
			}
			return present;
		});
		if (nodes[0] == null) {
			throw new IllegalStateException("node should be present but is null: " + query);
		}
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

	public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
		waitUntil(evaluate, timeout, () -> "wait timeout");
	}

	public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
		waitUntil(evaluate, 2000L, () -> "wait timeout");
	}

	public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
		long startTime = System.currentTimeMillis();
		try {
			while (!evaluate.call()) {
				Thread.yield();
				Thread.sleep(10);
				if (System.currentTimeMillis() - timeout > startTime) {
					fail(errorMessage.call());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	protected void runLaterAndWait(Runnable runnable) {
		boolean[] hasRun = new boolean[]{false};
		Platform.runLater(() -> {
			runnable.run();
			hasRun[0] = true;
		});
		waitUntil(() -> hasRun[0], 5000L);
	}
}
