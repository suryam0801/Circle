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

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
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

        LiveData<String[]> liveData = viewModel.getDataSnapsExploreCircleLiveData(user.getDistrict());

        liveData.observe(this, returnArray -> {
            Circle circle = new Gson().fromJson(returnArray[0], Circle.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    addCircle(circle);
                    break;
                case "changed":
                    changeCircle(circle);
                    break;
                case "removed":
                    removeCircle(circle);
                    break;
            }
        });
        return view;
    }

    public void addCircle(Circle circle) {
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
            //exploreRecyclerView.scrollToPosition(index);
        }
    }

    public void changeCircle(Circle circle) {
        //edit existing circle
        int index = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
        exploreCircleList.remove(index);
        exploreCircleList.add(index, circle);
        adapter.notifyItemChanged(index);
    }

    public void removeCircle(Circle circle) {
        int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
        if (position != -1) {
            exploreCircleList.remove(position);
            adapter.notifyItemRemoved(position);
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
            //setCircleTabs(dbSnapShot);
        });

        filterDisplay.addView(chip);
    }

    @Override
    public void onDestroy() {
        SessionStorage.tempIndexStore(getActivity(), 0);
        super.onDestroy();
    }
}
