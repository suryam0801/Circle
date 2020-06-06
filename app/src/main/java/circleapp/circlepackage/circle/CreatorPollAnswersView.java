package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.CircleWall.PollAnswerDisplayAdapter;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.MemberListAdapter;

public class CreatorPollAnswersView extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB;
    private HashMap<Subscriber, String> list = new HashMap<>();
    private String TAG = CreatorPollAnswersView.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creator_poll_answers_view);

        Circle circle = SessionStorage.getCircle(CreatorPollAnswersView.this);
        Broadcast broadcast = SessionStorage.getBroadcast(CreatorPollAnswersView.this);

        Poll poll = broadcast.getPoll();
        HashMap<String, String> userResponse = poll.getUserResponse();

        User user = SessionStorage.getUser(CreatorPollAnswersView.this);

        database = FirebaseDatabase.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel").child(circle.getId()); //circle.getId()

        RecyclerView recyclerView = findViewById(R.id.poll_answers_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(CreatorPollAnswersView.this));
        final RecyclerView.Adapter adapter = new PollAnswerDisplayAdapter(CreatorPollAnswersView.this, list);
        recyclerView.setAdapter(adapter);

        circlesPersonelDB.child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Subscriber member = dataSnapshot.getValue(Subscriber.class);

                for (Map.Entry<String, String> entry : userResponse.entrySet()) {
                    if(entry.getKey().equals(member.getId())){
                        list.put(member, entry.getValue());
                        adapter.notifyDataSetChanged();
                    }
                }
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
}
