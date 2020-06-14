package circleapp.circlepackage.circle.PersonelDisplay;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApplicantDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicantDisplayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  FirebaseAuth firebaseAuth;

    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB;
//    private FirebaseAuth firebaseAuth;
    private List<Subscriber> applicantsList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String TAG = "APPLICANTS_DISPLAY_FRAGMENT";

    public ApplicantDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ApplicantDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApplicantDisplayFragment newInstance(String param1, String param2) {
        ApplicantDisplayFragment fragment = new ApplicantDisplayFragment();
        Circle circle=new Circle();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
//        if (firebaseAuth.getCurrentUser().getUid().equalsIgnoreCase(circle.getCreatorID()))
        {

        }
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
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_all_applicants, container, false);
        Circle circle = SessionStorage.getCircle(getActivity());

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel").child(circle.getId());//circle.getId()

        RecyclerView recyclerView = view.findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        applicantsList = new ArrayList<>(); //initialize membersList


        final RecyclerView.Adapter adapter = new ApplicantListAdapter(getContext(), applicantsList, circle);
        if (firebaseAuth.getCurrentUser().getUid().equalsIgnoreCase(circle.getCreatorID()))
        {
            recyclerView.setAdapter(adapter);
        }
        circlesPersonelDB.child("applicants").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "CHILD ADDED");
                    Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                    applicantsList.add(subscriber);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    recyclerView.setAdapter(adapter);
                    Log.d(TAG, "CHILD REMOVED");
                    Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                    int position = 0;
                    List<Subscriber> tempList = new ArrayList<>(applicantsList);
                    //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                    for (Subscriber sub : tempList) {
                        if (sub.getId().equals(subscriber.getId())) {
                            applicantsList.remove(position);
                            adapter.notifyItemRemoved(position);
                            break;
                        }
                        position = position+1;
                    }
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        return view;
    }
}
