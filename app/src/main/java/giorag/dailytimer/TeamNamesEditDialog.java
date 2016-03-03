package giorag.dailytimer;

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

/**
 * Created by GioraPC on 01/03/2016.
 */
public class TeamNamesEditDialog extends DialogFragment {

    public static final String STATE = "state";
    private EditText personName;
    private RecyclerView namesList;
    private ImageButton addName;
    private TeamNamesAdapter adapter;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBundle(STATE, adapter.getInstanceState());
    }

    public TeamNamesEditDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_names, container);
        personName = (EditText) view.findViewById(R.id.team_names_new_name);
        namesList = (RecyclerView) view.findViewById(R.id.team_names_list);
        namesList.setLayoutManager(new LinearLayoutManager(getContext()));
        addName = (ImageButton) view.findViewById(R.id.team_names_add_name);

        getDialog().setTitle("Update team");

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE))
            adapter = new TeamNamesAdapter(savedInstanceState.getBundle(STATE));
        else
            adapter = new TeamNamesAdapter(null);

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

        private final String PEOPLE_EXTRA = "PEOPLE";
        private final String AVAILABILITIES_EXTRA = "AVAILABILITIES";

        private ArrayList<String> people;
        private ArrayList<Boolean> availabilities;

        public Bundle getInstanceState() {
            Bundle state = new Bundle();
            state.putStringArrayList(PEOPLE_EXTRA, people);
            boolean[] availabilitiesArray = new boolean[availabilities.size()];
            for (int i = 0; i < availabilitiesArray.length; ++i) {
                availabilitiesArray[i] = availabilities.get(i);
            }
            state.putBooleanArray(AVAILABILITIES_EXTRA, availabilitiesArray);

            return state;
        }

        private void initializeFromPreviousState(Bundle bundle) {
            if (bundle == null || !bundle.containsKey(PEOPLE_EXTRA) || !bundle.containsKey(AVAILABILITIES_EXTRA)) {
                people = new ArrayList<>();
                availabilities = new ArrayList<>();
                return;
            }

            people = bundle.getStringArrayList(PEOPLE_EXTRA);
            availabilities = new ArrayList<>();
            boolean[] availabilitiesArray = bundle.getBooleanArray(AVAILABILITIES_EXTRA);
            for(int i = 0; i < availabilitiesArray.length; ++i) {
                availabilities.add(availabilitiesArray[i]);
            }
        }

        public TeamNamesAdapter(Bundle bundle) {
            initializeFromPreviousState(bundle);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.team_names_list_item, null);

            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.name.setText(people.get(position));
            holder.name.setChecked(availabilities.get(position));
        }

        @Override
        public int getItemCount() {
            return people.size();
        }

        public void addName(String name) {
            if(people.contains(name) || name.isEmpty())
                return;

            people.add(name);
            availabilities.add(true);
            notifyDataSetChanged();
        }

        @Override
        public void onItemDismiss(int position) {
            people.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(people, i, i + 1);
                    Collections.swap(availabilities, i, i + 1);
                }
            }
            else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(people, i, i - 1);
                    Collections.swap(availabilities, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof CheckedTextView) {
                CheckedTextView ctv =(CheckedTextView) v;
                ctv.toggle();
                int index = people.indexOf(ctv.getText());
                availabilities.set(index, ctv.isChecked());
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