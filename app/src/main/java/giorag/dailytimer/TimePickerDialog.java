package giorag.dailytimer;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerDialog extends DialogPreference {
    TimePicker timePicker;
    private Listener timeListener;
    String key;
    SharedPreferences preferences;

    private static final String KEY_PREVIOUS_TIME_SET = "previous_time_set";

    public TimePickerDialog (Context context) {
        this(context, null);
    }

    public TimePickerDialog (Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePickerDialog (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        timeListener=(Listener) context;

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
        preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    private String getTimeFromSettings() {
        final Calendar clr = Calendar.getInstance();
        final String defaultTime = clr.get(clr.HOUR_OF_DAY) + ":" + clr.get(clr.MINUTE);
        return preferences.getString(KEY_PREVIOUS_TIME_SET, defaultTime);
    }

    @Override
    public View onCreateDialogView(){
        timePicker =  new TimePicker(getContext());
        timePicker.setIs24HourView(true);

        if (Build.VERSION.SDK_INT >= 23)
            setPreviousTime();

        return timePicker;
    }

    @TargetApi(23)
    private void setPreviousTime() {
        String time = getTimeFromSettings();
        String[] hourOfDayAndMinute = time.split(":");
        int hourOfDay = Integer.parseInt(hourOfDayAndMinute[0]);
        int minute = Integer.parseInt(hourOfDayAndMinute[1]);
        timePicker.setHour(hourOfDay);
        timePicker.setMinute(minute);
    }

    private void saveTimeInSettings(int hourOfDay, int minute) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PREVIOUS_TIME_SET, hourOfDay + ":" + minute);
        editor.commit();
    }

    public void onDialogClosed(boolean positiveResult){
        if (timeListener != null && positiveResult) {
            int timeOfDay = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();
            saveTimeInSettings(timeOfDay, minute);
            timeListener.setTime(key, timeOfDay, minute);
        }
    }

    public interface Listener {
        void setTime(String key,int hour,int minute);
    }
}