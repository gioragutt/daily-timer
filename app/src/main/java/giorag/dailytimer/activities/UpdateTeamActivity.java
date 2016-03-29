package giorag.dailytimer.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import giorag.dailytimer.DividerItemDecoration;
import giorag.dailytimer.R;
import giorag.dailytimer.SimpleItemTouchHelperCallback;
import giorag.dailytimer.TinyDB;
import giorag.dailytimer.interfaces.ItemTouchHelperAdapter;
import giorag.dailytimer.modals.Person;

public class UpdateTeamActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String PEOPLE = "people";
    public static final String PEOPLE_ORDER = "people_order";
    private RecyclerView namesList;
    private FloatingActionButton addName;
    private TeamNamesAdapter adapter;
    private boolean isRandomized;
    private Button undoRandomizeButton;
    private TinyDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_team);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        undoRandomizeButton = (Button) findViewById(R.id.team_names_undo_randomize);
        namesList = (RecyclerView) findViewById(R.id.team_names_list);
        namesList.setLayoutManager(new LinearLayoutManager(this));
        db = new TinyDB(this);

        addName = (FloatingActionButton) findViewById(R.id.team_names_add_name);
        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText memberName = new EditText(UpdateTeamActivity.this);
                memberName.setHint("Member name");
                AlertDialog dialog = new AlertDialog.Builder(UpdateTeamActivity.this)
                        .setTitle("Add member")
                        .setMessage("enter a name")
                        .setView(memberName)
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                adapter.addName(memberName.getText().toString());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).create();

                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();
            }
        });

        adapter = new TeamNamesAdapter(db);
        namesList.setAdapter(adapter);
        namesList.addItemDecoration(new DividerItemDecoration(this, null));

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(namesList);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void savePeopleList() {
        db.putListObject(PEOPLE, new ArrayList<Object>(adapter.people));
    }

    private void savePeopleOrderList() {
        db.putListObject(PEOPLE_ORDER, new ArrayList<Object>(adapter.people));
    }

    private void update(boolean shouldSaveOrder) {
        savePeopleList();

        if(shouldSaveOrder)
            savePeopleOrderList();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            openMainActivity();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void launchSettings(MenuItem item) {
        Intent settings = new Intent(this, SettingsActivity.class);
        startActivity(settings);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_team_list) {
            // already in update team list activity,
            // do nothing
        }
        else if (id == R.id.nav_daily_timer) {
            openMainActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void undoRandomize(View view) {
        undoRandomizeButton.setEnabled(false);
        ArrayList<Object> oldOrder = db.getListObject(PEOPLE_ORDER, Person.class);
        ArrayList<Person> oldOrderPeople = new ArrayList<>();
        for (Object person : oldOrder) {
            oldOrderPeople.add((Person)person);
        }
        adapter.people = oldOrderPeople;
        adapter.notifyDataSetChanged();
    }

    public void randomizeList(View view) {
        undoRandomizeButton.setEnabled(true);
        isRandomized = true;

        ArrayList<Person> randomizedOrderPeople = new ArrayList<>();

        for (int index : getRandomIndexList(adapter.people.size())) {
            randomizedOrderPeople.add(adapter.people.get(index));
        }

        adapter.people = randomizedOrderPeople;
        adapter.notifyDataSetChanged();
        update(false);
    }

    private int[] getRandomIndexList(int amountOfIndexes) {
        int[] indexes = new int[amountOfIndexes];
        for (int i = 0; i < amountOfIndexes; ++i)
            indexes[i] = -1;

        Random rnd = new Random(new Random().nextInt());

        for (int i = 0; i < amountOfIndexes; ++i) {
            int indexToPlace = rnd.nextInt(amountOfIndexes);
            while (indexes[indexToPlace] != -1)
                indexToPlace = rnd.nextInt(amountOfIndexes);
            indexes[indexToPlace] = i;
        }

        return indexes;
    }

    class TeamNamesAdapter extends RecyclerView.Adapter<TeamNamesAdapter.ViewHolder>
            implements ItemTouchHelperAdapter, View.OnClickListener {

        private ArrayList<Person> people;

        public TeamNamesAdapter(TinyDB db) {
            people = new ArrayList<>();
            try {
                ArrayList<Object> peopleAsObjects = db.getListObject(PEOPLE, Person.class);
                for (Object obj : peopleAsObjects) {
                    people.add((Person) obj);
                }

                if (db.getListObject(PEOPLE_ORDER, Person.class) == null) {
                    db.putListObject(PEOPLE_ORDER, peopleAsObjects);
                }
            }
            catch (NullPointerException e) {
                return;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_names_list_item, null);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Person curr = people.get(position);
            holder.name.setText(curr.name);
            holder.name.setChecked(curr.available);
        }

        @Override
        public int getItemCount() {
            return people.size();
        }

        public void addName(String name) {
            if(people.contains(name) || name.isEmpty())
                return;

            people.add(new Person(name, true));
            notifyDataSetChanged();
            update(true);
        }

        @Override
        public void onItemDismiss(int position) {
            people.remove(position);
            notifyItemRemoved(position);
            update(true);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(people, i, i + 1);
                }
            }
            else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(people, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            update(true);
            isRandomized = false;
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof CheckedTextView))
                return;

            CheckedTextView ctv = (CheckedTextView) v;
            ctv.toggle();

            String name = ctv.getText().toString();
            for (Person p : people) {
                if (p.name.equals(name)) {
                    p.available = ctv.isChecked();
                    update(true);
                    return;
                }
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckedTextView name;
            public ViewHolder(View itemView) {
                super(itemView);

                name = (CheckedTextView)itemView.findViewById(R.id.name_in_list);
                name.setOnClickListener(TeamNamesAdapter.this);
            }
        }
    }
}
