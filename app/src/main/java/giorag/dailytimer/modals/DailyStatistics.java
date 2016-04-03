package giorag.dailytimer.modals;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by GioraPC on 03/04/2016.
 */

public class DailyStatistics implements Serializable {
    Date dateOfDaily;
    Time totalAllocated;
    Time actualUsed;
    Time skipped;
    Time personalAllocated;
    Time bufferAllocated;
    Time bufferUsed;
    ArrayList<Person> people;

    public DailyStatistics(Time totalAllocated, Time personalAllocated, Time bufferAllocated, ArrayList<Person> people) {
        this.dateOfDaily = new Date();
        this.totalAllocated = totalAllocated;
        this.personalAllocated = personalAllocated;
        this.bufferAllocated = bufferAllocated;
        this.people = people;

        this.actualUsed = Time.ZERO;
        this.skipped = Time.ZERO;
        this.bufferUsed = Time.ZERO;
    }

    public void personFinished(Time used, Time skippedTime) {
        actualUsed = actualUsed.add(used);
        skipped = skipped.add(skippedTime);

        long usedLong = used.toLong(), personalLong = personalAllocated.toLong();

        if (usedLong > personalLong) {
            Time buffer = Time.fromLong(usedLong - personalLong);
            bufferUsed = bufferUsed.add(buffer);
        }
    }

    private Time getAverageTime(Time time) {
        return Time.fromLong(time.toLong() / people.size());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = sdf.format(dateOfDaily);

        builder.append("Date dateOfDaily :\n" + formattedDate + "\n\n");
        builder.append("Participants :\n" );
        for (int i = 0; i < people.size(); ++i) {
            builder.append("\t" + (i + 1) + " - " + people.get(i).name + "\n");
        }
        builder.append("\n");
        builder.append("int personCount : " + people.size() + "\n");
        builder.append("Time totalAllocated : " + totalAllocated.toLongString() + "\n");
        builder.append("Time personalAllocated : " + personalAllocated.toLongString() + "\n");
        builder.append("Time bufferAllocated : " + bufferAllocated.toLongString() + "\n");
        builder.append("\n");
        builder.append("Time actualUsed : " + actualUsed.toLongString() + "\n");
        builder.append("Time averageUsed : " + getAverageTime(actualUsed).toLongString() + "\n");
        builder.append("\n");
        builder.append("Time bufferUsed : " + bufferUsed.toLongString() + "\n");
        builder.append("Time averageBuffer : " + getAverageTime(bufferUsed).toLongString() + "\n");
        builder.append("\n");
        builder.append("Time skipped : " + skipped.toLongString() + "\n");
        builder.append("Time averageSkipped : " + getAverageTime(skipped).toLongString() + "\n");

        return builder.toString();
    }
}
