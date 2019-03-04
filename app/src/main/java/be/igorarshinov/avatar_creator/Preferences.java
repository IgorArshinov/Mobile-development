package be.igorarshinov.avatar_creator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    private Preferences() {
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getItemListCurrentValuePreference(Context context) {
        return getSharedPreferences(context).getString("preference_item_list_key", "Date");
    }
}
