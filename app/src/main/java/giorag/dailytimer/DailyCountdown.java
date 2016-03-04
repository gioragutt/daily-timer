package giorag.dailytimer;

import android.os.CountDownTimer;

import giorag.dailytimer.activities.MainActivity;
import giorag.dailytimer.modals.Time;

public class DailyCountdown extends CountDownTimer {

    private long remaining;
    private long interval;
    private MainActivity main;

    public DailyCountdown(Time time, long interval, MainActivity main) {
        super(time.toLong(), interval);

        this.remaining = time.toLong();
        this.interval = interval;
        this.main = main;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getInterval() {
        return interval;
    }

    @Override
    public void onTick(long remaining) {
        main.setTimerText(Time.fromLong(remaining));
        this.remaining = remaining;
    }

    @Override
    public void onFinish() {
        main.getTimer().setText("Done!");
    }
}
