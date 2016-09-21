package de.qabel.desktop.ui;

import com.google.common.base.Optional;
import com.sun.javafx.event.EventDispatchChainImpl;
import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.AsyncUtils;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import org.testfx.service.locator.BoundsLocatorException;
import org.testfx.service.query.PointQuery;

import javafx.scene.Node;
import org.testfx.api.FxRobot;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static de.qabel.desktop.AsyncUtils.assertAsync;

public class AbstractPage {
    protected FXRobot baseFXRobot;
    protected FxRobot robot;

    public AbstractPage(FXRobot baseFXRobot, FxRobot robot) {
        this.baseFXRobot = baseFXRobot;
        this.robot = robot;
    }

    protected void fakeClick(String query) {
        baseFXRobot.waitForIdle();
        Node node = waitForNode(query);
        Point2D pos = robot.point(node).getPosition();
        baseFXRobot.mouseMove((int)pos.getX(), (int)pos.getY());
        Point2D screenPos = node.localToScreen(pos.getX(), pos.getY());
        Point2D scenePos = node.localToScene(pos.getX(), pos.getY());

        if (node instanceof Region && !(node instanceof Control)) {
            throw new IllegalArgumentException("cannot fake clicks on random regions");
        }

        runLaterAndWait(() -> waitForNode(query).requestFocus());
        if (focusSufficesClick(node)) {
            return;
        }

        MouseEvent event = new MouseEvent(
            MouseEvent.MOUSE_CLICKED,
            pos.getX(),
            pos.getY(),
            screenPos.getX(),
            screenPos.getY(),
            MouseButton.PRIMARY,
            1,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true,
            false,
            false,
            new PickResult(node, scenePos.getX(), scenePos.getY())
        );
        EventHandler<? super MouseEvent> pressedHandler = node.getOnMousePressed();
        EventHandler<? super MouseEvent> clickedHandler = node.getOnMouseClicked();
        EventHandler<? super MouseEvent> releasedHandler = node.getOnMouseReleased();

        Platform.runLater(() -> {
            if (pressedHandler != null) {
                pressedHandler.handle(event);
            }
            if (clickedHandler != null) {
                clickedHandler.handle(event);
            }
            if (releasedHandler != null) {
                releasedHandler.handle(event);
            }
            if (pressedHandler == null && clickedHandler == null && releasedHandler == null) {
                node.buildEventDispatchChain(new EventDispatchChainImpl()).dispatchEvent(event);
            }
        });

        if (fireSufficesClick(node)) {
            Platform.runLater(((ButtonBase) node)::fire);
            return;
        }
    }

    private boolean fireSufficesClick(Node node) {
        return node instanceof ButtonBase;
    }

    private boolean focusSufficesClick(Node node) {
        return node instanceof TextInputControl;
    }

    protected FxRobot rightClickOn(String query) {
        baseFXRobot.waitForIdle();
        Node node = waitForNode(query);
        moveTo(node);
        return robot.rightClickOn(node);
    }

    protected FxRobot clickOn(String query) {
        try {
            fakeClick(query);
            return robot;
        } catch (Exception ignored) {
            System.err.println("failed to fake click on '" + query + "', falling back to real click");
        }
        for (int i = 0; i < 10; i++) {
            try {
                baseFXRobot.waitForIdle();
                Node node = waitForNode(query);
                moveTo(node);
                return clickOn(waitForNode(query));
            } catch (NullPointerException retry) {
            }
        }
        throw new IllegalStateException("failed to click on " + query + ", it vanished 10 times in a row");
    }

    private boolean hasMoved(Node node, double x, double y) {
        PointQuery point = robot.point(node);
        return point.getPosition().getX() != x || point.getPosition().getY() != y;
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
        double x;
        double y;
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            Point2D position = robot.point(node).getPosition();
            x = position.getX();
            y = position.getY();
            robot.moveTo(node);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            baseFXRobot.waitForIdle();
        } while (hasMoved(node, x, y));
        return robot.moveTo(node);
    }

    protected Node waitForNode(String query) {
        Node[] nodes = new Node[1];
        assertAsync(() -> {
            try {
                Optional<Node> node = robot.lookup(query).tryQuery();
                boolean present = node.isPresent() && node.get() != null;
                if (present) {
                    nodes[0] = node.get();
                }

                robot.point(nodes[0]).query();
                if (!present) {
                    throw new IllegalStateException("node not present");
                }
            } catch (Exception e) {
                throw new AssertionError(e.getMessage(), e);
            }
        });
        if (nodes[0] == null) {
            throw new IllegalStateException("node should be present but is null: " + query);
        }
        return nodes[0];
    }

    protected Node getFirstNode(String query) {
        return robot.lookup(query).tryQuery().get();
    }

    protected List<Node> getNodes(String query) {
        List<Node> nodes = new LinkedList<>();
        nodes.addAll(robot.lookup(query).queryAll());
        return nodes;
    }

    protected void runLaterAndWait(Runnable runnable) {
        boolean[] hasRun = new boolean[]{false};
        Platform.runLater(() -> {
            runnable.run();
            hasRun[0] = true;
        });
        waitUntil(() -> hasRun[0], 5000L);
    }

    public static void waitUntil(Callable<Boolean> evaluate) {
        AsyncUtils.waitUntil(evaluate);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout) {
        AsyncUtils.waitUntil(evaluate, timeout);
    }

    public static void waitUntil(Callable<Boolean> evaluate, Callable<String> errorMessage) {
        AsyncUtils.waitUntil(evaluate, errorMessage);
    }

    public static void waitUntil(Callable<Boolean> evaluate, long timeout, Callable<String> errorMessage) {
        AsyncUtils.waitUntil(evaluate, timeout, errorMessage);
    }
}
