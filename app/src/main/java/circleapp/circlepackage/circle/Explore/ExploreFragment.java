package circleapp.circlepackage.circle.Explore;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

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
    private List<Circle> allCircles = new ArrayList<>();
    private FirebaseAuth currentUser;

    private FirebaseDatabase database;
    private DatabaseReference circlesDB;

    private User user;
    private List<String> userTempinterestTagsList;
    RecyclerView exploreRecyclerView;
    private LinearLayout emptyExploreDisplay;
    private boolean adminCircleExists = false;
    private TextView locationDisplay;


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
        circlesDB.keepSynced(true); //synchronizes and stores local copy of data
        locationDisplay = view.findViewById(R.id.explore_district_name_display);
        emptyExploreDisplay = view.findViewById(R.id.explore_empty_display);

        locationDisplay.setText(user.getDistrict());
        //retrieve interest tags from user

        if(user.getInterestTags()==null){
           userTempinterestTagsList.add("null");
        }
        else{
            userTempinterestTagsList = new ArrayList<>(user.getInterestTags().keySet());
        }

        setCircleTabs(view);
        return view;
    }

    private void setCircleTabs(View view) {
        //initialize recylcerview
        exploreRecyclerView = view.findViewById(R.id.exploreRecyclerView);
        exploreRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        exploreRecyclerView.setLayoutManager(layoutManager);
        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new CircleDisplayAdapter(getContext(), exploreCircleList, user);

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                exploreRecyclerView.setAdapter(adapter);
                Circle circle = dataSnapshot.getValue(Circle.class);
                allCircles.add(circle);

                List<String> circleIteratorinterestTagsList = new ArrayList<>(circle.getInterestTags().keySet());

                //checking if circle already exists
                boolean contains = false;
                for (Circle c : exploreCircleList) {
                    if (c.getId().equals(circle.getId()))
                        contains = true;
                }

                //checking if user is a member of the circle
                boolean existingMember = false;
                if (circle.getMembersList() != null) {
                    if (circle.getMembersList().keySet().contains(currentUser.getUid()))
                        existingMember = true;
                }

                boolean similarTags = false;
                for(String tag : userTempinterestTagsList){
                    if(circleIteratorinterestTagsList.contains(tag))
                        similarTags = true;
                }

                boolean nullTagCircle = false;
                if(circle.getInterestTags().containsKey("null"))
                    nullTagCircle = true;

                if (nullTagCircle == false && contains == false && existingMember == false && !circle.getCreatorID().equals(currentUser.getUid())) {
                    Log.d("EXPLORE FRAGMENT", existingMember + " " + circle.getCreatorName().equals("The Circle Team"));
                    if (circle.getCreatorName().equals("The Circle Team") && existingMember == false) { //add default admin entry tag
                        exploreCircleList.add(0, circle);
                        adminCircleExists = true;
                    } else if(circle.getCircleDistrict().equalsIgnoreCase(user.getDistrict())) {
                        if(similarTags == true && adminCircleExists == true)
                            exploreCircleList.add(1, circle);
                        else if (similarTags == true && adminCircleExists == false)
                            exploreCircleList.add(0, circle);
                        else
                            exploreCircleList.add(circle);
                    }
                    adapter.notifyDataSetChanged();
                    emptyExploreDisplay.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                exploreRecyclerView.setAdapter(adapter);
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict() != null && circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    List<Circle> tempCircleList = new ArrayList<>(exploreCircleList);
                    for (Circle c : tempCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            if (circle.getMembersList() != null && circle.getMembersList().containsKey(user.getUserId())) {
                                exploreCircleList.remove(position);
                                adapter.notifyItemRemoved(position);
                            } else {
                                exploreCircleList.remove(position);
                                exploreCircleList.add(position, circle);
                                adapter.notifyDataSetChanged();
                            }

                        }
                        ++position;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle.getCircleDistrict().equals(user.getDistrict())) {
                    int position = 0;
                    for (Circle c : exploreCircleList) {
                        if (c.getId().equals(circle.getId())) {
                            exploreCircleList.remove(position);
                            adapter.notifyDataSetChanged();
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
