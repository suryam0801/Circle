package circleapp.circlepackage.circle.Explore;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.materialize.color.Material;

import java.util.List;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<Notification> NotificationList;
//    private TextView notificationTitle, notificationDescription, timeElapsedTextView;
//    private LinearLayout backgroundColor;
//    private AppCompatImageView foregroundIcon;
//    CircleImageView profilePic;

    public NotificationAdapter(Context mContext, List<Notification> NotificationList) {
        this.mContext = mContext;
        this.NotificationList = NotificationList;
    }

//    @Override
//    public int getCount() {
//        return NotificationList.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return NotificationList.get(position);
//    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.notification_object, viewGroup, false);
        return new NotificationAdapter.ViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.ViewHolder holder, int position) {
        Notification notif = NotificationList.get(position);

        long createdTime = notif.getTimestamp();
        long currentTime = System.currentTimeMillis();
        holder.timeElapsedTextView.setText(HelperMethods.getTimeElapsed(currentTime, createdTime));

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setCornerRadius(15.0f); // border corner radius

        SpannableStringBuilder acceptText = new SpannableStringBuilder("Your request to join " + notif.getCircleName() + " has been accepted. You can start a conversation with the group now.");
        SpannableStringBuilder rejectText = new SpannableStringBuilder("Your request to join " + notif.getCircleName() + " has been rejected. Explore all the other Circles that would love to have you and your skills"); //start index: 20
        SpannableStringBuilder newBroadCast = new SpannableStringBuilder(notif.getFrom() + ": " + notif.getMessage()); //start index: 32
        SpannableStringBuilder newComment = new SpannableStringBuilder(notif.getFrom() + ": " + notif.getMessage()); //start index: 30
        SpannableStringBuilder newuser = new SpannableStringBuilder("Welcome to the CIRCLE "); //start index: 32
        SpannableStringBuilder new_applicant = new SpannableStringBuilder("New member has been added to " + notif.getCircleName() + " mobile application group."); //start index: 28
        SpannableStringBuilder report_result_accepted = new SpannableStringBuilder("The " + notif.getType() + " you reported has been removed. Thanks for keeping the platform safe!");
        SpannableStringBuilder report_result_rejected = new SpannableStringBuilder("The " + notif.getType() + " you reported has been reviewed and found not to violate community guidelines. Thanks for keeping the platform safe!");
        SpannableStringBuilder creator_report = new SpannableStringBuilder("The " + notif.getType() + " has been reviewed and found to violate community guidelines. Please refrain from posting such content in the future. Thanks for understanding!");

        ForegroundColorSpan fcsSkyBlue = new ForegroundColorSpan(Color.parseColor("#6CACFF"));

        String state = NotificationList.get(position).getState();

        switch (state) {
            case "Accepted":
                holder.notificationTitle.setText("Application Accepted");
                acceptText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(acceptText);
                break;
            case "Rejected":
                gd.setColor(Color.parseColor("#FF6161"));
                holder.backgroundColor.setBackground(gd);
                holder.notificationTitle.setText("Application Rejected");
                holder.foregroundIcon.setBackground(mContext.getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
                rejectText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(rejectText);
                break;
            case "broadcast_added":
                holder.notificationTitle.setText("New Post Added in " + notif.getCircleName());
                if (notif.getCircleId() == "308be3ec-9b1d-4a05-b1ca-e8d71cd0662e") {
                    holder.setLogo("");
                } else {
                    holder.setLogo(notif.getCircleIcon());
                }
                holder.foregroundIcon.setVisibility(View.GONE);
                holder.profilePic.setVisibility(View.VISIBLE);
                //newBroadCast.setSpan(fcsSkyBlue, 32, 32 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(newBroadCast);
                break;
            case "comment_added":
                holder.notificationTitle.setText("New Comment Added in" + notif.getCircleName());
                if (notif.getCircleId() == "308be3ec-9b1d-4a05-b1ca-e8d71cd0662e") {
                    holder.setLogo("");
                } else {
                    holder.setLogo(notif.getCircleIcon());
                }
                holder.foregroundIcon.setVisibility(View.GONE);
                holder.profilePic.setVisibility(View.VISIBLE);
                //newComment.setSpan(fcsSkyBlue, 30, 30 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(newComment);
                break;
            case "new_user":
                holder.notificationTitle.setText("Welcome to CIRCLE");
                holder.notificationDescription.setText(newuser);
                break;
            case "new_applicant":
                gd.setColor(Color.parseColor("#D856FF"));
                holder.backgroundColor.setBackground(gd);
                holder.notificationTitle.setText("New Member Onboard");
                //foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
                new_applicant.setSpan(fcsSkyBlue, 29, 29 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(new_applicant);
                break;
            case "report_result_accepted":
                gd.setColor(Color.parseColor("#D856FF"));
                holder.backgroundColor.setBackground(gd);
                holder.notificationTitle.setText("Reported Content Removed!");
                report_result_accepted.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(report_result_accepted);
                break;
            case "report_result_rejected":
                gd.setColor(Color.parseColor("#FF6161"));
                holder.backgroundColor.setBackground(gd);
                holder.notificationTitle.setText("Reported Content Not Removed");
                report_result_rejected.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(report_result_rejected);
                break;
            case "creator_report":
                holder.setLogo(notif.getCircleIcon());
                holder.foregroundIcon.setVisibility(View.GONE);
                holder.profilePic.setVisibility(View.VISIBLE);
                holder.notificationTitle.setText("Your Circle Violated Our Policies");
                creator_report.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.notificationDescription.setText(creator_report);
                break;
        }
holder.container.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        FirebaseWriteHelper.NotifyOnclickListener(mContext, notif, position, NotificationList.get(position).getBroadcastId());
    }
});

    }

    @Override
    public int getItemCount() {
        return NotificationList.size();
    }

//    @Override
//    public View getView(int position, View view, ViewGroup viewGroup) {
//        View v = View.inflate(this.mContext, R.layout.notification_object, null);
//
//
//        notificationTitle = v.findViewById(R.id.notification_object_title);
//        notificationDescription = v.findViewById(R.id.notification_object_description);
//        backgroundColor = v.findViewById(R.id.notification_background_icon);
//        foregroundIcon = v.findViewById(R.id.notification_foreground_icon);
//        timeElapsedTextView = v.findViewById(R.id.notification_time_elapsed);
//        profilePic = v.findViewById(R.id.notification_circle_logo);
//
//        Notification notif = NotificationList.get(position);
//
//        long createdTime = notif.getTimestamp();
//        long currentTime = System.currentTimeMillis();
//        timeElapsedTextView.setText(HelperMethods.getTimeElapsed(currentTime, createdTime));
//
//        GradientDrawable gd = new GradientDrawable();
//        gd.setShape(GradientDrawable.OVAL);
//        gd.setCornerRadius(15.0f); // border corner radius
//
//        SpannableStringBuilder acceptText = new SpannableStringBuilder("Your request to join " + notif.getCircleName() + " has been accepted. You can start a conversation with the group now.");
//        SpannableStringBuilder rejectText = new SpannableStringBuilder("Your request to join " + notif.getCircleName() + " has been rejected. Explore all the other Circles that would love to have you and your skills"); //start index: 20
//        SpannableStringBuilder newBroadCast = new SpannableStringBuilder(notif.getFrom() + ": " + notif.getMessage()); //start index: 32
//        SpannableStringBuilder newComment = new SpannableStringBuilder(notif.getFrom() + ": " + notif.getMessage()); //start index: 30
//        SpannableStringBuilder newuser = new SpannableStringBuilder("Welcome to the CIRCLE "); //start index: 32
//        SpannableStringBuilder new_applicant = new SpannableStringBuilder("New member has been added to " + notif.getCircleName() + " mobile application group."); //start index: 28
//        SpannableStringBuilder report_result_accepted = new SpannableStringBuilder("The " + notif.getType() + " you reported has been removed. Thanks for keeping the platform safe!");
//        SpannableStringBuilder report_result_rejected = new SpannableStringBuilder("The " + notif.getType() + " you reported has been reviewed and found not to violate community guidelines. Thanks for keeping the platform safe!");
//        SpannableStringBuilder creator_report = new SpannableStringBuilder("The " + notif.getType() + " has been reviewed and found to violate community guidelines. Please refrain from posting such content in the future. Thanks for understanding!");
//
//        ForegroundColorSpan fcsSkyBlue = new ForegroundColorSpan(Color.parseColor("#6CACFF"));
//
//        String state = NotificationList.get(position).getState();
//
//        switch (state) {
//            case "Accepted":
//                notificationTitle.setText("Application Accepted");
//                acceptText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(acceptText);
//                break;
//            case "Rejected":
//                gd.setColor(Color.parseColor("#FF6161"));
//                backgroundColor.setBackground(gd);
//                notificationTitle.setText("Application Rejected");
//                foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_cancel_black_24dp));
//                rejectText.setSpan(fcsSkyBlue, 21, 21 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(rejectText);
//                break;
//            case "broadcast_added":
//                notificationTitle.setText("New Post Added in " + notif.getCircleName());
//                if (notif.getCircleId() == "308be3ec-9b1d-4a05-b1ca-e8d71cd0662e") {
//                    setLogo("");
//                } else {
//                    setLogo(notif.getCircleIcon());
//                }
//                foregroundIcon.setVisibility(View.GONE);
//                profilePic.setVisibility(View.VISIBLE);
//                //newBroadCast.setSpan(fcsSkyBlue, 32, 32 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(newBroadCast);
//                break;
//            case "comment_added":
//                notificationTitle.setText("New Comment Added in" + notif.getCircleName());
//                if (notif.getCircleId() == "308be3ec-9b1d-4a05-b1ca-e8d71cd0662e") {
//                    setLogo("");
//                } else {
//                    setLogo(notif.getCircleIcon());
//                }
//                foregroundIcon.setVisibility(View.GONE);
//                profilePic.setVisibility(View.VISIBLE);
//                //newComment.setSpan(fcsSkyBlue, 30, 30 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(newComment);
//                break;
//            case "new_user":
//                notificationTitle.setText("Welcome to CIRCLE");
//                notificationDescription.setText(newuser);
//                break;
//            case "new_applicant":
//                gd.setColor(Color.parseColor("#D856FF"));
//                backgroundColor.setBackground(gd);
//                notificationTitle.setText("New Member Onboard");
//                //foregroundIcon.setBackground(v.getContext().getResources().getDrawable(R.drawable.ic_person_add_black_24dp));
//                new_applicant.setSpan(fcsSkyBlue, 29, 29 + notif.getCircleName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(new_applicant);
//                break;
//            case "report_result_accepted":
//                gd.setColor(Color.parseColor("#D856FF"));
//                backgroundColor.setBackground(gd);
//                notificationTitle.setText("Reported Content Removed!");
//                report_result_accepted.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(report_result_accepted);
//                break;
//            case "report_result_rejected":
//                gd.setColor(Color.parseColor("#FF6161"));
//                backgroundColor.setBackground(gd);
//                notificationTitle.setText("Reported Content Not Removed");
//                report_result_rejected.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(report_result_rejected);
//                break;
//            case "creator_report":
//                setLogo(notif.getCircleIcon());
//                foregroundIcon.setVisibility(View.GONE);
//                profilePic.setVisibility(View.VISIBLE);
//                notificationTitle.setText("Your Circle Violated Our Policies");
//                creator_report.setSpan(fcsSkyBlue, 4, 4 + notif.getType().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                notificationDescription.setText(creator_report);
//                break;
//        }
//
//        return v;
//
//    }

//    public void setLogo(String circleIcon) {
//        if (circleIcon.length() > 10) {
//            Glide.with(mContext)
//                    .load(circleIcon)
//                    .into(profilePic);
//        } else if (circleIcon.equals("default")) {
//            int profile = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
//            Glide.with(mContext)
//                    .load(ContextCompat.getDrawable(mContext, profile))
//                    .into(profilePic);
//        } else {
//
//            Glide.with(mContext)
//                    .load(circleIcon)
//                    .into(profilePic);
//        }
//
//
//    }
//

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView notificationTitle, notificationDescription, timeElapsedTextView;
        private LinearLayout backgroundColor;
        private AppCompatImageView foregroundIcon;
        CircleImageView profilePic;
        LinearLayout container;

        public ViewHolder(@NonNull View v) {
            super(v);
            container = v.findViewById(R.id.notification_container);
            notificationTitle = v.findViewById(R.id.notification_object_title);
            notificationDescription = v.findViewById(R.id.notification_object_description);
            backgroundColor = v.findViewById(R.id.notification_background_icon);
            foregroundIcon = v.findViewById(R.id.notification_foreground_icon);
            timeElapsedTextView = v.findViewById(R.id.notification_time_elapsed);
            profilePic = v.findViewById(R.id.notification_circle_logo);
        }
        public void setLogo(String circleIcon) {
            if (circleIcon.length() > 10) {
                Glide.with(mContext)
                        .load(circleIcon)
                        .into(profilePic);
            } else if (circleIcon.equals("default")) {
                int profile = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
                Glide.with(mContext)
                        .load(ContextCompat.getDrawable(mContext, profile))
                        .into(profilePic);
            } else {

                Glide.with(mContext)
                        .load(circleIcon)
                        .into(profilePic);
            }


        }

    }
}
