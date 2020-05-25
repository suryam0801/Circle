package circleapp.circlepackage.circle.PersonelDisplay;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.BroadcastListAdapter;
import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllMembersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllMembersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String TAG = "ALL MEMBERS FRAGMENT";

    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB;
    private List<Subscriber> memberList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllMembersFragment() {
        // Required empty public constructor
    }

    public static AllMembersFragment newInstance(String param1, String param2) {
        AllMembersFragment fragment = new AllMembersFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_members, container, false);

        Circle circle = SessionStorage.getCircle(getActivity());

        database = FirebaseDatabase.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel").child("5ecaa3af69fae6d313e2f740"); //circle.getId()

        RecyclerView recyclerView = view.findViewById(R.id.allmembers_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberList = new ArrayList<>(); //initialize membersList
        final RecyclerView.Adapter adapter = new MemberListAdapter(getContext(), memberList);
        recyclerView.setAdapter(adapter);

        circlesPersonelDB.child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "CHILD ADDED");
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                memberList.add(subscriber);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "CHILD CHANGED");
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                int position = 0;
                List<Subscriber> tempList = new ArrayList<>(memberList);
                //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                for (Subscriber sub : tempList) {
                    if (sub.getId().equals(subscriber.getId())) {
                        memberList.set(position, subscriber);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "CHILD REMOVED");
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                int position = 0;
                List<Subscriber> tempList = new ArrayList<>(memberList);
                //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                for (Subscriber sub : tempList) {
                    if (sub.getId().equals(subscriber.getId())) {
                        memberList.remove(position);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        return view;
    }


}
