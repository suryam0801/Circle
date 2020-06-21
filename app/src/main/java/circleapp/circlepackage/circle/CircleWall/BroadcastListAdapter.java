package circleapp.circlepackage.circle.CircleWall;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.Helpers.PercentDrawable;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class BroadcastListAdapter extends RecyclerView.Adapter<BroadcastListAdapter.ViewHolder> {
    private List<Broadcast> broadcastList;
    private Context context;
    private Circle circle;
    private FirebaseAuth currentUser;

    private Vibrator v;
    private User user;

    //contructor to set latestCircleList and context for Adapter
    public BroadcastListAdapter(Context context, List<Broadcast> broadcastList, Circle circle) {
        this.context = context;
        this.broadcastList = broadcastList;
        this.circle = circle;
        currentUser = FirebaseAuth.getInstance();
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public BroadcastListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.broadcast_display_view, viewGroup, false);
        return new BroadcastListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {

        final Broadcast broadcast = broadcastList.get(i);
        user = SessionStorage.getUser((Activity) context);

        final Poll poll;

        if (user.getProfileImageLink().length() > 10) { //checking if its uploaded image
            Glide.with((Activity) context)
                    .load(user.getProfileImageLink())
                    .into(viewHolder.profPicDisplay);
        } else { //checking if it is default avatar
            int profilePic = Integer.parseInt(user.getProfileImageLink());
            Glide.with((Activity) context)
                    .load(ContextCompat.getDrawable((Activity) context, profilePic))
                    .into(viewHolder.profPicDisplay);
        }

        //calculating and setting time elapsed
        long currentTime = System.currentTimeMillis();
        long createdTime = broadcast.getTimeStamp();
        String timeElapsed = HelperMethods.getTimeElapsed(currentTime, createdTime);
        viewHolder.timeElapsedDisplay.setText(timeElapsed);

        viewHolder.timeElapsedDisplay.setOnClickListener(view -> {
            SessionStorage.saveBroadcastList((Activity) context, broadcastList);
            Intent intent = new Intent(context, FullPageBroadcastCardView.class);
            intent.putExtra("position", i);
            context.startActivity(intent);
            ((Activity) context).finish();
        });

        //new comments setter
        viewHolder.viewComments.setText(broadcast.getNumberOfComments() + "");
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

        if (broadcast.isPollExists() == true) {
            poll = broadcast.getPoll();
            viewHolder.pollDisplay.setVisibility(View.VISIBLE);
            viewHolder.pollQuestionDisplay.setText(poll.getQuestion());
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

                RadioButton button = generateRadioButton(entry.getKey(), percentage);
                LinearLayout layout = generateTextViewLayoutBackground(button, percentage);

                if (viewHolder.currentUserPollOption != null && viewHolder.currentUserPollOption.equals(button.getText()))
                    button.setChecked(true);

                button.setOnClickListener(view -> {
                    vibrate();
                    Bundle params1 = new Bundle();
                    params1.putString("PollInteracted", "Radio button");

                    Toast.makeText(context, "Thanks for voting", Toast.LENGTH_SHORT).show();
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

                    viewHolder.broadcastDB.child(circle.getId()).child(broadcast.getId()).child("poll")
                            .child("userResponse").child(currentUser.getCurrentUser().getUid()).setValue(viewHolder.getCurrentUserPollOption());

                    viewHolder.broadcastDB.child(circle.getId()).child(broadcast.getId())
                            .child("poll").child("options").setValue(pollOptionsTemp);
                });
                viewHolder.pollOptionsDisplayGroup.addView(layout);
                button.setPressed(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public LinearLayout generateTextViewLayoutBackground(RadioButton button, int percentage) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
        linearLayoutParams.setMargins(0, 10, 0, 10);
        linearLayoutParams.weight = 100;
        layout.setLayoutParams(linearLayoutParams);
        layout.setBackground(new PercentDrawable(100, "#EFF6FF"));

        TextView tv = new TextView(context);
        tv.setText(percentage + "%");
        tv.setTextAppearance(context, R.style.poll_percentage_textview_style);
        tv.setPadding(0, 0, 30, 0);

        layout.addView(button);
        layout.addView(tv);

        return layout;
    }

    public RadioButton generateRadioButton(String optionName, int percentage) {
        RadioButton button = new RadioButton(context);
        LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
        rbParams.weight = 90;
        rbParams.setMargins(0, 0, 0, 0);
        button.setPadding(10, 0, 0, 0);
        button.setLayoutParams(rbParams);
        button.setHighlightColor(Color.BLACK);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{Color.parseColor("#6CACFF")}, //disabled
                        new int[]{Color.parseColor("#6CACFF")} //enabled
                },
                new int[]{
                        Color.parseColor("#6CACFF") //disabled
                        , Color.parseColor("#6CACFF") //enabled
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            button.setButtonTintList(colorStateList);


        button.setBackground(new PercentDrawable(percentage, "#D8E9FF"));
        button.setTextColor(Color.BLACK);
        button.setText(optionName);

        return button;
    }


    public void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView broadcastNameDisplay, broadcastMessageDisplay,
                pollQuestionDisplay, timeElapsedDisplay, viewComments;
        private CircleImageView profPicDisplay;
        private LinearLayout pollDisplay, pollOptionsDisplayGroup;
        private String currentUserPollOption = null;
        private FirebaseDatabase database;
        private DatabaseReference broadcastDB;
        private Button viewPollAnswers;

        public ViewHolder(View view) {
            super(view);
            database = FirebaseDatabase.getInstance();
            broadcastDB = database.getReference("Broadcasts");
            broadcastNameDisplay = view.findViewById(R.id.broadcastWall_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.broadcastWall_Message);
            pollQuestionDisplay = view.findViewById(R.id.broadcastWall_poll_question_textview);
            timeElapsedDisplay = view.findViewById(R.id.broadcastWall_object_postedTime);
            pollOptionsDisplayGroup = view.findViewById(R.id.poll_options_radio_group);
            profPicDisplay = view.findViewById(R.id.broadcasttWall_profilePicture);
            pollDisplay = view.findViewById(R.id.broadcastWall_poll_display_view);
            viewComments = view.findViewById(R.id.broadcastWall_object_viewComments);
            viewPollAnswers = view.findViewById(R.id.view_poll_answers);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
