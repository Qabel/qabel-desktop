package de.qabel.desktop.ui.inject;

import com.airhacks.afterburner.injection.Injector;
import com.airhacks.afterburner.injection.PresenterFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AfterburnerInjectorTest {
    @Test
    public void injectionContextWithNoBeanOrPrimitive() {
        NotABean expected = new NotABean(25);
        Map<String, Object> injectionContext = new HashMap<>();
        injectionContext.put("name", expected);
        PresenterFactory injector = new AfterburnerInjector();
        PresenterWithNotABeanField withField = injector.instantiatePresenter(PresenterWithNotABeanField.class, injectionContext::get);
        assertThat(withField.getName(), is(expected));
        AfterburnerInjector.forgetAll();
        Injector.forgetAll();
    }
}
