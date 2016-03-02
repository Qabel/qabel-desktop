package de.qabel.desktop.ui.accounting;

import com.airhacks.afterburner.views.QabelFXMLView;
import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QabelFXMLViewTest extends AbstractControllerTest {
	AccountingController controller;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		QabelFXMLView.unloadDefaultResourceBundle();
	}

	@Test
	public void deLocalTest() {
		Locale.setDefault(new Locale("de", "DE"));
		createController();
		String str = controller.getRessource().getString("remoteFsSize");
		assertThat(str, is("Größe"));
	}

	@Test
	public void enLocalTest() {
		Locale.setDefault(new Locale("en", "EN"));
		createController();
		String str = controller.getRessource().getString("remoteFsSize");
		assertThat(str, is("Size"));
	}

	@Test
	public void chLocalTest() {
		Locale.setDefault(new Locale("de", "CH"));
		createController();
		String str = controller.getRessource().getString("remoteFsSize");
		assertThat(str, is("Size"));
	}

	@Test
	public void rndLocalTest() {
		Locale.setDefault(new Locale("rd", "DE"));
		createController();
		String str = controller.getRessource().getString("remoteFsSize");
		assertThat(str, is("Size"));
	}

	private void createController() {
		AccountingView view = new AccountingView();
		controller = (AccountingController) view.getPresenter();
	}

}
