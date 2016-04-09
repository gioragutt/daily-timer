package giorag.dailytimer.preferences;

import android.content.Context;

import java.util.ArrayList;
import java.util.Set;

import giorag.dailytimer.TinyDB;

/**
 * Created by GioraPC on 10/04/2016.
 */
abstract class BasePreferenceHelper  {
    Context context;
    TinyDB db;

    protected BasePreferenceHelper(Context context) {
        this.context = context;
        db  = new TinyDB(context);
    }

    protected String getStringPreference(String key, String defaultValue) {
        return db.getString(key, defaultValue);
    }

    protected Set<String> getStringSetPreference(String key) {
        return db.preferences.getStringSet(key, null);
    }

    protected int getIntPreference(String key, int defaultValue) {
        return db.getInt(key, defaultValue);
    }

    protected float getFloatPreference(String key, float defaultValue) {
        return db.getFloat(key, defaultValue);
    }

    protected boolean getBooleanPreference(String key, boolean defaultValue) {
        return db.getBoolean(key, defaultValue);
    }

    protected abstract void retrievePreferences();
}
