package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class BroadcastListAdapter extends RecyclerView.Adapter<BroadcastListAdapter.ViewHolder> {
    private List<Broadcast> broadcastList;
    private Context context;
    private Circle circle;
    private Dialog deleteBroadcastConfirmation;

    //contructor to set latestCircleList and context for Adapter
    public BroadcastListAdapter(Context context, List<Broadcast> broadcastList, Circle circle) {
        this.context = context;
        this.broadcastList = broadcastList;
        this.circle = circle;
    }

    @Override
    public BroadcastListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.broadcast_display_view, viewGroup, false);
        return new BroadcastListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BroadcastListAdapter.ViewHolder viewHolder, int i) {

        final Broadcast broadcast = broadcastList.get(i);
        User user = SessionStorage.getUser((Activity) context);
        //HelperMethods.initializeBroadcastListener(context, broadcast, user);
        HelperMethods.initializeNewReadComments(context, broadcast, user);
        FirebaseRetrievalViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) context).get(FirebaseRetrievalViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            circle = dataSnapshot.getValue(Circle.class);
            if (circle != null&&circle.getMembersList()!=null) {
                Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                if (circle.getMembersList().containsKey(SessionStorage.getUser((Activity) context).getUserId())) {
                    SessionStorage.saveCircle((Activity) context, circle);
                }
            }
        });

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

        //new comments setter
        String commentsDisplayText = broadcast.getNumberOfComments() + " messages";
        viewHolder.viewComments.setText(commentsDisplayText);

        try {
            if (user.getNewTimeStampsComments() != null && user.getNewTimeStampsComments().get(broadcast.getId()) < broadcast.getLatestCommentTimestamp()) {
                viewHolder.viewComments.setText(commentsDisplayText + " (new)");
            }
        } catch (Exception e) {
            //null value for get new timestamp comments for particular broadcast
        }

        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> intentToDiscussionActivity(i));
        viewHolder.newCommentsTopNotifContainer.setOnClickListener(view -> intentToDiscussionActivity(i));
        viewHolder.container.setOnClickListener(view -> intentToDiscussionActivity(i));

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
            SessionStorage.saveBroadcast((Activity) context, broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });

        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        //setting the main view
        if (broadcast.getMessage() != null) {
            viewHolder.broadcastMessageDisplay.setVisibility(View.VISIBLE);
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());
        }

        if (broadcast.isImageExists() == true)
            ifImageExistsAction(viewHolder, broadcast, i);

        if (broadcast.isPollExists() == true)
            ifPollExistsAction(viewHolder, broadcast, SessionStorage.getUser((Activity) context));


        deleteBroadcastConfirmation = new Dialog(context);

        viewHolder.container.setOnLongClickListener(v -> {
            if (broadcast.getCreatorID().equals(user.getUserId())) {
                showDeleteBroadcastDialog(broadcast, SessionStorage.getUser((Activity) context));
            } else
                HelperMethods.showReportAbusePopup(deleteBroadcastConfirmation, context, circle.getId(), broadcast.getId(), "", broadcast.getCreatorID(), user.getUserId());

            return true;
        });

        boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());

        if (broadcastMuted) {
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
        } else {
            int noOfUserUnread = broadcast.getNumberOfComments() - user.getNoOfReadDiscussions().get(broadcast.getId());
            if (noOfUserUnread > 0) {
                viewHolder.newCommentsTopNotifContainer.setVisibility(View.VISIBLE);
                viewHolder.newCommentsTopTv.setText(noOfUserUnread + "");
            }
            FirebaseWriteHelper.broadcastListenerList(0, user.getUserId(), circle.getId(), broadcast.getId());
        }

        viewHolder.broadcastListenerToggle.setOnClickListener(view -> {
            toggleNotif(broadcast, viewHolder);
        });

    }

    public void toggleNotif(Broadcast broadcast, ViewHolder viewHolder) {
        User user = SessionStorage.getUser((Activity) context);
        List<String> userMutedArray;
        if (user.getMutedBroadcasts() != null)
            userMutedArray = new ArrayList<>(user.getMutedBroadcasts());
        else
            userMutedArray = new ArrayList<>();

        boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());

        if (broadcastMuted) {
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_listening_icon));
            userMutedArray.remove(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            FirebaseWriteHelper.updateUser(user, context);
            FirebaseWriteHelper.broadcastListenerList(0, user.getUserId(), circle.getId(), broadcast.getId());
        } else {
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
            userMutedArray.add(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            FirebaseWriteHelper.updateUser(user, context);
            FirebaseWriteHelper.broadcastListenerList(1, user.getUserId(), circle.getId(), broadcast.getId());
        }
    }

    public void intentToDiscussionActivity(int position) {
        SessionStorage.saveBroadcastList((Activity) context, broadcastList);
        Intent intent = new Intent(context, FullPageBroadcastCardView.class);
        intent.putExtra("broadcastPosition", position);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public void ifImageExistsAction(ViewHolder viewHolder, Broadcast broadcast, int position) {
        viewHolder.imageDisplayHolder.setVisibility(View.VISIBLE);
        viewHolder.imageDisplay.setVisibility(View.VISIBLE);

        //setting imageview
        Glide.with((Activity) context)
                .load(broadcast.getAttachmentURI())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.imageLoadProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.imageLoadProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(viewHolder.imageDisplay);

        //navigate to full screen photo display when clicked
        viewHolder.imageDisplay.setOnClickListener(view -> {
            Intent intent = new Intent(context, FullPageImageDisplay.class);
            intent.putExtra("uri", broadcast.getAttachmentURI());
            intent.putExtra("indexOfBroadcast", position);
            context.startActivity(intent);
            ((Activity) context).finish();
        });

    }

    public void ifPollExistsAction(ViewHolder viewHolder, Broadcast broadcast, User user) {
        final Poll poll;
        poll = broadcast.getPoll();
        viewHolder.pollDisplay.setVisibility(View.VISIBLE);
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
                updatePollAnswer(option, viewHolder, poll, broadcast, SessionStorage.getUser((Activity) context));
            });
            viewHolder.pollOptionsDisplayGroup.addView(layout);
            button.setPressed(true);
        }
    }

    public void updatePollAnswer(String option, ViewHolder viewHolder, Poll poll, Broadcast broadcast, User user) {
        HashMap<String, Integer> pollOptionsTemp = poll.getOptions();
        int currentSelectedVoteCount = poll.getOptions().get(option);

        if (viewHolder.getCurrentUserPollOption() == null) { //voting for first time
            ++currentSelectedVoteCount;
            pollOptionsTemp.put(option, currentSelectedVoteCount);
            viewHolder.setCurrentUserPollOption(option);
        } else if (!viewHolder.getCurrentUserPollOption().equals(option)) {
            int userPreviousVoteCount = poll.getOptions().get(viewHolder.getCurrentUserPollOption()); //repeated vote (regulates count)

            if (userPreviousVoteCount != 0)
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

        poll.setOptions(pollOptionsTemp);
        poll.setUserResponse(userResponseHashmap);
        broadcast.setPoll(poll);

        FirebaseWriteHelper.updateBroadcast(broadcast, context, circle.getId());
    }

    public void showDeleteBroadcastDialog(Broadcast broadcast, User user) {
        deleteBroadcastConfirmation.setContentView(R.layout.delete_broadcast_popup);
        final Button closeDialogButton = deleteBroadcastConfirmation.findViewById(R.id.delete_broadcast_confirm_btn);
        final Button cancel = deleteBroadcastConfirmation.findViewById(R.id.delete_broadcast_cancel_btn);

        closeDialogButton.setOnClickListener(view -> {
            FirebaseWriteHelper.deleteBroadcast(context, circle.getId(), broadcast, circle.getNoOfBroadcasts(), user);
            deleteBroadcastConfirmation.dismiss();
            Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show();
        });

        cancel.setOnClickListener(view -> deleteBroadcastConfirmation.dismiss());

        deleteBroadcastConfirmation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteBroadcastConfirmation.show();
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle, newCommentsTopTv;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, newCommentsTopNotifContainer;
        private RelativeLayout container;
        private ScrollView pollDisplay;
        private String currentUserPollOption = null;
        private Button viewPollAnswers;
        private PhotoView imageDisplay;
        private RelativeLayout imageDisplayHolder;
        private ProgressBar imageLoadProgressBar;
        private ImageButton broadcastListenerToggle;

        public ViewHolder(View view) {
            super(view);
            broadcastNameDisplay = view.findViewById(R.id.broadcastWall_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.broadcastWall_Message);
            timeElapsedDisplay = view.findViewById(R.id.broadcastWall_object_postedTime);
            pollOptionsDisplayGroup = view.findViewById(R.id.poll_options_radio_group);
            profPicDisplay = view.findViewById(R.id.broadcasttWall_profilePicture);
            pollDisplay = view.findViewById(R.id.broadcastWall_poll_display_view);
            viewComments = view.findViewById(R.id.broadcastWall_object_viewComments);
            viewPollAnswers = view.findViewById(R.id.view_poll_answers);
            broadcastTitle = view.findViewById(R.id.broadcastWall_Title);
            container = view.findViewById(R.id.broadcast_display_container);
            imageDisplay = view.findViewById(R.id.uploaded_image_display_broadcast);
            imageDisplayHolder = view.findViewById(R.id.image_display_holder);
            imageLoadProgressBar = view.findViewById(R.id.image_progress);
            newCommentsTopNotifContainer = view.findViewById(R.id.broadcast_adapter_comments_alert_display);
            newCommentsTopTv = view.findViewById(R.id.broadcast_adapter_no_of_comments_display);
            broadcastListenerToggle = view.findViewById(R.id.broadcast_listener_on_off_toggle);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }

}