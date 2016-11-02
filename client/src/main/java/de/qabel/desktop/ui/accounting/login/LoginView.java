package de.qabel.desktop.ui.accounting.login;

import com.airhacks.afterburner.views.QabelFXMLView;

public class LoginView extends QabelFXMLView {
    @Override
    public LoginController getPresenter() {
        return (LoginController)super.getPresenter();
    }
}
