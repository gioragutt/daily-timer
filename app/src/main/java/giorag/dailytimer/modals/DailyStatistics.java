package giorag.dailytimer.modals;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.LinkedHashMap;

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
    LinkedHashMap<Person, PersonTimeStatistics> peopleTiming;


    class PersonTimeStatistics implements Serializable {
        public Time used;
        public Time bufferUsed;
        public Time skipped;

        public PersonTimeStatistics(Time used, Time bufferUsed, Time skipped) {
            this.used = used;
            this.bufferUsed = bufferUsed;
            this.skipped = skipped;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ U ").append(used.toLongString())
                    .append(" , B ").append(bufferUsed.toLongString())
                    .append(" , S ").append(skipped.toLongString())
                    .append(" ]");
            return builder.toString();
        }
    }



    public DailyStatistics(Time totalAllocated, Time personalAllocated, Time bufferAllocated, ArrayList<Person> people) {
        this.dateOfDaily = new Date();
        this.totalAllocated = totalAllocated;
        this.personalAllocated = personalAllocated;
        this.bufferAllocated = bufferAllocated;
        peopleTiming = new LinkedHashMap<>();
        for (Person p : people) {
            peopleTiming.put(p, new PersonTimeStatistics(Time.ZERO, Time.ZERO, Time.ZERO));
        }

        this.actualUsed = Time.ZERO;
        this.skipped = Time.ZERO;
        this.bufferUsed = Time.ZERO;
    }

    public void personFinished(Person personThatFinished, Time used, Time skippedTime) {
        actualUsed = actualUsed.add(used);
        skipped = skipped.add(skippedTime);

        PersonTimeStatistics statistics = peopleTiming.get(personThatFinished);
        statistics.used = used;
        statistics.skipped = skippedTime;

        long usedLong = used.toLong(), personalLong = personalAllocated.toLong();

        if (usedLong > personalLong) {
            Time buffer = Time.fromLong(usedLong - personalLong);
            bufferUsed = bufferUsed.add(buffer);
            statistics.bufferUsed = buffer;
        }
    }

    private Time getAverageTime(Time time) {
        return Time.fromLong(time.toLong() / peopleTiming.size());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = sdf.format(dateOfDaily);

        builder.append("Date dateOfDaily :\n" + formattedDate + "\n\n");
        builder.append("Participants :\n" );
        int i = 1;
        for (Person p : peopleTiming.keySet()) {
            builder.append("\t").append(i++).append(" - ").append(p.name).append("\n").append(peopleTiming.get(p).toString()).append("\n");
        }
        builder.append("\n");
        builder.append("int personCount : " + peopleTiming.size() + "\n");
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
