package com.codernauti.gamebank.util;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.codernauti.gamebank.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Eduard on 07-Mar-18.
 */

public class SharePrefUtil {

    public static final String DEFAULT_STRING_VALUE = "default";


    public static void saveStringPreference(Context context, String key, String data) {
        Log.d("Save string preference", key + ": " + data);

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(key, data)
                .apply();
    }


    public static String getStringPreference(Context context, String key){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(key, DEFAULT_STRING_VALUE);
    }

    public static String getNicknamePreference(Context context) {
        String nick = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PrefKey.NICKNAME, DEFAULT_STRING_VALUE);

        if (nick.equals(DEFAULT_STRING_VALUE)) {

            NicknameGenerator ng = new NicknameGenerator(context);

            nick = ng.getRandomNameWithoutPreferences();
            saveStringPreference(context, PrefKey.NICKNAME, nick);
        }

        return nick;
    }

}
