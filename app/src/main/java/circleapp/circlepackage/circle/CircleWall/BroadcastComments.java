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
import android.widget.ListView;

import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
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
    private DatabaseReference broadcastCommentsDB;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_comments);

        database = FirebaseDatabase.getInstance();
        broadcastCommentsDB = database.getReference("BroadcastComments");

        commentsListView = findViewById(R.id.comments_listView);
        commentEditText = findViewById(R.id.comment_type_editText);
        commentSend = findViewById(R.id.comment_send_button);
        back = findViewById(R.id.bck_broadcastComments);
        commentsList = new ArrayList<>();

        commentAdapter= new CommentAdapter(BroadcastComments.this, commentsList);
        commentsListView.setAdapter(commentAdapter);

        commentEditText.clearFocus();

        circle = SessionStorage.getCircle(BroadcastComments.this);

        loadComments();

        commentSend.setOnClickListener(view -> {
            if (!commentEditText.getText().toString().trim().equals(""))
                makeCommentEntry();
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(BroadcastComments.this, CircleWall.class));
            finish();
        });
    }

    public void loadComments() {


        broadcastCommentsDB.child(circle.getId()).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment tempComment = dataSnapshot.getValue(Comment.class);
                commentsList.add(0, tempComment); //to store timestamp values descendingly
                commentAdapter.notifyDataSetChanged();
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

        broadcastCommentsDB.child(circle.getId()).push().setValue(map);
    }
}