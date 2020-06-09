package circleapp.circlepackage.circle.Explore;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.EditProfile.EditProfile;
import circleapp.circlepackage.circle.Notification.NotificationActivity;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.RecyclerItemClickListener;
import circleapp.circlepackage.circle.SessionStorage;

import static androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL;

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
    private DatabaseReference circlesDB;
    private List<Circle> allCircles = new ArrayList<>();
    private User user;
    private FloatingActionButton btnAddCircle;
    private LinearLayout emptyDisplay;

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
        circlesDB.keepSynced(true); //synchronizes and stores local copy of data
        currentUser = FirebaseAuth.getInstance();
        user = SessionStorage.getUser(getActivity());
        btnAddCircle = view.findViewById(R.id.add_circle_button);
        emptyDisplay = view.findViewById(R.id.workbench_empty_display);

        setWorkbenchTabs(view);

        btnAddCircle.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CreateCircle.class));
        });

        return view;
    }

    private void setWorkbenchTabs(View view) {
        //initialize  workbench recylcerview
        RecyclerView wbrecyclerView = view.findViewById(R.id.wbRecyclerView);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(getContext(),  RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);
        //initializing the WorkbenchDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual circles in the recycler view
        final RecyclerView.Adapter wbadapter = new WorkbenchDisplayAdapter(workbenchCircleList, getActivity());
        wbrecyclerView.setAdapter(wbadapter);


        //single value listener for Circles Collection
        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call

        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                allCircles.add(circle);
                //checking if user is a member of the circle
                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                //checking for duplicate
                boolean duplicate = false;
                for (Circle c : workbenchCircleList) {
                    if (c.getId().equals(circle.getId())) {
                        duplicate = true;
                    }
                }

                //setting the adapter initially
                //filter for only circles associated with creator id
                if ((circle.getCreatorID().equals(currentUser.getUid()) || existingMember == true) && duplicate == false) {
                    workbenchCircleList.add(circle);
                    //notify the adapter each time a new item needs to be added to the recycler view
                    wbadapter.notifyDataSetChanged();
                    emptyDisplay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Circle circle = dataSnapshot.getValue(Circle.class);

                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    List<Circle> tempCircleList = new ArrayList<>(workbenchCircleList);
                    for (Circle c : tempCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            workbenchCircleList.remove(position);
                            workbenchCircleList.add(position, circle);
                            wbadapter.notifyDataSetChanged();
                        }
                        ++position;
                    }
                }

                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                //checking for duplicate
                boolean duplicate = false;
                for (Circle c : workbenchCircleList) {
                    if (c.getId().equals(circle.getId())) {
                        duplicate = true;
                    }
                }

                if (existingMember == true && duplicate == false) {
                    workbenchCircleList.add(circle);
                    wbadapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    for (Circle c : workbenchCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            workbenchCircleList.remove(position);
                            wbadapter.notifyDataSetChanged();
                            break;
                        }
                        ++position;
                    }
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

}
