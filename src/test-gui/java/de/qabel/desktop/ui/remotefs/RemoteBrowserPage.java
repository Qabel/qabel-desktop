package de.qabel.desktop.ui.remotefs;

import com.sun.javafx.robot.impl.BaseFXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class RemoteBrowserPage extends AbstractPage {
    private RemoteFSController controller;

    public RemoteBrowserPage(BaseFXRobot baseFXRobot, FxRobot robot, RemoteFSController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void assertRowCountAtLeast(int rows) {
        waitUntil(() -> getNodes(".cell").size() >= rows);
    }

    public RemoteBrowserRow getRow(int rowIndex) {
        assertRowCountAtLeast(rowIndex + 1);
        return new RemoteBrowserRow(baseFXRobot, robot, controller, rowIndex);
    }

    public FxRobot expandNode(int nodeToExpand) {
        waitUntil(() -> getNodes(".tree-disclosure-node > .arrow").size() >= nodeToExpand, 5000L);
        return clickOn(getNodes(".tree-disclosure-node > .arrow").get(nodeToExpand));
    }
}
