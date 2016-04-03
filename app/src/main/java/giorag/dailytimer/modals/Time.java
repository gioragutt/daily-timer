package giorag.dailytimer.modals;

import java.io.Serializable;

public class Time implements Serializable {
    public static final Time ZERO = Time.fromLong(0);

    public long minute;
    public long second;
    public long milli;

    public Time(long minute, long second, long milli) {
        this.minute = minute;
        this.second = second;
        this.milli = milli;
    }

    public Time add(Time other) {
        return Time.fromLong(toLong() + other.toLong());
    }

    @Override
    public String toString() {
        String seconds = "" + second;
        if (second < 10)
            seconds = "0" + seconds;
        return minute + ":" + seconds;
    }

    public String toLongString() {
        String seconds = "" + second;
        if (second < 10)
            seconds = "0" + seconds;
        String milliseconds = "" + milli;
        if (milli < 100)
            milliseconds =  "0" + milliseconds;
        if (milli < 10)
            milliseconds =  "0" + milliseconds;

        return minute + ":" + seconds + "," + milliseconds;
    }

    public long toLong() {
        return (second + 60 * minute) * 1000 + milli;
    }

    public static Time fromLong(long value) {
        long millis = value % 1000;
        long seconds = value / 1000;
        long minutes = seconds / 60;
        seconds %= 60;
        return new Time(minutes, seconds, millis);
    }

}
