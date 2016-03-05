package giorag.dailytimer;

import android.os.CountDownTimer;
import android.widget.TextView;

import giorag.dailytimer.activities.MainActivity;
import giorag.dailytimer.modals.Time;

public class DailyCountdown extends CountDownTimer {

    private long remaining;
    private long interval;
    private TextView timer;

    public DailyCountdown(Time time, long interval, TextView timer) {
        super(time.toLong(), interval);

        this.remaining = time.toLong();
        this.interval = interval;
        this.timer = timer;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getInterval() {
        return interval;
    }

    @Override
    public void onTick(long remaining) {
        timer.setText(Time.fromLong(remaining).toString());
        this.remaining = remaining;
    }

    @Override
    public void onFinish() {
        timer.setText("Done!");
    }
}
