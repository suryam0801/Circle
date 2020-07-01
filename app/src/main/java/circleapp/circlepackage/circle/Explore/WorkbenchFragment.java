package circleapp.circlepackage.circle.Explore;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.CreateCircle.CreateCircleCategoryPicker;
import circleapp.circlepackage.circle.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkbenchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkbenchFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private List<Circle> workbenchCircleList = new ArrayList<>();
    private FirebaseDatabase database;
    private FirebaseAuth currentUser;
    private static DatabaseReference circlesDB, userDB;
    private static User user;
    private LinearLayout emptyDisplay;
    private RecyclerView.Adapter wbadapter;

    public WorkbenchFragment() {
        // Required empty public constructor
    }

    public static WorkbenchFragment newInstance(String param1, String param2) {
        WorkbenchFragment fragment = new WorkbenchFragment();
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
        View view = inflater.inflate(R.layout.fragment_workbench, container, false);

        database = FirebaseDatabase.getInstance();
        circlesDB = database.getReference("Circles");
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        circlesDB.keepSynced(true); //synchronizes and stores local post_icon of data
        currentUser = FirebaseAuth.getInstance();
        user = SessionStorage.getUser(getActivity());

        emptyDisplay = view.findViewById(R.id.workbench_empty_display);
        ImageButton explore = view.findViewById(R.id.placeholder_explore_circle_layout);
        ImageButton create = view.findViewById(R.id.placeholder_create_circle_layout);

        //initialize  workbench recylcerview
        RecyclerView wbrecyclerView = view.findViewById(R.id.wbRecyclerView);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);
        //initializing the WorkbenchDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual circles in the recycler view
        wbadapter = new WorkbenchDisplayAdapter(workbenchCircleList, getActivity());
        wbrecyclerView.setAdapter(wbadapter);

        //remove placeholder
        if (user.getActiveCircles() == 0 && user.getCreatedCircles() == 0) {
            emptyDisplay.setVisibility(View.VISIBLE);
        }

        create.setOnClickListener(view12 -> {
            startActivity(new Intent(getActivity(), CreateCircleCategoryPicker.class));
            getActivity().finish();
        });

        explore.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new ExploreFragment()).commit();
        });

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsWorkbenchCircleLiveData(user.getUserId());

        liveData.observe(this, dataSnapshot -> {
            if (dataSnapshot != null) ;
            setWorkbenchTabs(dataSnapshot);
        });

        return view;
    }

    private void setWorkbenchTabs(DataSnapshot dataSnapshot) {
        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
            Circle circle = snapshot.getValue(Circle.class);

            //if circle is already in the list
            boolean exists = HelperMethods.listContainsCircle(workbenchCircleList, circle);
            if (exists) {
                int index = HelperMethods.returnIndexOfCircleList(workbenchCircleList, circle);
                workbenchCircleList.remove(index);
                wbadapter.notifyItemRemoved(index);
                workbenchCircleList.add(index, circle);
                wbadapter.notifyItemInserted(index);
            } else {
                workbenchCircleList.add(circle);
                wbadapter.notifyDataSetChanged();
                initializeNewCount(circle);

            }
        }
    }

    public static void initializeNewCount(Circle c) {
        if (user.getNotificationsAlert() != null && !user.getNotificationsAlert().containsKey(c.getId())) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            userDB.child("notificationsAlert").child(c.getId()).setValue(0);
        } else if (user.getNotificationsAlert() == null) {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(c.getId(), 0);
            user.setNotificationsAlert(newNotifs);
            userDB.child("notificationsAlert").child(c.getId()).setValue(0);
        }
    }
}
