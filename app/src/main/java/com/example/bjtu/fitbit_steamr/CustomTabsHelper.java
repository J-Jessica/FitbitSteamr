package com.example.bjtu.fitbit_steamr;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

public class CustomTabsHelper {

    public static void openUrl(Context context, String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(Color.RED);
        builder.setShowTitle(true);
//        builder.setCloseButtonIcon(BitmapFactory.decodeResource(
//                context.getResources(), R.drawable
//                        .ic_arrow_back_black_24dp));
        builder.addDefaultShareMenuItem();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
}
