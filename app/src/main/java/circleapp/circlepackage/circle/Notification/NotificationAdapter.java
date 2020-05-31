package circleapp.circlepackage.circle.Notification;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.R;

public class NotificationAdapter extends BaseAdapter {

    private Context mContext;
    private List<Notification> NotificationList;
    private FirebaseFirestore db;
    private String TAG = "NOTIFICATION_LIST_ADAPTER";
    private TextView notificationTitle, notificationDescription, timeElapsedTextView;
    private LinearLayout backgroundColor;
    private AppCompatImageView foregroundIcon;

    public NotificationAdapter(Context mContext, List<Notification> NotificationList){
        this.mContext = mContext;
        this.NotificationList = NotificationList;
    }

    @Override
    public int getCount() {
        return NotificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return NotificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View v = View.inflate(mContext, R.layout.notification_object, null);

        Notification notif = NotificationList.get(position);

        notificationTitle = v.findViewById(R.id.notification_object_title);
        notificationDescription = v.findViewById(R.id.notification_object_description);
        backgroundColor = v.findViewById(R.id.notification_background_icon);
        foregroundIcon = v.findViewById(R.id.notification_foreground_icon);
        timeElapsedTextView = v.findViewById(R.id.notification_time_elapsed);


        long createdTime = notif.getTimestamp();
        long currentTime = System.currentTimeMillis();
        long days = TimeUnit.MILLISECONDS.toDays(currentTime - createdTime);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - createdTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - createdTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - createdTime);

        if(seconds < 60) {
            timeElapsedTextView.setText(seconds + "s ago");
        } else if (minutes > 1 && minutes < 60){
            timeElapsedTextView.setText(minutes + "m ago");
        } else if (hours > 1 && hours < 24) {
            timeElapsedTextView.setText(hours + "h ago");
        } else if (days > 1 && days < 365 ) {
            if(days > 7)
                timeElapsedTextView.setText((days/7) + "w ago");
            else
                timeElapsedTextView.setText(days + "d ago");
        }

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setCornerRadius(15.0f); // border corner radius

        SpannableStringBuilder acceptText = new SpannableStringBuilder("Your request to join "  + notif.getCircleName() + " has been accepted. You can start a conversation with the group now.");
        SpannableStringBuilder rejectText = new SpannableStringBuilder("Your request to join "  + notif.getCircleName() + " has been rejected. Explore all the other Circles that would love to have you and your skills"); //start index: 20
        SpannableStringBuilder newBroadCast = new SpannableStringBuilder("New BroadCast has been added to " + notif.getCircleName()); //start index: 32
        SpannableStringBuilder newMember = new SpannableStringBuilder("New member has been added to " + notif.getCircleName() + " mobile application group."); //start index: 28
        SpannableStringBuilder taskUpdate = new SpannableStringBuilder("A task has been updated in " + notif.getCircleName()); //start index: 28
        SpannableStringBuilder resourceAdded = new SpannableStringBuilder("A resource has been added to " + notif.getCircleName() + ", check it out and give your opinion."); //start index: 29
        SpannableStringBuilder addedToChatGroup = new SpannableStringBuilder("You have been added to a chat group in " + notif.getCircleName()); //start index: 39
        SpannableStringBuilder memberRemoved = new SpannableStringBuilder("A member has been removed from " + notif.getCircleName()); //start index: 31

        ForegroundColorSpan fcsSkyBlue = new ForegroundColorSpan(Color.parseColor("#6CACFF"));

        String state = NotificationList.get(position).getState();

        switch (state) {
            case "Accepted":
                notificationTitle.setText("Application Accepted");
                acceptText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(acceptText);
                break;
            case "Rejected":
                gd.setColor(Color.parseColor("#FF6161"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("Application Rejected");
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                rejectText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(rejectText);
                break;
            case "broadcast_added":
                notificationTitle.setText("New BroadCast Added");
                newBroadCast.setSpan(fcsSkyBlue, 32, 32 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(newBroadCast);
                break;
            case "new member added":
                gd.setColor(Color.parseColor("#D856FF"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("New Member Onboard");
                //foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
                newMember.setSpan(fcsSkyBlue, 29, 29 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(newMember);
                break;
            case "added to chatgroup":
                gd.setColor(Color.parseColor("#36D1DC"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("Added to New Chatgroup");
                addedToChatGroup.setSpan(fcsSkyBlue, 39, 39 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_chat_black_24dp));
                notificationDescription.setText(addedToChatGroup);
                break;
            case "task added":
                gd.setColor(Color.parseColor("#11F692"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("New Task");
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_playlist_add_black_24dp));
                taskUpdate.setSpan(fcsSkyBlue, 27, 27 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(taskUpdate);
                break;
            case "member removed":
                gd.setColor(Color.parseColor("#FF6161"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("Member Removed");
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_remove_circle_outline_black_24dp));
                memberRemoved.setSpan(fcsSkyBlue, 31, 31 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(memberRemoved);
                break;
            case "resource added":
                gd.setColor(Color.parseColor("#158BF1"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("New Resource");
                resourceAdded.setSpan(fcsSkyBlue, 29, 29 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_attach_file_black_24dp));
                notificationDescription.setText(resourceAdded);
                break;
        }

        return v;
    }
}
