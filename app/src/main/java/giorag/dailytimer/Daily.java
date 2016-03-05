package giorag.dailytimer;

import java.util.ArrayList;

import giorag.dailytimer.enums.BufferType;
import giorag.dailytimer.modals.Person;
import giorag.dailytimer.modals.Time;

public class Daily {
    Time total;
    Time personal;
    Time buffer;
    BufferType bufferType;
    DailyCountdown totalCountdown;
    DailyCountdown personalCountdown;
    DailyCountdown bufferCountdown;
    ArrayList<Person> people;
    int currentPerson;

}
