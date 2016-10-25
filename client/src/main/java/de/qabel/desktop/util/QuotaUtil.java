package de.qabel.desktop.util;

import de.qabel.core.accounting.QuotaState;

import java.text.MessageFormat;
import java.util.Locale;

import static humanize.Humanize.binaryPrefix;

public class QuotaUtil {

    public static String getQuotaDescription(QuotaState q, String quotaDescriptionPattern) {
        String usedQuota = binaryPrefix(q.getSize(), Locale.getDefault());
        String totalQuota = binaryPrefix(q.getQuota(), Locale.getDefault());

        return MessageFormat.format(quotaDescriptionPattern, usedQuota, totalQuota);
    }

    public static int getUsedRatioInPercent(QuotaState q) {
        if (q.getSize() == q.getQuota()) {
            return 0;
        }
        return (int) (q.getSize() / (double) q.getQuota() * 100);
    }
}
