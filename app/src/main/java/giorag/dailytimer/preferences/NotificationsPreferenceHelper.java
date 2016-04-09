package giorag.dailytimer.preferences;

import android.content.Context;

import java.util.Set;

/**
 * Created by GioraPC on 10/04/2016.
 */
public class NotificationsPreferenceHelper extends BasePreferenceHelper {
    private static final String KEY_SHOULD_NOTIFY = "should_notify";
    private static final String KEY_REMINDER_DAYS = "reminder_days";

    private boolean shouldNotify;
    private Set<String> daysToNotify;

    public boolean shouldNotify() {
        return shouldNotify;
    }

    public Set<String> daysToNotify() {
        return daysToNotify;
    }

    public NotificationsPreferenceHelper(Context context) {
        super(context);
        retrievePreferences();
    }

    @Override
    public void retrievePreferences() {
        shouldNotify = getBooleanPreference(KEY_SHOULD_NOTIFY, false);
        daysToNotify = getStringSetPreference(KEY_REMINDER_DAYS);
    }
}
