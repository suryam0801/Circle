package circleapp.circlepackage.circle.Explore;

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

    private User user;
    RecyclerView exploreRecyclerView;
    private LinearLayout emptyExploreDisplay;


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
        emptyExploreDisplay = view.findViewById(R.id.explore_empty_display);

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
        exploreRecyclerView.setAdapter(adapter);

        //loads all the data for offline use the very first time the user loads the app
        //only reloads new data objects or modifications to existing objects on each call
        circlesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                emptyExploreDisplay.setVisibility(View.GONE);

                Circle circle = dataSnapshot.getValue(Circle.class);

                boolean isMember = HelperMethods.isMemberOfCircle(circle, user.getUserId());
                boolean isInLocation = circle.getCircleDistrict().trim().equalsIgnoreCase(user.getDistrict().trim());

                if (!isMember) {

                    if (circle.getCreatorName().equals("The Circle Team")) {
                        exploreCircleList.add(0, circle);
                        adapter.notifyItemInserted(0);
                    }

                    if (isInLocation) {
                        exploreCircleList.add(adapter.getItemCount(), circle);
                        adapter.notifyItemInserted(adapter.getItemCount());
                    }
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
                        exploreCircleList.remove(position);
                        exploreCircleList.add(position, circle);
                        adapter.notifyItemChanged(position);
                    }

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                int position = HelperMethods.returnIndexOfCircleList(exploreCircleList, circle);
                if(!exploreCircleList.isEmpty()){
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

}
