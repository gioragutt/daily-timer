package giorag.dailytimer;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import giorag.dailytimer.activities.MainActivity;
import giorag.dailytimer.enums.BufferType;
import giorag.dailytimer.enums.RunningState;
import giorag.dailytimer.interfaces.OnDailyFinishListener;
import giorag.dailytimer.interfaces.OnPersonChangedListener;
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
    RunningState runningState;

    OnDailyFinishListener onDailyFinishListener;
    OnPersonChangedListener onPersonChangedListener;

    TextView participantLabel;
    TextView totalTimer;
    TextView personalTimer;
    TextView bufferTimer;

    TinyDB db;
    Context context;
    boolean shouldNotifyNextPerson;

    public Daily(Time total,
                 Time personal,
                 Time buffer,
                 ArrayList<Person> people,
                 BufferType bufferType,
                 int currentPerson,
                 TextView totalTimer,
                 TextView personalTimer,
                 TextView bufferTimer,
                 TextView participantLabel,
                 Context context) {
        this.total = total;
        this.personal = personal;
        this.buffer = buffer;
        this.people = people;
        this.bufferType = bufferType;
        if (bufferType == BufferType.None)
            buffer = Time.ZERO;
        this.currentPerson = currentPerson;
        this.totalTimer = totalTimer;
        this.personalTimer = personalTimer;
        this.bufferTimer = bufferTimer;
        this.participantLabel = participantLabel;
        this.runningState = RunningState.Default;
        this.context = context;
        this.db = new TinyDB(context);
        this.shouldNotifyNextPerson = true;

        log("Constructing Daily!");
        log("Total : " + total.toString());
        log("Personal : " + personal.toString());
        log("Buffer : " + buffer.toString());
        log("People : " + people.toString());
        log("Buffer Type " + bufferType.toString());
        log("Current Person : " + currentPerson);
        log("Running State : " + runningState.toString());

        initializeCountdownTimers();
        setParticipant(currentPerson);
    }

    private void log(String message) {
        Log.i("Daily", message);
    }

    public void setOnDailyFinishListener(OnDailyFinishListener onDailyFinishListener) {
        this.onDailyFinishListener = onDailyFinishListener;
    }

    public void setOnPersonChangedListener(OnPersonChangedListener onPersonChangedListener) {
        this.onPersonChangedListener = onPersonChangedListener;
    }

    private void initializeCountdownTimers() {
        log("Initializing total with " + this.total);
        totalCountdown = new DailyCountdown(this.total);
        totalCountdown.setOnFinishListener(new DailyCountdown.OnFinishListener() {
            @Override
            public void onFinish() {
                onTotalFinish();
            }
        });
        totalCountdown.setOnTickListener(new DailyCountdown.OnTickListener() {
            @Override
            public void onTick(long remaining) {
                onTotalTick(remaining);
            }
        });

        log("Initializing personal with " + this.personal);
        personalCountdown = new DailyCountdown(this.personal);
        personalCountdown.setOnFinishListener(new DailyCountdown.OnFinishListener() {
            @Override
            public void onFinish() {
                onPersonalFinish();
            }
        });
        personalCountdown.setOnTickListener(new DailyCountdown.OnTickListener() {
            @Override
            public void onTick(long remaining) {
                onPersonalTick(remaining);
            }
        });

        log("Initializing buffer with " + this.buffer);
        bufferCountdown = new DailyCountdown(this.buffer);
        bufferCountdown.setOnFinishListener(new DailyCountdown.OnFinishListener() {
            @Override
            public void onFinish() {
                onBufferFinish();
            }
        });
        bufferCountdown.setOnTickListener(new DailyCountdown.OnTickListener() {
            @Override
            public void onTick(long remaining) {
                onBufferTick(remaining);
            }
        });

        initializeTimerLabels();
    }

    private void initializeTimerLabels() {
        totalTimer.setText(total.toString());
        personalTimer.setText(personal.toString());
        bufferTimer.setText("-" + buffer.toString());
    }

    private void updateTimerLabels() {
        totalTimer.setText(Time.fromLong(totalCountdown.getRemainingLong()).toString());
        personalTimer.setText(Time.fromLong(personalCountdown.getRemainingLong()).toString());
        bufferTimer.setText("-" + Time.fromLong(bufferCountdown.getRemainingLong()).toString());
    }

    private void onPersonalTick(long remaining) {
        personalTimer.setText(Time.fromLong(remaining).toString());

        if (remaining <= 15000 && shouldNotifyNextPerson)
        {
            shouldNotifyNextPerson = false;
            if (currentPerson < people.size() - 1) {
                Toast.makeText(context, "Next up: " + people.get(currentPerson + 1).name, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onTotalTick(long remaining) {
        totalTimer.setText(Time.fromLong(remaining).toString());
    }

    private void onBufferTick(long remaining) {
        bufferTimer.setText("-" + Time.fromLong(remaining).toString());
    }

    private void onPersonalFinish() {
        shouldNotifyNextPerson = true;
        if (!isNotLastPerson())
            finish();
        else if (bufferCountdown.getRemainingLong() > 0)
            bufferCountdown.start();
        else
            next();
    }

    public boolean isNotLastPerson() {
        return currentPerson < people.size() - 1;
    }

    private void onTotalFinish() {
        log("OnTotalFinish");
        finish();
    }

    private void onBufferFinish() {
        log("OnBufferFinish");
        switch (bufferType) {
            case Individual:
                onIndividualBufferFinish();
                break;
            case Team:
                onTeamBufferFinish();
                break;
        }
    }

    private void onTeamBufferFinish() {
        if (!isNotLastPerson())
            finish();
        else
            next();
    }

    private void onIndividualBufferFinish() {
        if (isNotLastPerson()) {
            log("OnBufferFinish - not last person, nexting");
            next();
        }
        else {
            log("OnBufferFinish - last person " + currentPerson + " ending");
            finish();
        }
    }

    public void finish() {
        reset();
        totalTimer.setText("Done!");
        personalTimer.setText("Done!");
        bufferTimer.setText("Done!");
        participantLabel.setText("All done!");

        if (onDailyFinishListener != null)
            onDailyFinishListener.onFinish();

    }

    private void setParticipant(int index) {
        if (index < 0 || index > people.size())
            throw new InvalidParameterException("Participant index must be between 0 and " + (people.size() - 1));

        if (people.size() <= 0) {
            participantLabel.setText("Team List is empty!");
            return;
        }

        currentPerson = index;
        participantLabel.setText(people.get(index).name);
    }

    public void restoreState(MainActivity main,
                             BufferType bufferType,
                             TextView totalTimer,
                             TextView personalTimer,
                             TextView bufferTimer,
                             TextView participantLabel) {
        this.participantLabel = participantLabel;
        this.totalTimer = totalTimer;
        this.personalTimer = personalTimer;
        this.bufferTimer = bufferTimer;
        this.db = new TinyDB(main);

        //bufferType = BufferType.valueOf(db.getString("bufferType"));
        total = Time.fromLong(db.getLong("total", 0));
        personal = Time.fromLong(db.getLong("personal", 0));
        buffer = bufferType == BufferType.None ?  buffer = Time.ZERO : Time.fromLong(db.getLong("buffer", 0));
        currentPerson = db.getInt("currentPerson", 0);
        people = new ArrayList<>();

        ArrayList<Object> personObjects = db.getListObject("people", Person.class);
        for (Object p : personObjects)
            people.add((Person) p);

        runningState = RunningState.valueOf(db.getString("runningState"));

        totalCountdown = totalCountdown.reset(db.getLong("totalCountdown", 0), db.getBoolean("totalCountdown-running", false));
        personalCountdown = personalCountdown.reset(db.getLong("personalCountdown", 0), db.getBoolean("personalCountdown-running", false));
        bufferCountdown = bufferCountdown.reset(bufferType == BufferType.None ? 0 : db.getLong("bufferCountdown", 0),
                                                db.getBoolean("bufferCountdown-running", false));

        setOnDailyFinishListener(main);
        setOnPersonChangedListener(main);
        runningState.restore(main);

        log("=================================");
        log("Constructing Daily!");
        log("Total : " + total.toString());
        log("Personal : " + personal.toString());
        log("Buffer : " + buffer.toString());
        log("People : " + people.toString());
        log("Buffer Type " + bufferType.toString());
        log("Current Person : " + currentPerson);
        log("Running State : " + runningState.toString());
        log("=================================");
    }

    public void saveState() {
        db.putLong("total", total.toLong());
        db.putLong("personal", personal.toLong());
        db.putLong("buffer", buffer.toLong());
        db.putString("bufferType", bufferType.toString());
        db.putInt("currentPerson", currentPerson);
        db.putListObject("people", new ArrayList<Object>(people));
        db.putString("runningState", runningState.toString());

        db.putLong("personalCountdown", personalCountdown.getRemainingLong());
        db.putBoolean("personalCountdown-running", personalCountdown.isRunning);
        db.putLong("bufferCountdown", bufferCountdown.getRemainingLong());
        db.putBoolean("bufferCountdown-running", bufferCountdown.isRunning);
        db.putLong("totalCountdown", totalCountdown.getRemainingLong());
        db.putBoolean("totalCountdown-running", totalCountdown.isRunning);
    }

    public void start() {
        runningState = RunningState.Running;
        totalCountdown.start();
        personalCountdown.start();
    }

    public void pause() {
        runningState = RunningState.Paused;
        totalCountdown = totalCountdown.reset(totalCountdown.getRemainingLong(), false);
        personalCountdown = personalCountdown.reset(personalCountdown.getRemainingLong(), false);
        bufferCountdown = bufferCountdown.reset(bufferCountdown.getRemainingLong(), false);
    }

    public void reset() {
        runningState = RunningState.Default;
        totalCountdown = totalCountdown.reset(total.toLong(), false);
        personalCountdown = personalCountdown.reset(personal.toLong(), false);
        bufferCountdown = bufferCountdown.reset(buffer.toLong(), false);
        currentPerson = 0;
        setParticipant(currentPerson);

        initializeTimerLabels();
    }

    public void next() {
        currentPerson++;
        setParticipant(currentPerson);
        procOnPersonChanged();
        personalCountdown = personalCountdown.reset(personal.toLong(), runningState == RunningState.Running);
        log("next - currentPerson [ " + currentPerson + " " + people.get(currentPerson).name + " ] runningState [ " + runningState.toString() + " ]");

        switch (bufferType) {
            case Individual:
                bufferCountdown = bufferCountdown.reset(buffer.toLong(), false);
                break;
            default:
                bufferCountdown = bufferCountdown.reset(bufferCountdown.getRemainingLong(), false);
                break;
        }

        updateTimerLabels();
    }

    private void procOnPersonChanged() {
        if (onPersonChangedListener != null)
            onPersonChangedListener.onPersonChanged(people.get(currentPerson));
    }

    public static BufferType convert(int num) {
        switch(num) {
            case 0: return BufferType.Team;
            case 1: return BufferType.Individual;
            case 2: return BufferType.None;
            default: return BufferType.None;
        }
    }
}
