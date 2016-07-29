package de.qabel.desktop.util;

import de.qabel.core.accounting.QuotaState;

import static humanize.Humanize.binaryPrefix;

public class QuotaUtil {

    public static int ratioByDiff(long usedQuota, long availableQuota) {
        return (int) (usedQuota / (double) availableQuota * 100);
    }

    public static String getQuotaDescription(QuotaState q, String freeLabel) {
        String seperator = " " + freeLabel + " / ";
        return binaryPrefix(q.getQuota() - q.getSize()) + seperator + binaryPrefix(q.getQuota());
    }

    public static int getUsedRatio(QuotaState q) {
        return (int) (q.getSize() / (double) q.getQuota() * 100);
    }
}
