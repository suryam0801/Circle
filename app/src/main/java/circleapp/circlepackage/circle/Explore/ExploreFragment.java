package circleapp.circlepackage.circle.Explore;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Circle> exploreCircleList = new ArrayList<>();
    private FirebaseAuth currentUser;

    private FirebaseDatabase database;
    private DatabaseReference circlesDB;

    private ChipGroup filterDisplay;
    private RecyclerView.Adapter adapter;

    private User user;
    RecyclerView exploreRecyclerView;

    private List<String> listOfFilters = new ArrayList<>();

    private TextView filter;


    public ExploreFragment() {
        // Required empty public constructor
    }


    public static ExploreFragment newInstance(String param1, String param2) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        currentUser = FirebaseAuth.getInstance();
        user = SessionStorage.getUser(getActivity());
        database = FirebaseDatabase.getInstance();
        circlesDB = database.getReference("Circles");
        circlesDB.keepSynced(true); //synchronizes and stores local post_icon of data

        listOfFilters = SessionStorage.getFilters(getActivity());

        filter = view.findViewById(R.id.explore_filter_btn);
        filterDisplay = view.findViewById(R.id.filter_display_chip_group);
        exploreRecyclerView = view.findViewById(R.id.exploreRecyclerView);

        //initialize recylcerview
        exploreRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        exploreRecyclerView.setLayoutManager(layoutManager);

        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        adapter = new CircleDisplayAdapter(getContext(), exploreCircleList, user);
        exploreRecyclerView.setAdapter(adapter);


        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(exploreRecyclerView);

        filter.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), ExploreCategoryFilter.class);
            SessionStorage.saveFilters(getActivity(), listOfFilters);
            startActivity(intent);
        });

        if (listOfFilters != null) {
            for(String f : listOfFilters)
                setFilterChips(f);
        }

        setCircleTabs();
        return view;
    }

    private void setCircleTabs() {

        int index = SessionStorage.getTempIndexStore(getActivity());

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());
                boolean isInLocation = circle.getCircleDistrict().trim().equalsIgnoreCase(user.getDistrict().trim());

                if (!isMember && circle.getVisibility().equals("Everybody")) {

                    if (circle.getCreatorName().equals("The Circle Team")) {
                        exploreCircleList.add(0, circle);
                        adapter.notifyItemInserted(0);
                    }

                    if (isInLocation) {
                        boolean circleMatchesFilter = HelperMethods.circleFitsWithinFilterContraints(listOfFilters, circle);
                        if (listOfFilters == null || listOfFilters.isEmpty()) {
                            exploreCircleList.add(adapter.getItemCount(), circle);
                            adapter.notifyItemInserted(adapter.getItemCount());
                        } else if (circleMatchesFilter) {
                            exploreCircleList.add(adapter.getItemCount(), circle);
                            adapter.notifyItemInserted(adapter.getItemCount());
                        }
                    }
                    exploreRecyclerView.scrollToPosition(index);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());
                boolean containsCircle = HelperMethods.listContainsCircle(exploreCircleList, circle);

                if (containsCircle) {
                    if (isMember) {
                        exploreCircleList.remove(position);
                        adapter.notifyItemRemoved(position);
                    } else {
                        exploreCircleList.set(position, circle);
                        adapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                if (position != -1) {
                    exploreCircleList.remove(position);
                    adapter.notifyItemRemoved(position);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setFilterChips(final String name) {
        final Chip chip = new Chip(getContext());

        chip.setCloseIcon(getResources().getDrawable(R.drawable.ic_clear_blue_24dp));
        chip.setCloseIconVisible(true);
        chip.setText(name);
        chip.setChipStrokeColorResource(R.color.color_blue);
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setChipStrokeWidth(5f);
        chip.setChipMinHeight(100f);
        chip.setTextColor(getResources().getColor(R.color.color_blue));

        chip.setOnCloseIconClickListener(view -> {
            listOfFilters.remove(chip.getText());
            filterDisplay.removeView(chip);
            exploreCircleList.clear();
            adapter.notifyDataSetChanged();
            setCircleTabs();
        });

        filterDisplay.addView(chip);
    }

    @Override
    public void onDestroy() {
        SessionStorage.tempIndexStore(getActivity(), 0);
        super.onDestroy();
    }
}
