package de.qabel.desktop.util;

import de.qabel.core.accounting.QuotaState;
import org.junit.Before;
import org.junit.Test;

import static de.qabel.desktop.util.QuotaUtil.getQuotaDescription;
import static de.qabel.desktop.util.QuotaUtil.getUsedRatio;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

public class QuotaUtilTest {

    private QuotaState noSpaceQuota;
    private QuotaState qs10Free;
    private static final String QUOTA_PATTERN = "{0} free of {1}";

    @Before
    public void setUp() {
        noSpaceQuota = new QuotaState(100, 100);
        qs10Free = new QuotaState(100, 10);
    }

    @Test
    public void ratioWithNoFreeSpace() {
        int usedRatio = getUsedRatio(noSpaceQuota);
        assertEquals(0, usedRatio);
    }

    @Test
    public void ratioWithFreeSpace() {
        int usedRatio = getUsedRatio(qs10Free);
        assertEquals(10, usedRatio);
    }

    @Test
    public void quotaDescriptionWith90Free() {
        String quotaDescription = getQuotaDescription(qs10Free, QUOTA_PATTERN);
        assertThat(quotaDescription, containsString("90"));
    }

    @Test
    public void quotaDescriptionWithNoFreeSpace() {
        String quotaDescription = getQuotaDescription(noSpaceQuota, QUOTA_PATTERN);
        assertThat(quotaDescription, containsString("0"));
    }
}
