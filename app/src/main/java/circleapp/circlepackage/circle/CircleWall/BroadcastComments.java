package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

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

import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class BroadcastComments extends AppCompatActivity {

    private ListView commentsListView;
    private List<Comment> commentsList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private EditText commentEditText;
    private Button commentSend;
    private Circle circle;
    private FirebaseDatabase database;
    private DatabaseReference broadcastCommentsDB, circlesDB, broadcastDB, userDB;
    private ImageButton back;
    private Broadcast broadcast;
    private User user;
    private LinearLayout emptyHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_comments);

        circle = SessionStorage.getCircle(BroadcastComments.this);
        broadcast = SessionStorage.getBroadcast(BroadcastComments.this);
        user = SessionStorage.getUser(BroadcastComments.this);

        database = FirebaseDatabase.getInstance();
        broadcastCommentsDB = database.getReference("BroadcastComments");
        circlesDB = database.getReference("Circles").child(circle.getId());
        broadcastDB = database.getReference("Broadcasts").child(circle.getId()).child(broadcast.getId());
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userDB.child("newTimeStampsComments").setValue(user.getNewTimeStampsComments());
        userDB.child("noOfReadDiscussions").setValue(user.getNoOfReadDiscussions());

        commentsListView = findViewById(R.id.comments_listView);
        commentEditText = findViewById(R.id.comment_type_editText);
        commentSend = findViewById(R.id.comment_send_button);
        back = findViewById(R.id.bck_broadcastComments);
        commentsList = new ArrayList<>();
        emptyHolder = findViewById(R.id.commentWall_empty_display);
        emptyHolder.setVisibility(View.VISIBLE);

        commentAdapter= new CommentAdapter(BroadcastComments.this, commentsList);
        commentsListView.setAdapter(commentAdapter);

        commentEditText.clearFocus();

        loadComments();
//        BroadcastComments.setSoftInputMode(SOFT_INPUT_ADJUST_PAN);
        commentSend.setOnClickListener(view -> {
            if (!commentEditText.getText().toString().trim().equals(""))
                makeCommentEntry();
            commentEditText.setText("");
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(BroadcastComments.this, CircleWall.class));
            finish();
        });
    }

    public void loadComments() {
        broadcastCommentsDB.child(circle.getId()).child(broadcast.getId()).orderByChild("timestamp").limitToLast(13).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment tempComment = dataSnapshot.getValue(Comment.class);
                commentsList.add(tempComment); //to store timestamp values descendingly
                commentAdapter.notifyDataSetChanged();
                commentsListView.setSelection(commentsListView.getAdapter().getCount()-1);
                emptyHolder.setVisibility(View.GONE);
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

    public void makeCommentEntry() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.put("comment", commentEditText.getText().toString().trim());
        map.put("commentorId", SessionStorage.getUser(BroadcastComments.this).getUserId());
        map.put("commentorPicURL", SessionStorage.getUser(BroadcastComments.this).getProfileImageLink());
        map.put("commentorName", SessionStorage.getUser(BroadcastComments.this).getFirstName().trim() + " " +
                SessionStorage.getUser(BroadcastComments.this).getLastName().trim());

        broadcastCommentsDB.child(circle.getId()).child(broadcast.getId()).push().setValue(map);

        int noOfNewDiscussions = circle.getNoOfNewDiscussions() + 1;
        int noOfNewBroadcasts = broadcast.getNumberOfComments() + 1;
        circlesDB.child("noOfNewDiscussions").setValue(noOfNewDiscussions);
        broadcastDB.child("latestCommentTimestamp").setValue(System.currentTimeMillis());
        broadcastDB.child("numberOfComments").setValue(noOfNewBroadcasts);

        circle.setNoOfNewDiscussions(noOfNewDiscussions);
        SessionStorage.saveCircle(BroadcastComments.this, circle);
        broadcast.setLatestCommentTimestamp(System.currentTimeMillis());
        broadcast.setNumberOfComments(noOfNewBroadcasts);
        SessionStorage.saveBroadcast(BroadcastComments.this, broadcast);

    }
}