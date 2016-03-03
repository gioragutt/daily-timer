package giorag.dailytimer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;

import giorag.dailytimer.modals.Person;

public class TeamNamesEditDialog extends DialogFragment {

    public static final String PEOPLE = "people";
    private EditText personName;
    private RecyclerView namesList;
    private ImageButton addName;
    private TeamNamesAdapter adapter;
    private TinyDB db;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savePeopleList();
    }

    public TeamNamesEditDialog() {
    }

    private void updateParent() {
        TeamNamesEditDialogListener listener = (TeamNamesEditDialogListener)getActivity();
        listener.onPersonsListUpdate(adapter.people);
    }

    private void savePeopleList() {
        db.putListObject(PEOPLE, new ArrayList<Object>(adapter.people));
    }

    private void update() {
        savePeopleList();
        updateParent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_names, container);
        personName = (EditText) view.findViewById(R.id.team_names_new_name);
        namesList = (RecyclerView) view.findViewById(R.id.team_names_list);
        namesList.setLayoutManager(new LinearLayoutManager(getContext()));
        addName = (ImageButton) view.findViewById(R.id.team_names_add_name);

        db = new TinyDB(getActivity());

        getDialog().setTitle("Update team");

        adapter = new TeamNamesAdapter(db);
        namesList.setAdapter(adapter);
        namesList.addItemDecoration(new DividerItemDecoration(getActivity(), null));
        addName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addName(personName.getText().toString());
                personName.setText("");
            }
        });

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(namesList);

        return view;
    }

    class TeamNamesAdapter extends RecyclerView.Adapter<TeamNamesAdapter.ViewHolder>
            implements ItemTouchHelperAdapter, View.OnClickListener {

        private ArrayList<Person> people;

        public TeamNamesAdapter(TinyDB db) {
            people = new ArrayList<>();
            try {
                ArrayList<Object> objects = db.getListObject(PEOPLE, Person.class);
                for (Object obj : objects) {
                    people.add((Person) obj);
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
            update();
        }

        @Override
        public void onItemDismiss(int position) {
            people.remove(position);
            notifyItemRemoved(position);
            update();
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
                    update();
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