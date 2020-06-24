package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
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

import circleapp.circlepackage.circle.Helpers.FullPageImageDisplay;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class FullPageBroadcastCardAdapter extends RecyclerView.Adapter<FullPageBroadcastCardAdapter.ViewHolder> {
    private Context mContext;
    private List<Broadcast> broadcastList;
    private Circle circle;
    private FirebaseDatabase database;
    private DatabaseReference broadcastCommentsDB, broadcastDB, userDB;
    private Vibrator v;
    private FirebaseAuth currentUser;
    private User user;

    public FullPageBroadcastCardAdapter(Context mContext, List<Broadcast> broadcastList, Circle circle) {
        this.mContext = mContext;
        this.broadcastList = broadcastList;
        this.circle = circle;

        database = FirebaseDatabase.getInstance();
        broadcastCommentsDB = database.getReference("BroadcastComments");
        userDB = database.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        broadcastDB = database.getReference("Broadcasts");
        currentUser = FirebaseAuth.getInstance();
        user = SessionStorage.getUser((Activity) mContext);
        v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @NonNull
    @Override
    public FullPageBroadcastCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_full_page_card_item, parent, false);
        return new FullPageBroadcastCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FullPageBroadcastCardAdapter.ViewHolder holder, int position) {
        ((Activity) mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        CommentAdapter commentAdapter;
        List<Comment> commentsList = new ArrayList<>();
        Broadcast currentBroadcast = broadcastList.get(position);

        commentAdapter = new CommentAdapter(mContext, commentsList);
        holder.commentListView.setAdapter(commentAdapter);

        setBroadcastInfo(mContext, holder, currentBroadcast);

        broadcastCommentsDB.child(circle.getId()).child(currentBroadcast.getId()).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment tempComment = dataSnapshot.getValue(Comment.class);
                commentsList.add(tempComment); //to store timestamp values descendingly
                commentAdapter.notifyDataSetChanged();
                holder.commentListView.setSelection(holder.commentListView.getAdapter().getCount() - 1);

                HelperMethods.setListViewHeightBasedOnChildren(holder.commentListView);
                if (commentsList.size() == currentBroadcast.getNumberOfComments())
                    updateUserFields(commentsList, currentBroadcast);
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

    public void setBroadcastInfo(Context context, ViewHolder viewHolder, Broadcast broadcast){

        final Poll poll;

        if (broadcast.getCreatorPhotoURI().length() > 10) { //checking if its uploaded image
            Glide.with((Activity) context)
                    .load(broadcast.getCreatorPhotoURI())
                    .into(viewHolder.profPicDisplay);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(broadcast.getCreatorPhotoURI());
            Glide.with((Activity) context)
                    .load(ContextCompat.getDrawable((Activity) context, profilePic))
                    .into(viewHolder.profPicDisplay);
        }

        viewHolder.broadcastTitle.setText(broadcast.getTitle());

        //calculating and setting time elapsed
        long currentTime = System.currentTimeMillis();
        long createdTime = broadcast.getTimeStamp();
        String timeElapsed = HelperMethods.getTimeElapsed(currentTime, createdTime);
        viewHolder.timeElapsedDisplay.setText(timeElapsed);

        //new comments setter
        viewHolder.viewComments.setText(broadcast.getNumberOfComments() + " messages");
        if(user.getNewTimeStampsComments().get(broadcast.getId()) < broadcast.getLatestCommentTimestamp())
            viewHolder.viewComments.setTextColor(context.getResources().getColor(R.color.color_blue));


        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> {
            context.startActivity(new Intent((Activity)context, BroadcastComments.class));
            SessionStorage.saveBroadcast((Activity)context, broadcast);
            ((Activity) context).finish();
        });

        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        if (broadcast.getMessage() == null)
            viewHolder.broadcastMessageDisplay.setVisibility(View.GONE);
        else
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
            SessionStorage.saveBroadcast((Activity) context, broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });

        if(broadcast.getAttachmentURI()!=null){
            viewHolder.imageView.setVisibility(View.VISIBLE);
            //setting imageview
            Glide.with((Activity) context)
                    .load(broadcast.getAttachmentURI())
                    .into(viewHolder.imageView);

            //navigate to full screen photo display when clicked
            viewHolder.imageView.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullPageImageDisplay.class);
                intent.putExtra("uri", broadcast.getAttachmentURI());
                context.startActivity(intent);
                ((Activity) context).finish();
            });
        }


        if (broadcast.isPollExists() == true) {
            poll = broadcast.getPoll();
            viewHolder.pollOptionsDisplayGroup.setVisibility(View.VISIBLE);
            viewHolder.viewPollAnswers.setVisibility(View.VISIBLE);
            viewHolder.broadcastTitle.setText(poll.getQuestion());
            HashMap<String, Integer> pollOptions = poll.getOptions();

            //Option Percentage Calculation
            int totalValue = 0;
            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {
                totalValue += entry.getValue();
            }

            //clear option display each time to avoid repeating options
            if (viewHolder.pollOptionsDisplayGroup.getChildCount() > 0)
                viewHolder.pollOptionsDisplayGroup.removeAllViews();

            if (poll.getUserResponse() != null && poll.getUserResponse().containsKey(currentUser.getCurrentUser().getUid()))
                viewHolder.setCurrentUserPollOption(poll.getUserResponse().get(currentUser.getCurrentUser().getUid()));

            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {

                int percentage = 0;
                if (totalValue != 0)
                    percentage = (int) (((double) entry.getValue() / totalValue) * 100);

                RadioButton button = HelperMethods.generateRadioButton(context, entry.getKey(), percentage);
                LinearLayout layout = HelperMethods.generateLayoutPollOptionBackground(context, button, percentage);

                if (viewHolder.currentUserPollOption != null && viewHolder.currentUserPollOption.equals(button.getText()))
                    button.setChecked(true);

                button.setOnClickListener(view -> {
                    vibrate();
                    Bundle params1 = new Bundle();
                    params1.putString("PollInteracted", "Radio button");

                    String option = button.getText().toString();
                    HashMap<String, Integer> pollOptionsTemp = poll.getOptions();
                    int currentSelectedVoteCount = poll.getOptions().get(option);

                    if (viewHolder.getCurrentUserPollOption() == null) { //voting for first time
                        ++currentSelectedVoteCount;
                        pollOptionsTemp.put(option, currentSelectedVoteCount);
                        viewHolder.setCurrentUserPollOption(option);
                    } else if (!viewHolder.getCurrentUserPollOption().equals(option)) {
                        int userPreviousVoteCount = poll.getOptions().get(viewHolder.getCurrentUserPollOption()); //repeated vote (regulates count)

                        --userPreviousVoteCount;
                        ++currentSelectedVoteCount;
                        pollOptionsTemp.put(option, currentSelectedVoteCount);
                        pollOptionsTemp.put(viewHolder.getCurrentUserPollOption(), userPreviousVoteCount);
                        viewHolder.setCurrentUserPollOption(option);
                    }

                    HashMap<String, String> userResponseHashmap;
                    if(poll.getUserResponse()!=null){
                        userResponseHashmap = new HashMap<>(poll.getUserResponse());
                        userResponseHashmap.put(currentUser.getCurrentUser().getUid(), viewHolder.getCurrentUserPollOption());
                    } else {
                        userResponseHashmap = new HashMap<>();
                        userResponseHashmap.put(currentUser.getCurrentUser().getUid(), viewHolder.getCurrentUserPollOption());
                    }

                    poll.setOptions(pollOptionsTemp);
                    poll.setUserResponse(userResponseHashmap);
                    broadcast.setPoll(poll);

                    broadcastDB.child(circle.getId()).child(broadcast.getId()).child("poll")
                            .child("userResponse").child(currentUser.getCurrentUser().getUid()).setValue(viewHolder.getCurrentUserPollOption());

                    broadcastDB.child(circle.getId()).child(broadcast.getId())
                            .child("poll").child("options").setValue(pollOptionsTemp);

                    Toast.makeText(context, "Thanks for voting", Toast.LENGTH_SHORT).show();

                    setBroadcastInfo(context, viewHolder, broadcast);

                });
                viewHolder.pollOptionsDisplayGroup.addView(layout);
                button.setPressed(true);
            }
        }

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public void updateUserFields(List<Comment> commentsList, Broadcast broadcast) {
        User user = SessionStorage.getUser((Activity) mContext);

        HashMap<String, Integer> tempNoOfDiscussion = new HashMap<>(user.getNoOfReadDiscussions());
        int numberOfUnreadComments = HelperMethods.returnNoOfCommentsPostTimestamp(commentsList, user.getNewTimeStampsComments().get(broadcast.getId()));
        int currentRead = HelperMethods.returnNumberOfReadCommentsForCircle(user, circle);
        tempNoOfDiscussion.put(circle.getId(), currentRead + numberOfUnreadComments);
        user.setNoOfReadDiscussions(tempNoOfDiscussion);
        userDB.child("noOfReadDiscussions").setValue(tempNoOfDiscussion);

        //updating user latest timestamp for that comment
        HashMap<String, Long> tempCommentTimeStamps = new HashMap<>(user.getNewTimeStampsComments());
        tempCommentTimeStamps.put(broadcast.getId(), broadcast.getLatestCommentTimestamp());
        user.setNewTimeStampsComments(tempCommentTimeStamps);
        SessionStorage.saveUser((Activity) mContext, user);
        userDB.child("newTimeStampsComments").child(broadcast.getId()).setValue(broadcast.getLatestCommentTimestamp());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListView commentListView;
        private ScrollView container;
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup;
        private String currentUserPollOption = null;
        private Button viewPollAnswers;
        private PhotoView imageView;

        public ViewHolder(View view) {
            super(view);
            commentListView = view.findViewById(R.id.full_page_broadcast_comments_display);
            container = view.findViewById(R.id.full_page_broadcast_container);
            broadcastNameDisplay = view.findViewById(R.id.full_page_broadcast_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.full_page_broadcast_Message);
            timeElapsedDisplay = view.findViewById(R.id.full_page_broadcast_postedTime);
            profPicDisplay = view.findViewById(R.id.full_page_broadcast_profilePicture);
            pollOptionsDisplayGroup = view.findViewById(R.id.full_page_broadcast_poll_options_radio_group);
            viewComments = view.findViewById(R.id.full_page_broadcast_viewComments);
            viewPollAnswers = view.findViewById(R.id.full_page_broadcast_view_poll_answers);
            broadcastTitle = view.findViewById(R.id.full_page_broadcast_Title);
            imageView = view.findViewById(R.id.uploaded_image_display_broadcast_full_page);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }

    public void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

}
