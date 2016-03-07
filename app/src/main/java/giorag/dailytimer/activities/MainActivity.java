package giorag.dailytimer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

import giorag.dailytimer.Daily;
import giorag.dailytimer.R;
import giorag.dailytimer.enums.BufferType;
import giorag.dailytimer.interfaces.TeamNamesEditDialogListener;
import giorag.dailytimer.TinyDB;
import giorag.dailytimer.modals.Person;
import giorag.dailytimer.modals.Time;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        TeamNamesEditDialogListener,
        Daily.OnDailyFinishListener,
        Daily.OnPersonChangedListener
{
    public static final String CMD_PLAY = "{cmd-play}";
    public static final String CMD_REPLAY = "{cmd-replay}";
    public static final String CMD_PAUSE = "{cmd-pause}";
    public static final String CMD_STOP = "{cmd-stop}";
    long totalTime;
    long speakingTime;
    long bufferTime;
    BufferType bufferType;

    Button pause;
    Button start;
    ImageButton next;
    MediaPlayer ringtonePlayer;

    TextView totalTimer;
    TextView personalTimer;
    TextView bufferTimer;
    TextView participantLabel;
    TinyDB db;
    Daily daily;
    SharedPreferences preferences;
    ArrayList<Person> people;
    Vibrator dildo;

    @Override
    public void onPersonsListUpdate(ArrayList<Person> people) {
        this.people = people;
        ArrayList<Person> availablePeople = getAvailablePeople();

        initializeTimerTime(availablePeople.size());
        initializeDaily(availablePeople);
        initializeViews();
    }

    @NonNull
    private ArrayList<Person> getAvailablePeople() {
        ArrayList<Person> availablePeople = new ArrayList<>();
        for (Person p : this.people) {
            if (p.available) {
                availablePeople.add(p);
            }
        }
        return availablePeople;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        daily.saveState();
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        log("onRestoreInstanceState");
        daily.restoreState(this, totalTimer, personalTimer, bufferTimer, participantLabel);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        db = new TinyDB(this);
        initializeSettings();

        log("Speaking time : " + Time.fromLong(speakingTime).toString());
        log("Total time : " + Time.fromLong(totalTime).toString());
        log("Buffer time : " + Time.fromLong(bufferTime).toString());

        pause = (Button)findViewById(R.id.main_pause);
        start = (Button)findViewById(R.id.main_start);
        next = (ImageButton)findViewById(R.id.move_to_next_person);
        totalTimer = (TextView)findViewById(R.id.main_timer);
        personalTimer = (TextView)findViewById(R.id.person_timer);
        bufferTimer = (TextView)findViewById(R.id.buffer_timer);
        participantLabel = (TextView)findViewById(R.id.current_practicipent_name);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClick();
            }
        });

        initializeDaily(getAvailablePeople());
        initializeViews();
    }

    private void initializeDaily(ArrayList<Person> availablePeople) {
        daily = new Daily(Time.fromLong(totalTime), Time.fromLong(speakingTime), Time.fromLong(bufferTime),
                availablePeople, bufferType, 0, totalTimer, personalTimer, bufferTimer, participantLabel, this);

        daily.setOnDailyFinishListener(this);
        daily.setOnPersonChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void setNextButton(boolean enabled) {
        next.setEnabled(enabled);
        next.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_playback_forw));

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextClick();
            }
        });
    }

    private void onNextClick() {
        if (daily.isNotLastPerson())
            daily.next();
        else {
            daily.finish();
            setRestartButton();
        }
    }

    private void setRestartButton() {
        next.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_autorenew));
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRestartClick();
            }
        });
    }

    private void onRestartClick() {
        onResetClick();
        next.setEnabled(false);
        start.setEnabled(true);
    }

    private void setStartButton(boolean enabled) {
        start.setText(CMD_PLAY);
        start.setEnabled(enabled);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartClick();
            }
        });
    }

    public void onStartClick() {
        log("Pressed START!");
        daily.start();
        start.setEnabled(false);
        setPauseButton(true);
        setNextButton(true);
    }

    private void setResumeButton(boolean enabled) {
        start.setText(CMD_REPLAY);
        start.setEnabled(enabled);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResumeClick();
            }
        });
    }

    public void onResumeClick() {
        log("Pressed RESUME!");
        daily.start();
        start.setEnabled(false);
        setPauseButton(true);
    }

    private void setPauseButton(boolean enabled) {
        pause.setText(CMD_PAUSE);
        pause.setEnabled(enabled);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseClick();
            }
        });
    }

    public void onPauseClick() {
        log("Pressed PAUSE!");
        daily.pause();
        setResumeButton(true);
        setResetButton(true);
    }

    private void setResetButton(boolean enabled) {
        pause.setText(CMD_STOP);
        pause.setEnabled(enabled);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetClick();
            }
        });
    }

    public void onResetClick() {
        log("Pressed RESET!");
        daily.reset();
        setStartButton(true);
        setPauseButton(false);
        next.setEnabled(false);
    }

    private void initializeSettings() {
        people = new ArrayList<>();
        int peopleAmount = 0;

        try {
            ArrayList<Object> objects = db.getListObject(TeamNamesEditDialog.PEOPLE, Person.class);
            for (Object obj : objects) {
                Person p = (Person) obj;
                people.add(p);
                if (p.available) {
                    peopleAmount++;
                }
            }
        }
        catch (NullPointerException e) {
            peopleAmount = 0;
        }

        initializeTimerTime(peopleAmount);
    }

    private void initializeTimerTime(int peopleAmount) {
        speakingTime = Long.parseLong(preferences.getString("speaking_time", "30")) * 1000;
        int transitionBuffer = Integer.parseInt(preferences.getString("transition_buffer", "0"));
        bufferTime = Long.parseLong(preferences.getString("transition_buffer_time", "5")) * 1000;

        long totalTime = peopleAmount * speakingTime;
        switch (transitionBuffer) {
            case 0: {
                bufferType = BufferType.Team;
                totalTime += bufferTime;
                break;
            }
            case 1: {
                bufferType = BufferType.Individual;
                totalTime += bufferTime * peopleAmount;
                break;
            }
            default: break;
        }

        this.totalTime = totalTime;

        log("Speaking time : " + Time.fromLong(speakingTime).toString());
        log("Total time : " + Time.fromLong(this.totalTime).toString());
        log("Buffer time : " + Time.fromLong(bufferTime).toString());
    }

    private void log(String message) {
        Log.i("Daily-Timer", message);
    }

    public void initializeViews() {
        daily.reset();
        setStartButton(true);
        setPauseButton(false);
        next.setEnabled(false);
    }

    public void launchSettings(MenuItem item) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
    }

    private void showDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TeamNamesEditDialog editNamesDialog = new TeamNamesEditDialog();
        editNamesDialog.show(fm, "fragment_edit_names");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera)
            showDialog();
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFinish() {
        playRingtone();
        setRestartButton();
        setStartButton(false);
        setPauseButton(false);
    }

    @Override
    public void onPersonChanged(Person newPerson) {
        participantLabel.setText(newPerson.name);
        playRingtone();
    }

    private void playRingtone() {
        boolean shouldPlayRingtone = preferences.getBoolean("should_play_ringtone", false);

        if(!shouldPlayRingtone)
            return;

        boolean shouldVibrate = preferences.getBoolean("should_vibrate_on_ringtone", false);
        if(shouldVibrate) {
            dildo = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            dildo.vibrate(400);
        }

        Uri notification = Uri.parse(preferences.getString("ringtone_sound", ""));
        if(ringtonePlayer != null) {
            ringtonePlayer.release(); }
        ringtonePlayer = MediaPlayer.create(this, notification);
        ringtonePlayer.start();
    }

}


