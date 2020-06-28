package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private FirebaseAuth currentUser;
    private User user;

    //contructor to set latestCircleList and context for Adapter
    public BroadcastListAdapter(Context context, List<Broadcast> broadcastList, Circle circle) {
        this.context = context;
        this.broadcastList = broadcastList;
        this.circle = circle;
        currentUser = FirebaseAuth.getInstance();
        user = SessionStorage.getUser((Activity) context);

    }

    @Override
    public BroadcastListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.broadcast_display_view, viewGroup, false);
        return new BroadcastListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BroadcastListAdapter.ViewHolder viewHolder, int i) {

        final Broadcast broadcast = broadcastList.get(i);

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

        viewHolder.container.setOnClickListener(view -> {
            SessionStorage.saveBroadcastList((Activity) context, broadcastList);
            Intent intent = new Intent(context, FullPageBroadcastCardView.class);
            intent.putExtra("position", i);
            context.startActivity(intent);
            ((Activity) context).finish();
        });

        //new comments setter
        viewHolder.viewComments.setText(broadcast.getNumberOfComments() + " messages");

        try {
            if (user.getNewTimeStampsComments().get(broadcast.getId()) < broadcast.getLatestCommentTimestamp())
                viewHolder.viewComments.setTextColor(context.getResources().getColor(R.color.color_blue));
        } catch (Exception e) {
            //null value for get new timestamp comments for particular broadcast
        }

        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> {
            SessionStorage.saveBroadcast((Activity) context, broadcast);
            Intent intent = new Intent((Activity) context, BroadcastComments.class);
            intent.putExtra("indexOfBroadcast", i);
            context.startActivity(intent);
            ((Activity) context).finish();
        });

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
            ifPollExistsAction(viewHolder, broadcast);

    }

    public void ifImageExistsAction(ViewHolder viewHolder, Broadcast broadcast, int position){
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

    public void ifPollExistsAction(ViewHolder viewHolder, Broadcast broadcast){
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
                HelperMethods.vibrate(context);
                Bundle params1 = new Bundle();
                params1.putString("PollInteracted", "Radio button");

                String option = button.getText().toString();
                updatePollAnswer(option, viewHolder, poll, broadcast);
            });
            viewHolder.pollOptionsDisplayGroup.addView(layout);
            button.setPressed(true);
        }
    }

    public void updatePollAnswer(String option, ViewHolder viewHolder, Poll poll, Broadcast broadcast) {
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
            userResponseHashmap.put(currentUser.getCurrentUser().getUid(), viewHolder.getCurrentUserPollOption());
        } else {
            userResponseHashmap = new HashMap<>();
            userResponseHashmap.put(currentUser.getCurrentUser().getUid(), viewHolder.getCurrentUserPollOption());
        }

        poll.setOptions(pollOptionsTemp);
        poll.setUserResponse(userResponseHashmap);
        broadcast.setPoll(poll);

        viewHolder.broadcastDB.child(circle.getId()).child(broadcast.getId()).child("poll").setValue(poll);
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
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, container;
        private ScrollView pollDisplay;
        private String currentUserPollOption = null;
        private FirebaseDatabase database;
        private DatabaseReference broadcastDB;
        private Button viewPollAnswers;
        private PhotoView imageDisplay;
        private RelativeLayout imageDisplayHolder;
        private ProgressBar imageLoadProgressBar;

        public ViewHolder(View view) {
            super(view);
            database = FirebaseDatabase.getInstance();
            broadcastDB = database.getReference("Broadcasts");
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

        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }

}