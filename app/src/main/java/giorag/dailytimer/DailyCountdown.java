package giorag.dailytimer;

import android.os.CountDownTimer;

import giorag.dailytimer.modals.Time;

public class DailyCountdown extends CountDownTimer {

    private static final long INTERVAL = 100;
    private long remaining;
    private OnTickListener onTickListener;
    private OnFinishListener onFinishListener;

    public void setOnFinishListener(OnFinishListener onFinish) {
        this.onFinishListener = onFinish;
    }

    public void setOnTickListener(OnTickListener onTick) {
        this.onTickListener = onTick;
    }

    public DailyCountdown(Time time) {
        super(time.toLong(), INTERVAL);

        this.remaining = time.toLong();
    }

    public DailyCountdown reset(long remaining, boolean startOnReset) {
        cancel();
        DailyCountdown newCountdown = new DailyCountdown(Time.fromLong(remaining));
        newCountdown.setOnFinishListener(onFinishListener);
        newCountdown.setOnTickListener(onTickListener);
        if (startOnReset)
            newCountdown.start();
        return newCountdown;
    }

    public long getRemaining() {
        return remaining;
    }

    interface OnTickListener {
        void onTick(long remaining);
    }

    interface OnFinishListener {
        void onFinish();
    }

    @Override
    public void onTick(long remaining) {
        this.remaining = remaining;
        if (onTickListener != null)
            onTickListener.onTick(remaining);
    }

    @Override
    public void onFinish() {
        if (onFinishListener != null)
            onFinishListener.onFinish();
    }
}
