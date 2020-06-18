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

import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
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
    AnalyticsLogEvents analyticsLogEvents;

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
        analyticsLogEvents = new AnalyticsLogEvents();

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
        SpannableStringBuilder newuser = new SpannableStringBuilder("Welcome to the CIRCLE "); //start index: 32
        SpannableStringBuilder new_applicant = new SpannableStringBuilder("New member has been added to " + notif.getCircleName() + " mobile application group."); //start index: 28


        ForegroundColorSpan fcsSkyBlue = new ForegroundColorSpan(Color.parseColor("#6CACFF"));

        String state = NotificationList.get(position).getState();

        switch (state) {
            case "Accepted":
                notificationTitle.setText("Application Accepted");
                acceptText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(acceptText);
                analyticsLogEvents.logEvents(mContext, "accepted_notification","accepted_to_circle","notification");
                break;
            case "Rejected":
                gd.setColor(Color.parseColor("#FF6161"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("Application Rejected");
                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                rejectText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(rejectText);
                analyticsLogEvents.logEvents(mContext, "rejected_notification","rejectedto_circle","notification");
                break;
            case "broadcast_added":
                notificationTitle.setText("New BroadCast Added");
                newBroadCast.setSpan(fcsSkyBlue, 32, 32 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(newBroadCast);
                analyticsLogEvents.logEvents(mContext, "broadcast_added","broadcast_added_new","notification");
                break;
            case "new_user":
                notificationTitle.setText("Welcome to CIRCLE");
                newBroadCast.setSpan(fcsSkyBlue, 32, 32 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(newBroadCast);
                break;
            case "new_applicant":
                gd.setColor(Color.parseColor("#D856FF"));
                backgroundColor.setBackground(gd);
                notificationTitle.setText("New Member Onboard");
                //foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
                new_applicant.setSpan(fcsSkyBlue, 29, 29 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                notificationDescription.setText(new_applicant);
                analyticsLogEvents.logEvents(mContext, "new_applicant","new_applicant_to_circle","notification");
                break;
        }

        return v;
    }
}
