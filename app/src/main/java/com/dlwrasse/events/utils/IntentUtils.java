package com.dlwrasse.events.utils;

import android.net.Uri;

public class IntentUtils {

    public static Uri getAppMarketPageUri(String packageName) {
        return Uri.parse("market://details?id=" + packageName);
    }

    public static Uri getAppPageUri(String packageName) {
        return Uri.parse("http://play.google.com/store/apps/details?id=" + packageName);
    }

    public static Uri getEmailUri(String emailAddress, String emailSubject) {
        return Uri.parse("mailto:" + emailAddress + "?subject=" + Uri.encode(emailSubject));
    }
}
