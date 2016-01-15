package de.qabel.desktop.ui.accounting;

import de.qabel.desktop.ui.AbstractControllerTest;
import org.junit.Test;
import java.util.Locale;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class QabelFXMLViewTest extends AbstractControllerTest {
	AccountingController controller;

	@Test
	public void deLocalTest() {
		Locale.setDefault(new Locale("de", "DE"));
		createController();
		assertThat(controller.getRessource().getLocale().getCountry(), is("DE"));
		assertThat(controller.getRessource().getLocale().getLanguage(), is("de"));
	}

	@Test
	public void enLocalTest() {
		Locale.setDefault(new Locale("en", "EN"));
		createController();
		assertThat(controller.getRessource().getLocale().getCountry(), is(""));
		assertThat(controller.getRessource().getLocale().getLanguage(), is(""));
	}

	@Test
	public void chLocalTest() {
		Locale.setDefault(new Locale("de", "CH"));
		createController();
		assertThat(controller.getRessource().getLocale().getCountry(), is(""));
		assertThat(controller.getRessource().getLocale().getLanguage(), is(""));
	}

	@Test
	public void rndLocalTest() {
		Locale.setDefault(new Locale("rd", "DE"));
		createController();
		assertThat(controller.getRessource().getLocale().getCountry(), is(""));
		assertThat(controller.getRessource().getLocale().getLanguage(), is(""));
	}

	private void createController() {
		AccountingView view = new AccountingView();
		controller = (AccountingController) view.getPresenter();
	}

}
