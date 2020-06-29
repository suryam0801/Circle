package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

public class BroadcastComments extends AppCompatActivity {

    private RecyclerView commentsListView;
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
    private int indexOfParentBroadcast = 0;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_comments);

        indexOfParentBroadcast = getIntent().getIntExtra("indexOfBroadcast", 0);

        circle = SessionStorage.getCircle(BroadcastComments.this);
        broadcast = SessionStorage.getBroadcast(BroadcastComments.this);
        user = SessionStorage.getUser(BroadcastComments.this);

        database = FirebaseDatabase.getInstance();
        broadcastCommentsDB = database.getReference("BroadcastComments");
        circlesDB = database.getReference("Circles").child(circle.getId());
        broadcastDB = database.getReference("Broadcasts").child(circle.getId()).child(broadcast.getId());
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        commentsListView = findViewById(R.id.comments_listView);
        commentEditText = findViewById(R.id.comment_type_editText);
        commentEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE );
        commentSend = findViewById(R.id.comment_send_button);
        back = findViewById(R.id.bck_broadcastComments);
        commentsList = new ArrayList<>();
        emptyHolder = findViewById(R.id.commentWall_empty_display);
        emptyHolder.setVisibility(View.VISIBLE);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        commentsListView.setLayoutManager(mLinearLayoutManager);

        commentAdapter= new CommentAdapter(BroadcastComments.this, commentsList);
        commentsListView.setAdapter(commentAdapter);

        commentEditText.clearFocus();

        loadComments();

        commentSend.setOnClickListener(view -> {
            if (!commentEditText.getText().toString().trim().equals(""))
                makeCommentEntry();
            commentEditText.setText("");
        });

        back.setOnClickListener(view -> {
            Intent intent = new Intent(this, CircleWall.class);
            intent.putExtra("indexOfBroadcast", indexOfParentBroadcast);
            startActivity(intent);
            finish();
        });
    }

    public void loadComments() {
        broadcastCommentsDB.child(circle.getId()).child(broadcast.getId()).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment tempComment = dataSnapshot.getValue(Comment.class);
                commentsList.add(tempComment); //to store timestamp values descendingly
                commentAdapter.notifyDataSetChanged();
                commentsListView.scrollToPosition(commentsList.size()-1);

//                commentsListView.set
                emptyHolder.setVisibility(View.GONE);

                //call view activity only after all comments have been populated
                if(commentsList.size() == broadcast.getNumberOfComments())
                    updateUserFields("view");
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
        long currentCommentTimeStamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", currentCommentTimeStamp);
        map.put("comment", commentEditText.getText().toString().trim());
        map.put("commentorId", SessionStorage.getUser(BroadcastComments.this).getUserId());
        map.put("commentorPicURL", SessionStorage.getUser(BroadcastComments.this).getProfileImageLink());
        map.put("commentorName", SessionStorage.getUser(BroadcastComments.this).getName().trim());

        broadcastCommentsDB.child(circle.getId()).child(broadcast.getId()).push().setValue(map);

        updateCommentNumbersPostCreate(currentCommentTimeStamp);
        updateUserFields("create");
    }

    public void updateCommentNumbersPostCreate(long timetamp){
        //updating broadCastTimeStamp after creating the comment
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcastDB.child("latestCommentTimestamp").setValue(timetamp);
        broadcastDB.child("numberOfComments").setValue(broacastNumberOfComments);
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        SessionStorage.saveBroadcast(BroadcastComments.this, broadcast);

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circlesDB.child("noOfNewDiscussions").setValue(circleNewNumberOfDiscussions);
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        SessionStorage.saveCircle(BroadcastComments.this, circle);
    }

    public void updateUserFields(String navFrom){
        HashMap<String, Integer> tempNoOfDiscussion;
        if(user.getNoOfReadDiscussions()!=null)
            tempNoOfDiscussion = new HashMap<>(user.getNoOfReadDiscussions());
        else
            tempNoOfDiscussion = new HashMap<>();

        switch (navFrom){
            case "create":
                //updating userReadDiscussions after creating the comment
                int updateDiscussionInt;
                if(tempNoOfDiscussion.containsKey(broadcast.getId()))
                    updateDiscussionInt = tempNoOfDiscussion.get(broadcast.getId());
                else
                    updateDiscussionInt = 0;
                tempNoOfDiscussion.put(broadcast.getId(), updateDiscussionInt + 1);
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                userDB.child("noOfReadDiscussions").setValue(tempNoOfDiscussion);
                break;

            case "view":
                tempNoOfDiscussion.put(broadcast.getId(), broadcast.getNumberOfComments());
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                userDB.child("noOfReadDiscussions").setValue(tempNoOfDiscussion);
                break;
        }

        //updating user latest timestamp for that comment
        HashMap<String, Long> tempCommentTimeStamps = new HashMap<>(user.getNewTimeStampsComments());
        tempCommentTimeStamps.put(broadcast.getId(), broadcast.getLatestCommentTimestamp());
        user.setNewTimeStampsComments(tempCommentTimeStamps);
        SessionStorage.saveUser(BroadcastComments.this, user);
        userDB.child("newTimeStampsComments").child(broadcast.getId()).setValue(broadcast.getLatestCommentTimestamp());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CircleWall.class);
        intent.putExtra("indexOfBroadcast", indexOfParentBroadcast);
        startActivity(intent);
        finish();
    }
}