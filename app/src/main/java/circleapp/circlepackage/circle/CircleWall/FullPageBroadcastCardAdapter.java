package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
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
    private User user;
    private int initialIndex;

    public FullPageBroadcastCardAdapter(Context mContext, List<Broadcast> broadcastList, Circle circle, int initialIndex) {
        this.mContext = mContext;
        this.broadcastList = broadcastList;
        this.circle = circle;
        this.initialIndex = initialIndex;
        user = SessionStorage.getUser((Activity) mContext);
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

        holder.collapseBroadcastView.setOnClickListener(view -> HelperMethods.collapse(holder.broadcst_container));
        holder.collapseCommentView.setOnClickListener(view -> HelperMethods.expand(holder.broadcst_container));

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);
        holder.commentListView.setLayoutManager(mLinearLayoutManager);

        holder.addCommentButton.setOnClickListener(view -> {
            String commentMessage = holder.addCommentEditText.getText().toString().trim();
            if (!commentMessage.equals("")) {
                makeCommentEntry(commentMessage, currentBroadcast);
            }
            holder.addCommentEditText.setText("");
        });


        String commentsDisplayText = currentBroadcast.getNumberOfComments() + " messages";
        holder.viewComments.setText(commentsDisplayText);

        setBroadcastInfo(mContext, holder, currentBroadcast);

        commentAdapter = new CommentAdapter(mContext, commentsList);
        holder.commentListView.setAdapter(commentAdapter);

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of((FragmentActivity) mContext).get(FirebaseRetrievalViewModel.class);
        LiveData<String[]> liveData = viewModel.getDataSnapsCommentsLiveData(circle.getId(), currentBroadcast.getId());

        liveData.observe((LifecycleOwner) mContext, returnArray -> {
            Comment tempComment = new Gson().fromJson(returnArray[0], Comment.class);
            commentsList.add(tempComment); //to store timestamp values descendingly
            commentAdapter.notifyDataSetChanged();

            Log.d("kewjfnwe", initialIndex + " " + position);

            if (position == initialIndex)
                HelperMethods.collapse(holder.broadcst_container);

            if (commentsList.size() == currentBroadcast.getNumberOfComments())
                updateUserFields(currentBroadcast, "view");

        });
    }

    public void setBroadcastInfo(Context context, ViewHolder viewHolder, Broadcast broadcast) {

        final Poll poll;

        if (broadcast.getCreatorPhotoURI().length() > 10) { //checking if its uploaded image
            Glide.with((Activity) context)
                    .load(broadcast.getCreatorPhotoURI())
                    .into(viewHolder.profPicDisplay);
        } else if (broadcast.getCreatorPhotoURI().equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(viewHolder.profPicDisplay);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(broadcast.getCreatorPhotoURI());
            Glide.with((Activity) context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(viewHolder.profPicDisplay);
        }

        viewHolder.broadcastTitle.setText(broadcast.getTitle());

        //calculating and setting time elapsed
        long currentTime = System.currentTimeMillis();
        long createdTime = broadcast.getTimeStamp();
        String timeElapsed = HelperMethods.getTimeElapsed(currentTime, createdTime);
        viewHolder.timeElapsedDisplay.setText(timeElapsed);

        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> HelperMethods.collapse(viewHolder.broadcst_container));

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

        if (broadcast.getAttachmentURI() != null) {
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

            if (poll.getUserResponse() != null && poll.getUserResponse().containsKey(user.getUserId()))
                viewHolder.setCurrentUserPollOption(poll.getUserResponse().get(user.getUserId()));

            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {

                int percentage = 0;
                if (totalValue != 0)
                    percentage = (int) (((double) entry.getValue() / totalValue) * 100);

                RadioButton button = HelperMethods.generateRadioButton(context, entry.getKey(), percentage);
                LinearLayout layout = HelperMethods.generateLayoutPollOptionBackground(context, button, percentage);

                if (viewHolder.currentUserPollOption != null && viewHolder.currentUserPollOption.equals(button.getText()))
                    button.setChecked(true);

                button.setOnClickListener(view -> {
                    HelperMethods.vibrate(context);
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
                    if (poll.getUserResponse() != null) {
                        userResponseHashmap = new HashMap<>(poll.getUserResponse());
                        userResponseHashmap.put(user.getUserId(), viewHolder.getCurrentUserPollOption());
                    } else {
                        userResponseHashmap = new HashMap<>();
                        userResponseHashmap.put(user.getUserId(), viewHolder.getCurrentUserPollOption());
                    }

                    Toast.makeText(context, "Thanks for voting", Toast.LENGTH_SHORT).show();

                    setBroadcastInfo(context, viewHolder, broadcast);

                });
                viewHolder.pollOptionsDisplayGroup.addView(layout);
                button.setPressed(true);
            }
        }

    }

    public void makeCommentEntry(String commentMessage, Broadcast broadcast) {
        long currentCommentTimeStamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", currentCommentTimeStamp);
        map.put("comment", commentMessage);
        map.put("commentorId", SessionStorage.getUser((Activity) mContext).getUserId());
        map.put("commentorPicURL", SessionStorage.getUser((Activity) mContext).getProfileImageLink());
        map.put("commentorName", SessionStorage.getUser((Activity) mContext).getName().trim());

        FirebaseWriteHelper.makeNewComment(map, circle.getId(), broadcast.getId());
        SendNotification.sendCommentInfo(user.getUserId(), broadcast.getId(), circle.getName(), circle.getId(), user.getName(), broadcast.getListenersList(), circle.getBackgroundImageLink(), commentMessage);

        updateCommentNumbersPostCreate(broadcast, currentCommentTimeStamp);
        updateUserFields(broadcast, "create");
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public void updateUserFields(Broadcast broadcast, String navFrom) {
        HashMap<String, Integer> tempNoOfDiscussion;
        if (user.getNoOfReadDiscussions() != null)
            tempNoOfDiscussion = new HashMap<>(user.getNoOfReadDiscussions());
        else
            tempNoOfDiscussion = new HashMap<>();

        switch (navFrom) {
            case "create":
                //updating userReadDiscussions after creating the comment
                int updateDiscussionInt;
                if (tempNoOfDiscussion.containsKey(broadcast.getId()))
                    updateDiscussionInt = tempNoOfDiscussion.get(broadcast.getId());
                else
                    updateDiscussionInt = 0;
                tempNoOfDiscussion.put(broadcast.getId(), updateDiscussionInt + 1);
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                FirebaseWriteHelper.updateUser(user, mContext);
                break;

            case "view":
                tempNoOfDiscussion.put(broadcast.getId(), broadcast.getNumberOfComments());
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                FirebaseWriteHelper.updateUser(user, mContext);
                break;
        }

        //updating user latest timestamp for that comment
        HashMap<String, Long> tempCommentTimeStamps = new HashMap<>(user.getNewTimeStampsComments());
        tempCommentTimeStamps.put(broadcast.getId(), broadcast.getLatestCommentTimestamp());
        user.setNewTimeStampsComments(tempCommentTimeStamps);
        FirebaseWriteHelper.updateUser(user, mContext);
    }

    public void updateCommentNumbersPostCreate(Broadcast broadcast, long timetamp) {
        //updating broadCastTimeStamp after creating the comment
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        FirebaseWriteHelper.updateBroadcast(broadcast, mContext, circle.getId());

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        FirebaseWriteHelper.updateCircle(circle, mContext);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView commentListView;
        private RelativeLayout broadcst_container;
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup;
        private String currentUserPollOption = null;
        private Button viewPollAnswers, collapseBroadcastView, collapseCommentView, addCommentButton;
        private EditText addCommentEditText;
        private PhotoView imageView;

        public ViewHolder(View view) {
            super(view);
            commentListView = view.findViewById(R.id.full_page_broadcast_comments_display);
            broadcst_container = view.findViewById(R.id.full_page_broadcast_container);
            broadcastNameDisplay = view.findViewById(R.id.full_page_broadcast_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.full_page_broadcast_Message);
            timeElapsedDisplay = view.findViewById(R.id.full_page_broadcast_postedTime);
            profPicDisplay = view.findViewById(R.id.full_page_broadcast_profilePicture);
            pollOptionsDisplayGroup = view.findViewById(R.id.full_page_broadcast_poll_options_radio_group);
            viewComments = view.findViewById(R.id.full_page_broadcast_viewComments);
            viewPollAnswers = view.findViewById(R.id.full_page_broadcast_view_poll_answers);
            broadcastTitle = view.findViewById(R.id.full_page_broadcast_Title);
            imageView = view.findViewById(R.id.uploaded_image_display_broadcast_full_page);
            collapseBroadcastView = view.findViewById(R.id.clickToViewComments);
            collapseCommentView = view.findViewById(R.id.clickToViewBroadcastFullPage);
            addCommentButton = view.findViewById(R.id.full_page_broadcast_comment_send_button);
            addCommentEditText = view.findViewById(R.id.full_page_broadcast_comment_type_editText);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
