package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Feedback;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class FeedbackFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ListView feedbacksListView;
    private List<Feedback> feedbacksList = new ArrayList<>();
    private FeedbackAdapter feedbackAdapter;
    private EditText feedbackEditText;
    private Button feedbackSend;
    private FirebaseDatabase database;
    private DatabaseReference feedbackDb, circlesDB, broadcastDB, userDB;
    private ImageButton back;
    private User user;
    private FirebaseAuth currentUser;

    private LinearLayout emptyHolder;

    public FeedbackFragment(){}

    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
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
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        database = FirebaseDatabase.getInstance();
        feedbackDb = database.getReference("UserFeedback");
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        feedbacksListView = view.findViewById(R.id.feedback_listView);
        feedbackEditText = view.findViewById(R.id.feedback_type_editText);
        feedbackSend = view.findViewById(R.id.feedback_send_button);
        emptyHolder =view.findViewById(R.id.feedbackWall_empty_display);
        emptyHolder.setVisibility(View.VISIBLE);
        feedbacksList = new ArrayList<>();

        feedbackAdapter= new FeedbackAdapter(getContext(), feedbacksList);
        feedbacksListView.setAdapter(feedbackAdapter);

        feedbackEditText.clearFocus();

        loadFeedback();
        feedbackSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!feedbackEditText.getText().toString().trim().equals(""))
                    makeFeedbackEntry();
                feedbackEditText.setText("");

            }
        });
        return view;
    }
    private void loadFeedback() {

//        feedbackDb.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d("Feedback",dataSnapshot.toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        feedbackDb.child(SessionStorage.getUser((Activity) getContext()).getDistrict())
//                .child(SessionStorage.getUser(FeedbackActivity.this).getUserId())
                .orderByChild("timestamp")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Feedback tempComment = dataSnapshot.getValue(Feedback.class);
                        feedbacksList.add(tempComment); //to store timestamp values descendingly
                        feedbackAdapter.notifyDataSetChanged();
                        feedbacksListView.setSelection(feedbacksListView.getAdapter().getCount()-1);
                        emptyHolder.setVisibility(View.GONE);

                        //call view activity only after all comments have been populated
//                if(commentsList.size() == broadcast.getNumberOfComments())
//                    updateUserFields("view");
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void makeFeedbackEntry() {
        long currentCommentTimeStamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", currentCommentTimeStamp);
        map.put("feedback", feedbackEditText.getText().toString().trim());
        map.put("userId", SessionStorage.getUser((Activity) getContext()).getUserId());
        map.put("userName", SessionStorage.getUser((Activity) getContext()).getName().trim());

        feedbackDb.child(SessionStorage.getUser((Activity) getContext()).getDistrict())
//                .child(SessionStorage.getUser(FeedbackActivity.this).getUserId())
                .push()
                .setValue(map);
    }



}
