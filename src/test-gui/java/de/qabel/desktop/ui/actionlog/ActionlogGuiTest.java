package de.qabel.desktop.ui.actionlog;

import com.airhacks.afterburner.views.FXMLView;
import de.qabel.desktop.repository.exception.EntityNotFoundExcepion;
import de.qabel.desktop.ui.AbstractGuiTest;
import de.qabel.desktop.ui.accounting.AccountingController;
import de.qabel.desktop.ui.accounting.AccountingView;
import javafx.scene.control.ButtonType;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActionlogGuiTest extends AbstractGuiTest<AccountingController> {
	@Override
	protected FXMLView getView() {
		return new ActionlogView();
	}

	@Test
	public void testAddsIdentity() throws EntityNotFoundExcepion {

	}
}
