package giorag.dailytimer;

import android.os.CountDownTimer;

import giorag.dailytimer.modals.Time;

public class DailyCountdown extends CountDownTimer {

    private static final long INTERVAL = 100;
    private long remaining;
    private OnTickListener onTickListener;
    private OnFinishListener onFinishListener;
    public boolean isRunning;

    public void setOnFinishListener(OnFinishListener onFinish) {
        this.onFinishListener = onFinish;
    }

    public void setOnTickListener(OnTickListener onTick) {
        this.onTickListener = onTick;
    }

    public DailyCountdown(Time time) {
        super(time.toLong(), INTERVAL);

        this.remaining = time.toLong();
        this.isRunning = false;
    }

    public void myStart() {
        isRunning = true;
        start();
    }

    public void myCancel() {
        isRunning = false;
        cancel();
    }


    public DailyCountdown reset(long remaining, boolean startOnReset) {
        myCancel();
        isRunning = false;
        DailyCountdown newCountdown = new DailyCountdown(Time.fromLong(remaining));
        newCountdown.setOnFinishListener(onFinishListener);
        newCountdown.setOnTickListener(onTickListener);
        if (startOnReset)
            newCountdown.myStart();
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
        isRunning = false;
        if (onFinishListener != null)
            onFinishListener.onFinish();
    }
}
