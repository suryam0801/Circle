package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethodsBL;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Comment;
import circleapp.circlepackage.circle.data.LocalObjectModels.Poll;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CommentsViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class FullPageBroadcastCardAdapter extends RecyclerView.Adapter<FullPageBroadcastCardAdapter.ViewHolder> {
    private Context mContext;
    private List<Broadcast> broadcastList;
    private Circle circle;
    private int initialIndex;
    private InputMethodManager imm;
    private GlobalVariables globalVariables = new GlobalVariables();

    public FullPageBroadcastCardAdapter(Context mContext, List<Broadcast> broadcastList, Circle circle, int initialIndex) {
        this.mContext = mContext;
        this.broadcastList = broadcastList;
        this.circle = circle;
        this.initialIndex = initialIndex;
        imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
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


        Broadcast currentBroadcast = broadcastList.get(position);
        User user = globalVariables.getCurrentUser();

        holder.collapseBroadcastView.setOnClickListener(view -> {
            HelperMethodsBL.updateUserFields(currentBroadcast, "view", user);
            HelperMethodsBL.initializeNewCommentsAlertTimestamp(broadcastList.get(position), user);
            HelperMethodsUI.collapse(holder.broadcst_container);
            holder.newNotifsContainer.setVisibility(View.GONE);
        });

        holder.collapseCommentView.setOnClickListener(view -> {
            HelperMethodsUI.expand(holder.broadcst_container);
            globalVariables.saveCurrentBroadcast(broadcastList.get(position));
            try {
                imm.hideSoftInputFromWindow(((Activity) mContext).getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
            }
        });

        holder.addCommentButton.setOnClickListener(view -> {
            String commentMessage = holder.addCommentEditText.getText().toString().trim();
            if (!commentMessage.equals("")) {
                makeCommentEntry(commentMessage, currentBroadcast, user);
            }
            holder.addCommentEditText.setText("");
        });

        String commentsDisplayText = currentBroadcast.getNumberOfComments() + " messages";
        holder.viewComments.setText(commentsDisplayText);

        setBroadcastInfo(mContext, holder, currentBroadcast, user);

        final boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(currentBroadcast.getId());

        if (broadcastMuted) {
            holder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
        } else {
            int noOfUserUnread = currentBroadcast.getNumberOfComments() - user.getNoOfReadDiscussions().get(currentBroadcast.getId());
            if (noOfUserUnread > 0) {
                holder.newNotifsContainer.setVisibility(View.VISIBLE);
                holder.newNotifsTV.setText(noOfUserUnread + "");
            }
            FirebaseWriteHelper.broadcastListenerList(0, user.getUserId(), circle.getId(), currentBroadcast.getId());
        }

        holder.notificationToggle.setOnClickListener(view -> {
            toggleNotif(currentBroadcast, holder);
        });


        CommentAdapter commentAdapter;
        List<Comment> commentsList = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, true);
        holder.commentListView.setLayoutManager(layoutManager);

        commentAdapter = new CommentAdapter(mContext, commentsList, currentBroadcast);
        holder.commentListView.setAdapter(commentAdapter);

        CommentsViewModel viewModel = ViewModelProviders.of((FragmentActivity) mContext).get(CommentsViewModel.class);
        LiveData<String[]> liveData = viewModel.getDataSnapsCommentsLiveData(circle.getId(), currentBroadcast.getId());

        liveData.observe((LifecycleOwner) mContext, returnArray -> {
            Comment tempComment = new Gson().fromJson(returnArray[0], Comment.class);
            commentsList.add(0, tempComment); //to store timestamp values descendingly
            commentAdapter.notifyItemInserted(0);
            //holder.commentListView.scrollToPosition(0);
            holder.commentListView.smoothScrollToPosition(0);

            if (position == initialIndex) {
                HelperMethodsUI.collapse(holder.broadcst_container);
                HelperMethodsBL.updateUserFields(currentBroadcast, "view", user);
                HelperMethodsBL.initializeNewCommentsAlertTimestamp(broadcastList.get(position), user);
            }
        });

    }

    public void toggleNotif(Broadcast broadcast, ViewHolder viewHolder) {
        User user = globalVariables.getCurrentUser();
        List<String> userMutedArray;
        if (user.getMutedBroadcasts() != null)
            userMutedArray = new ArrayList<>(user.getMutedBroadcasts());
        else
            userMutedArray = new ArrayList<>();

        boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());

        if (broadcastMuted) {
            viewHolder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_listening_icon));
            userMutedArray.remove(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            FirebaseWriteHelper.updateUser(user);
            FirebaseWriteHelper.broadcastListenerList(0, user.getUserId(), circle.getId(), broadcast.getId());
        } else {
            viewHolder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
            userMutedArray.add(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            FirebaseWriteHelper.updateUser(user);
            FirebaseWriteHelper.broadcastListenerList(1, user.getUserId(), circle.getId(), broadcast.getId());
        }
    }

    public void setBroadcastInfo(Context context, ViewHolder viewHolder, Broadcast broadcast, User user) {

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
        String timeElapsed = HelperMethodsUI.getTimeElapsed(currentTime, createdTime);
        viewHolder.timeElapsedDisplay.setText(timeElapsed);

        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> HelperMethodsUI.collapse(viewHolder.broadcst_container));

        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        if (broadcast.getMessage() == null)
            viewHolder.broadcastMessageDisplay.setVisibility(View.GONE);
        else
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
            globalVariables.saveCurrentBroadcast(broadcast);
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

                RadioButton button = HelperMethodsUI.generateRadioButton(context, entry.getKey(), percentage);
                LinearLayout layout = HelperMethodsUI.generateLayoutPollOptionBackground(context, button, percentage);

                if (viewHolder.currentUserPollOption != null && viewHolder.currentUserPollOption.equals(button.getText()))
                    button.setChecked(true);

                button.setOnClickListener(view -> {
                    HelperMethodsUI.vibrate(context);
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

                    setBroadcastInfo(context, viewHolder, broadcast, user);

                });
                viewHolder.pollOptionsDisplayGroup.addView(layout);
                button.setPressed(true);
            }
        }
    }

    public void makeCommentEntry(String commentMessage, Broadcast broadcast, User user) {
        long currentCommentTimeStamp = System.currentTimeMillis();
        String commentId = FirebaseWriteHelper.getCommentId(circle.getId(),broadcast.getId());
        Comment comment = new Comment(commentId,user.getName().trim(),commentMessage,user.getUserId(),user.getProfileImageLink(),currentCommentTimeStamp);

        FirebaseWriteHelper.makeNewComment(comment, circle.getId(), broadcast.getId());
        SendNotification.sendCommentInfo(mContext,user.getUserId(), broadcast.getId(), circle.getName(), circle.getId(), user.getName(), broadcast.getListenersList(), circle.getBackgroundImageLink(), commentMessage,comment.getCommentorName());

        updateCommentNumbersPostCreate(broadcast, currentCommentTimeStamp);
        HelperMethodsBL.updateUserFields(broadcast, "create", user);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public void updateCommentNumbersPostCreate(Broadcast broadcast, long timetamp) {
        //updating broadCastTimeStamp after creating the comment
        int broacastNumberOfComments = broadcast.getNumberOfComments() + 1;
        broadcast.setLatestCommentTimestamp(timetamp);
        broadcast.setNumberOfComments(broacastNumberOfComments);
        FirebaseWriteHelper.updateBroadcast(broadcast,circle.getId());

        //updating number of discussions in circle
        int circleNewNumberOfDiscussions = circle.getNoOfNewDiscussions() + 1;
        circle.setNoOfNewDiscussions(circleNewNumberOfDiscussions);
        FirebaseWriteHelper.updateCircle(circle);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView commentListView;
        private RelativeLayout broadcst_container;
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle, newNotifsTV;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, newNotifsContainer;
        private ImageButton notificationToggle;
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
            notificationToggle = view.findViewById(R.id.full_page_broadcast_listener_on_off_toggle);
            newNotifsContainer = view.findViewById(R.id.full_page_broadcast_adapter_comments_alert_display);
            newNotifsTV = view.findViewById(R.id.full_page_broadcast_adapter_no_of_comments_display);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
