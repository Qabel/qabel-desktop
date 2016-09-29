package de.qabel.desktop.ui.accounting.wizard;

import com.sun.javafx.robot.FXRobot;
import de.qabel.desktop.ui.AbstractPage;
import org.testfx.api.FxRobot;

public class WizardPage extends AbstractPage {

    private WizardController controller;

    public WizardPage(FXRobot baseFXRobot, FxRobot robot, WizardController controller) {
        super(baseFXRobot, robot);
        this.controller = controller;
    }

    public void showPopup() {
        runLaterAndWait(controller::showPopup);
        waitUntil(() -> controller.wizardPane.isVisible());
    }

    public void enterAlias(String alias) {
        clickOn("#aliasInput").write(alias);
    }

    public void next(){
        clickOn("#nextButton");
        waitUntil(()-> controller.getCurrentStep().isVisible());
    }

    public void finish(){
        clickOn("#finishButton");
    }
}
