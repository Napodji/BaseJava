package com.basejava.webapp.util;

import com.basejava.webapp.model.Organization;

public class HtmlHelper {
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String formatDates(Organization.Position position) {
        return DateHelper.format(position.getStartDate()) + " - " + DateHelper.format(position.getEndDate());
    }
}