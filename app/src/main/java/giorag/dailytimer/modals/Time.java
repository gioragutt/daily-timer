package giorag.dailytimer.modals;

public class Time
{
    public long minute;
    public long second;
    public long milli;

    public Time(long minute, long second, long milli) {
        this.minute = minute;
        this.second = second;
        this.milli = milli;
    }

    @Override
    public String toString() {
        String seconds = "" + second;
        if (second < 10)
            seconds = "0" + seconds;
        return minute + ":" + seconds;
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
