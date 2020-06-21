package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class FullPageBroadcastCardAdapter extends RecyclerView.Adapter<FullPageBroadcastCardAdapter.ViewHolder> {
    private Context mContext;
    private List<Broadcast> broadcastList;
    private Circle circle;
    private FirebaseDatabase database;
    private DatabaseReference broadcastCommentsDB, circlesDB, broadcastDB, userDB;


    public FullPageBroadcastCardAdapter(Context mContext, List<Broadcast> broadcastList, Circle circle) {
        this.mContext = mContext;
        this.broadcastList = broadcastList;
        this.circle = circle;

        database = FirebaseDatabase.getInstance();
        broadcastCommentsDB = database.getReference("BroadcastComments");
        circlesDB = database.getReference("Circles").child(circle.getId());
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    }

    @NonNull
    @Override
    public FullPageBroadcastCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_full_page_card_item, parent, false);
        return new FullPageBroadcastCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FullPageBroadcastCardAdapter.ViewHolder holder, int position) {
        ((Activity)mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        CommentAdapter commentAdapter;
        List<Comment> commentsList = new ArrayList<>();
        Broadcast currentBroadcast = broadcastList.get(position);

        commentAdapter= new CommentAdapter(mContext, commentsList);
        holder.commentListView.setAdapter(commentAdapter);

        holder.commentSend.setOnClickListener(view -> {
            if(holder.commentEditText.getText() != null){
                makeCommentEntry(currentBroadcast, holder.commentEditText.getText().toString());
                holder.commentEditText.setText("");
            } else {
                Toast.makeText(mContext, "Please type a comment first", Toast.LENGTH_SHORT).show();
            }
        });

        broadcastCommentsDB.child(circle.getId()).child(currentBroadcast.getId()).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment tempComment = dataSnapshot.getValue(Comment.class);
                commentsList.add(tempComment); //to store timestamp values descendingly
                commentAdapter.notifyDataSetChanged();
                holder.commentListView.setSelection(holder.commentListView.getAdapter().getCount()-1);

                if(commentsList.size() == currentBroadcast.getNumberOfComments())
               updateUserFields("view", commentsList, currentBroadcast);
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


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public void makeCommentEntry(Broadcast broadcast, String commentMessage) {
        long currentCommentTimeStamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", currentCommentTimeStamp);
        map.put("comment", commentMessage);
        map.put("commentorId", SessionStorage.getUser((Activity) mContext).getUserId());
        map.put("commentorPicURL", SessionStorage.getUser((Activity) mContext).getProfileImageLink());
        map.put("commentorName", SessionStorage.getUser((Activity) mContext).getName().trim());

        broadcastCommentsDB.child(circle.getId()).child(broadcast.getId()).push().setValue(map);

        updateCommentNumbersPostCreate(currentCommentTimeStamp, broadcast);
        updateUserFields("create", null, broadcast);
    }

    public void updateCommentNumbersPostCreate(long timetamp, Broadcast broadcast){
        broadcastDB = database.getReference("Broadcasts").child(circle.getId()).child(broadcast.getId());

        //updating broadCastTimeStamp after creating the comment
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcastDB.child("latestCommentTimestamp").setValue(timetamp);
        broadcastDB.child("numberOfComments").setValue(broacastNumberOfComments);
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        SessionStorage.saveBroadcast((Activity) mContext, broadcast);

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circlesDB.child("noOfNewDiscussions").setValue(circleNewNumberOfDiscussions);
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        SessionStorage.saveCircle((Activity) mContext, circle);
    }

    public void updateUserFields(String navFrom, List<Comment> commentsList, Broadcast broadcast){
        User user = SessionStorage.getUser((Activity) mContext);

        int userNumberOfReadDiscussions;
        switch (navFrom){
            case "create":
                //updating userReadDiscussions after creating the comment
                userNumberOfReadDiscussions = user.getNoOfReadDiscussions() + 1;
                user.setNoOfReadDiscussions(userNumberOfReadDiscussions);
                userDB.child("noOfReadDiscussions").setValue(userNumberOfReadDiscussions);
                break;

            case "view":
                int numberOfUnreadComments = HelperMethods.returnNoOfCommentsPostTimestamp(commentsList, user.getNewTimeStampsComments().get(broadcast.getId()));
                userNumberOfReadDiscussions = user.getNoOfReadDiscussions() + numberOfUnreadComments;
                user.setNoOfReadDiscussions(userNumberOfReadDiscussions);
                userDB.child("noOfReadDiscussions").setValue(userNumberOfReadDiscussions);
                break;
        }

        //updating user latest timestamp for that comment
        HashMap<String, Long> tempCommentTimeStamps = new HashMap<>(user.getNewTimeStampsComments());
        tempCommentTimeStamps.put(broadcast.getId(), broadcast.getLatestCommentTimestamp());
        user.setNewTimeStampsComments(tempCommentTimeStamps);
        SessionStorage.saveUser((Activity) mContext, user);
        userDB.child("newTimeStampsComments").child(broadcast.getId()).setValue(broadcast.getLatestCommentTimestamp());
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ListView commentListView;
        private EditText commentEditText;
        private Button commentSend;
        public ViewHolder(View view) {
            super(view);
            commentListView = view.findViewById(R.id.full_page_broadcast_comments_display);
            commentEditText = view.findViewById(R.id.full_page_broadcast_comment_entry);
            commentSend = view.findViewById(R.id.fullpage_broadcast_comment_send_button);
        }
    }
}
