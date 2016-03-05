package giorag.dailytimer.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import java.util.ArrayList;

import giorag.dailytimer.DailyCountdown;
import giorag.dailytimer.R;
import giorag.dailytimer.enums.RunningState;
import giorag.dailytimer.interfaces.TeamNamesEditDialogListener;
import giorag.dailytimer.TinyDB;
import giorag.dailytimer.modals.Person;
import giorag.dailytimer.modals.Time;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TeamNamesEditDialogListener {

    public static final String CMD_PLAY = "{cmd-play}";
    public static final String CMD_REPLAY = "{cmd-replay}";
    public static final String CMD_PAUSE = "{cmd-pause}";
    public static final String CMD_STOP = "{cmd-stop}";
    Time defaultTime;
    Button pause;
    Button start;
    TextView timer;
    TinyDB db;
    SharedPreferences preferences;
    DailyCountdown countdown;
    ArrayList<Person> people;
    RunningState runningState;

    @Override
    public void onPersonsListUpdate(ArrayList<Person> people) {
        this.people = people;
        int peopleAmount = 0;
        for (Person p : people) {
            if (p.available) {
                peopleAmount++;
            }
        }

        initializeTimerTime(peopleAmount);
        initializeViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        log("onSaveInstanceState");
        outState.putLong("remaining", countdown.getRemaining());
        outState.putLong("interval", countdown.getInterval());
        outState.putString("runningState", runningState.toString());
        log("onSaveInstanceState - remaining " + countdown.getRemaining());
        log("onSaveInstanceState - interval " + countdown.getInterval());
        log("onSaveInstanceState - runningState " + runningState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        log("onRestoreInstanceState");
        long remaining = savedInstanceState.getLong("remaining");
        long interval = savedInstanceState.getLong("interval");
        RunningState runningState = RunningState.valueOf(savedInstanceState.getString("runningState"));

        log("onRestoreInstanceState - remaining " + remaining);
        log("onRestoreInstanceState - interval " + interval);
        log("onRestoreInstanceState - runningState " + runningState);

        this.runningState = runningState;
        resetCountdown(runningState, remaining, interval);
        runningState.restore(this);
        setTimerText(Time.fromLong(remaining));
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

        pause = (Button)findViewById(R.id.main_pause);
        start = (Button)findViewById(R.id.main_start);
        timer = (TextView)findViewById(R.id.main_timer);

        runningState = RunningState.Default;

        initializeViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(runningState == RunningState.Default) {
            initializeSettings();
            initializeViews();
        }
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
        countdown.start();
        runningState = RunningState.Running;
        start.setEnabled(false);
        setPauseButton(true);
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
        runningState = RunningState.Running;
        resetCountdown(true);
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
        runningState = RunningState.Paused;
        resetCountdown(false);
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
        initializeViews();
        runningState = RunningState.Default;
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

        if (peopleAmount < 2)
            peopleAmount = 2;

        initializeTimerTime(peopleAmount);
    }

    private void initializeTimerTime(int peopleAmount) {
        int speakingTime = Integer.parseInt(preferences.getString("speaking_time", "30"));
        int transitionBuffer = Integer.parseInt(preferences.getString("transition_buffer", "0"));
        int transitionBufferTime = Integer.parseInt(preferences.getString("transition_buffer_time", "5"));

        long totalTime = peopleAmount * speakingTime;
        switch (transitionBuffer) {
            case 0: {
                totalTime += transitionBufferTime;
                break;
            }
            case 1: {
                totalTime += transitionBufferTime * peopleAmount;
                break;
            }
            default: break;
        }

        totalTime *= 1000;
        defaultTime = Time.fromLong(totalTime);
    }

    private void log(String message) {
        Log.i("Daily-Timer", message);
    }

    public void setTimerText(Time time) {
        timer.setText(time.toString());
    }

    public TextView getTimer() {
        return timer;
    }

    private void resetCountdown(boolean startOnReset) {
        RunningState state = startOnReset ? RunningState.Running : RunningState.Default;
        resetCountdown(state, countdown.getRemaining(), countdown.getInterval());
    }

    private void resetCountdown(RunningState runningState, long remaining, long interval) {
        countdown.cancel();
        countdown = new DailyCountdown(Time.fromLong(remaining), interval, timer);
        if (runningState == RunningState.Running)
            countdown.start();
    }

    public void initializeViews() {
        if (countdown != null)
            countdown.cancel();
        countdown = new DailyCountdown(defaultTime, 100, timer);
        setTimerText(defaultTime);
        runningState = RunningState.Default;
        setStartButton(true);
        setPauseButton(false);
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

        if (id == R.id.nav_camera) {
            showDialog();
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}


