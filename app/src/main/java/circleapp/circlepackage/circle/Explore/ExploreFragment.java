package circleapp.circlepackage.circle.Explore;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.FirebaseRetrievalViewModel;
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

    private ChipGroup filterDisplay;
    private RecyclerView.Adapter adapter;

    private User user;
    RecyclerView exploreRecyclerView;

    private List<String> listOfFilters = new ArrayList<>();

    private TextView filter;
    private DataSnapshot dbSnapShot;
    private int setIndex = 0;

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

        user = SessionStorage.getUser(getActivity());

        listOfFilters = SessionStorage.getFilters(getActivity());

        filter = view.findViewById(R.id.explore_filter_btn);
        filterDisplay = view.findViewById(R.id.filter_display_chip_group);
        exploreRecyclerView = view.findViewById(R.id.exploreRecyclerView);

        setIndex = getActivity().getIntent().getIntExtra("exploreIndex", 0);

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
            for (String f : listOfFilters)
                setFilterChips(f);
        }

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsExploreCircleLiveData(user.getDistrict());

        liveData.observe(this, dataSnapshot -> {
            if (dataSnapshot != null)
                dbSnapShot = dataSnapshot;
            setCircleTabs(dataSnapshot);
        });
        return view;
    }

    private void setCircleTabs(DataSnapshot dataSnapshot) {

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            Circle circle = snapshot.getValue(Circle.class);

            //if circle is already in the list
            int index = 0;
            boolean exists = HelperMethods.listContainsCircle(exploreCircleList, circle);
            if (exists) {
                index = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                exploreCircleList.remove(index);
                exploreCircleList.add(index, circle);
                adapter.notifyItemChanged(index);

            } else {
                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());

                if (!isMember && circle.getVisibility().equals("Everybody")) {

                    if (circle.getCreatorName().equals("The Circle Team")) {
                        exploreCircleList.add(0, circle);
                        adapter.notifyItemInserted(0);
                    }

                    boolean circleMatchesFilter = HelperMethods.circleFitsWithinFilterContraints(listOfFilters, circle);
                    if (listOfFilters == null || listOfFilters.isEmpty()) {
                        exploreCircleList.add(index, circle);
                        adapter.notifyItemInserted(index);
                        exploreRecyclerView.scrollToPosition(index);
                    } else if (circleMatchesFilter) {
                        exploreCircleList.add(index, circle);
                        adapter.notifyItemInserted(index);
                        exploreRecyclerView.scrollToPosition(index);
                    }
                    exploreRecyclerView.scrollToPosition(setIndex);
                }
            }
        }
        checkForRemovedCircles(dataSnapshot);

    }

    public void checkForRemovedCircles(DataSnapshot dataSnapshot) {
        List<String> tempWBCirclesIDList = new ArrayList<>();
        for (Circle c : exploreCircleList)
            tempWBCirclesIDList.add(c.getId());

        List<String> tempSnapshotCirclesIDList = new ArrayList<>();
        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
            Circle circle = snapshot.getValue(Circle.class);
            tempSnapshotCirclesIDList.add(circle.getId());
        }

        int missingIndex = -1;
        for(String s : tempWBCirclesIDList){
            if(!tempSnapshotCirclesIDList.contains(s))
                missingIndex = tempWBCirclesIDList.indexOf(s);
        }

        if(missingIndex != -1) {
            exploreCircleList.remove(missingIndex);
            adapter.notifyItemRemoved(missingIndex);
        }
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
            SessionStorage.saveFilters(getActivity(), listOfFilters);
            filterDisplay.removeView(chip);
            exploreCircleList.clear();
            adapter.notifyDataSetChanged();
            setCircleTabs(dbSnapShot);
        });

        filterDisplay.addView(chip);
    }

    @Override
    public void onDestroy() {
        SessionStorage.tempIndexStore(getActivity(), 0);
        super.onDestroy();
    }
}
