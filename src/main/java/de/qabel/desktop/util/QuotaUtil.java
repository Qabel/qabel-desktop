package de.qabel.desktop.util;

import de.qabel.core.accounting.QuotaState;

import java.text.MessageFormat;

import static humanize.Humanize.binaryPrefix;

public class QuotaUtil {

    public static int ratioByDiff(long usedQuota, long availableQuota) {
        return (int) (usedQuota / (double) availableQuota * 100);
    }

    public static String getQuotaDescription(QuotaState q, String quotaDescriptionPattern) {
        String usedQuota = binaryPrefix(q.getQuota() - q.getSize());
        String totalQuota = binaryPrefix(q.getQuota());
        return MessageFormat.format(quotaDescriptionPattern, usedQuota, totalQuota);
    }

    public static int getUsedRatio(QuotaState q) {
        return (int) (q.getSize() / (double) q.getQuota() * 100);
    }
}
