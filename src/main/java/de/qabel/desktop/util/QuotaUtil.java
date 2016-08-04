package de.qabel.desktop.util;

import de.qabel.core.accounting.QuotaState;

import java.text.MessageFormat;
import java.util.Locale;

import static humanize.Humanize.binaryPrefix;

public class QuotaUtil {

    public static int ratioByDiff(long usedQuota, long availableQuota) {
        return (int) (usedQuota / (double) availableQuota * 100);
    }

    public static String getQuotaDescription(QuotaState q, String quotaDescriptionPattern) {
        String usedQuota = binaryPrefix(q.getQuota() - q.getSize(), Locale.getDefault());
        String totalQuota = binaryPrefix(q.getQuota(), Locale.getDefault());

        return MessageFormat.format(quotaDescriptionPattern, usedQuota, totalQuota);
    }

    public static int getUsedRatio(QuotaState q) {
        if (q.getSize() == q.getQuota()) {
            return 0;
        }
        return (int) (q.getSize() / (double) q.getQuota() * 100);
    }
}
