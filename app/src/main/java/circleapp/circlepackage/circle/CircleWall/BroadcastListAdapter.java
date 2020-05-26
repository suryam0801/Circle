package circleapp.circlepackage.circle.CircleWall;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.PercentDrawable;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class BroadcastListAdapter extends RecyclerView.Adapter<BroadcastListAdapter.ViewHolder> {
    private List<Broadcast> broadcastList;
    private Context context;
    private Circle circle;

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
    public void onBindViewHolder(final ViewHolder viewHolder, int i){

        final Broadcast broadcast = broadcastList.get(i);
        final Poll poll;
        RadioButton button;

        //calculating time elapsed
        long currentTime = System.currentTimeMillis();
        long createdTime =broadcast.getTimeStamp();
        long days = TimeUnit.MILLISECONDS.toDays(currentTime - createdTime);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - createdTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - createdTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - createdTime);

        if (seconds < 60) {
            viewHolder.timeElapsedDisplay.setText(seconds + "s ago");
        } else if (minutes > 1 && minutes < 60) {
            viewHolder.timeElapsedDisplay.setText(minutes + "m ago");
        } else if (hours > 1 && hours < 24) {
            viewHolder.timeElapsedDisplay.setText(hours + "h ago");
        } else if (days > 1 && days < 365) {
            if (days > 7)
                viewHolder.timeElapsedDisplay.setText((days / 7) + "w ago");
            else
                viewHolder.timeElapsedDisplay.setText(days + "d ago");
        }


        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());
        viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());

        if (broadcast.getAttachmentURI() != null) {
            viewHolder.attachmentDisplay.setVisibility(View.VISIBLE);
            viewHolder.attachmentNameDisplay.setText("Click to download attachment");
        }

        if (broadcast.isPollExists() == true) {
            poll = broadcast.getPoll();
            viewHolder.pollDisplay.setVisibility(View.VISIBLE);
            viewHolder.pollQuestionDisplay.setText(poll.getQuestion());
            HashMap<String, Integer> pollOptions = poll.getOptions();

            //optionPercentageCalculation
            int totalValue = 0;
            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {
                totalValue += entry.getValue();
            }

            if (viewHolder.pollOptionsDisplayGroup.getChildCount() > 0)
                viewHolder.pollOptionsDisplayGroup.removeAllViews();

            if (poll.getUserResponse() != null)
                if (poll.getUserResponse().containsKey("USERID"))
                    viewHolder.setCurrentUserPollOption(poll.getUserResponse().get("USERID"));


            for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {
                button = new RadioButton(context);
                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lparams.setMargins(0, 10, 20, 10);
                button.setPadding(50, 0, 0, 10);
                button.setLayoutParams(lparams);
                button.setHighlightColor(Color.BLACK);
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_enabled}, //disabled
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[]{
                                Color.BLACK //disabled
                                , Color.BLUE //enabled
                        }
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    button.setButtonTintList(colorStateList);
                }
                if (totalValue != 0) {
                    int percentage = (int) (((double) entry.getValue() / totalValue) * 100);
                    button.setBackground(new PercentDrawable(percentage));
                }
                button.setTextColor(Color.BLACK);
                button.setText(entry.getKey());
                viewHolder.pollOptionsDisplayGroup.addView(button);
                button.setPressed(true);
            }

            viewHolder.pollOptionsDisplayGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    RadioButton rb = group.findViewById(checkedId);
                    String option = rb.getText().toString();
                    HashMap<String, Integer> pollOptions = poll.getOptions();
                    int currentSelectedVotes = poll.getOptions().get(option);

                    if (viewHolder.getCurrentUserPollOption() == null) {
                        ++currentSelectedVotes;
                        pollOptions.put(option, currentSelectedVotes);
                        viewHolder.setCurrentUserPollOption(option);
                    } else {
                        if(!viewHolder.getCurrentUserPollOption().equals(option)){
                            int userPreviousVote = poll.getOptions().get(viewHolder.getCurrentUserPollOption()); //if user already saved an answer, this will regulate voting count
                            --userPreviousVote;
                            ++currentSelectedVotes;
                            pollOptions.put(option, currentSelectedVotes);
                            pollOptions.put(viewHolder.getCurrentUserPollOption(), userPreviousVote);
                            viewHolder.setCurrentUserPollOption(option);
                        }
                    }

                    HashMap<String, String> currentUserResponse = new HashMap<>();
                    currentUserResponse.put("USERID", viewHolder.getCurrentUserPollOption());
                    viewHolder.broadcastDB.child(circle.getId()).child(broadcast.getId()).child("poll").child("userResponse").setValue(currentUserResponse);
                    viewHolder.broadcastDB.child(circle.getId()).child(broadcast.getId()).child("poll").child("options").setValue(pollOptions);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView broadcastNameDisplay, broadcastMessageDisplay, attachmentNameDisplay,
                pollQuestionDisplay, timeElapsedDisplay;
        private RadioGroup pollOptionsDisplayGroup;
        private CircleImageView profPicDisplay;
        private LinearLayout attachmentDisplay, pollDisplay;
        private ImageButton attachmentDownloadButton;
        private String currentUserPollOption = null;
        private FirebaseDatabase database;
        private DatabaseReference broadcastDB;

        public ViewHolder(View view) {
            super(view);
            database = FirebaseDatabase.getInstance();
            broadcastDB = database.getReference("Broadcasts");
            broadcastNameDisplay = view.findViewById(R.id.broadcastWall_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.broadcastWall_Message);
            attachmentNameDisplay = view.findViewById(R.id.broadcastWall_fileName);
            pollQuestionDisplay = view.findViewById(R.id.broadcastWall_poll_question_textview);
            timeElapsedDisplay = view.findViewById(R.id.broadcastWall_object_postedTime);
            pollOptionsDisplayGroup = view.findViewById(R.id.poll_options_radio_group);
            profPicDisplay = view.findViewById(R.id.broadcasttWall_profilePicture);
            attachmentDisplay = view.findViewById(R.id.attachment_display);
            pollDisplay = view.findViewById(R.id.broadcastWall_poll_display_view);
            attachmentDownloadButton = view.findViewById(R.id.attachment_download_btn);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
