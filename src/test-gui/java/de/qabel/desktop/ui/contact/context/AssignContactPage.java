package de.qabel.desktop.ui.contact.context;

import com.sun.javafx.robot.FXRobot;
import de.qabel.core.config.Identity;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class AssignContactPage extends AbstractPage {
    public AssignContactPage(FXRobot baseFXRobot, FxRobot robot) {
        super(baseFXRobot, robot);
    }

    public void waitForIdentity(Identity identity) {
        waitForNode("#assign-" + identity.getId());
    }

    public void waitForIgnore() {
        waitForNode("#ignore-");
    }
}
