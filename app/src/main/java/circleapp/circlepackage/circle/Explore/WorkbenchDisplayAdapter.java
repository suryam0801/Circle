package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    AnalyticsLogEvents analyticsLogEvents;
    //contructor to set MycircleList and context for Adapter
    public WorkbenchDisplayAdapter(List<Circle> mycircleList, Context context) {
        this.MycircleList = mycircleList;
        this.context = context;
    }

    @NonNull
    @Override
    public WorkbenchDisplayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.workbench_circle_display, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkbenchDisplayAdapter.ViewHolder holder, int position) {

        Circle circle = MycircleList.get(position);
        User user = SessionStorage.getUser((Activity) context);

        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("Users").child(user.getUserId());
        analyticsLogEvents = new AnalyticsLogEvents();
        GradientDrawable wbLayoutBackground = new GradientDrawable();
        wbLayoutBackground.setShape(GradientDrawable.RECTANGLE);
        wbLayoutBackground.setCornerRadius(20);

        GradientDrawable wbLineBackground = new GradientDrawable();
        wbLineBackground.setShape(GradientDrawable.RECTANGLE);
        wbLineBackground.setCornerRadius(20);

        GradientDrawable wbButtonBackground = new GradientDrawable();
        wbButtonBackground.setShape(GradientDrawable.RECTANGLE);
        wbButtonBackground.setCornerRadius(50);

        GradientDrawable wbShareButtonBackground = new GradientDrawable();
        wbShareButtonBackground.setShape(GradientDrawable.RECTANGLE);
        wbShareButtonBackground.setCornerRadius(500);

        switch (position % 3) {
            case 0:
                wbLayoutBackground.setColor(Color.parseColor("#D8E9FF"));
                wbLineBackground.setColor(Color.parseColor("#158BF1"));
                wbButtonBackground.setColor(Color.parseColor("#158BF1"));
                wbShareButtonBackground.setColor(Color.parseColor("#158BF1"));
                holder.tv_MycircleName.setTextColor(Color.parseColor("#158BF1"));
                holder.tv_circleCreatorName.setTextColor(Color.parseColor("#158BF1"));
                holder.tv_circleCreatedDateWB.setTextColor(Color.parseColor("#158BF1"));
                holder.membersCount.setTextColor(Color.parseColor("#158BF1"));
                break;
            case 1:
                wbLayoutBackground.setColor(Color.parseColor("#FFD1E9"));
                wbLineBackground.setColor(Color.parseColor("#FF38A2"));
                wbButtonBackground.setColor(Color.parseColor("#FF38A2"));
                wbShareButtonBackground.setColor(Color.parseColor("#FF38A2"));
                holder.tv_MycircleName.setTextColor(Color.parseColor("#FF38A2"));
                holder.tv_circleCreatorName.setTextColor(Color.parseColor("#FF38A2"));
                holder.tv_circleCreatedDateWB.setTextColor(Color.parseColor("#FF38A2"));
                holder.membersCount.setTextColor(Color.parseColor("#FF38A2"));
                break;
            case 2:
                wbLayoutBackground.setColor(Color.parseColor("#FFDDBB"));
                wbLineBackground.setColor(Color.parseColor("#FF9C38"));
                wbButtonBackground.setColor(Color.parseColor("#FF9C38"));
                wbShareButtonBackground.setColor(Color.parseColor("#FF9C38"));
                holder.tv_MycircleName.setTextColor(Color.parseColor("#FF9C38"));
                holder.tv_circleCreatorName.setTextColor(Color.parseColor("#FF9C38"));
                holder.tv_circleCreatedDateWB.setTextColor(Color.parseColor("#FF9C38"));
                holder.membersCount.setTextColor(Color.parseColor("#FF9C38"));
                break;
        }

        //set the details of each circle to its respective card.
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.container.setBackground(wbLayoutBackground);
        holder.divider.setBackground(wbLineBackground);
        holder.shareCircles.setBackground(wbShareButtonBackground);
        holder.tv_MycircleName.setText(circle.getName());
        holder.tv_circleCreatorName.setText(circle.getCreatorName());

        if (circle.getMembersList() != null)
            holder.membersCount.setText("+" + circle.getMembersList().size());
        else
            holder.membersCount.setText("Circle is empty. Invite members!");

        //setting new applicants
        if(circle.getApplicantsList()!= null && circle.getCreatorID().equals(user.getUserId())){
            holder.newApplicantsDisplay.setVisibility(View.VISIBLE);
            if(circle.getApplicantsList().size()>1)
                holder.newApplicantsDisplay.setText(circle.getApplicantsList().size());
        }

        //read for new notifs
        if (user.getNotificationsAlert() != null && user.getNotificationsAlert().containsKey(circle.getId())) {
            int userRead = user.getNotificationsAlert().get(circle.getId());
            Log.d("wekfjnwe", "efknwef " + (circle.getNoOfBroadcasts()));
            Log.d("wekfjnwe", "efknwef " + (userRead));
            if (circle.getNoOfBroadcasts() > userRead) {
                holder.newNotifAlert.setText((circle.getNoOfBroadcasts() - userRead)+"");
                holder.newNotifAlert.setVisibility(View.VISIBLE);
            } else {
                holder.newNotifAlert.setVisibility(View.GONE);
            }
        }

        holder.container.setOnClickListener(view -> {
            if (user.getNotificationsAlert() != null) {
                HashMap<String, Integer> tempUserNotifStore = new HashMap<>(user.getNotificationsAlert());
                tempUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(tempUserNotifStore);
                analyticsLogEvents.logEvents(context,"unread_tag", "view_posts_clicked","circle_wall");
            } else {
                HashMap<String, Integer> tempUserNotifStore = new HashMap<>();
                tempUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(tempUserNotifStore);
            }

            userDB.child("notificationsAlert").child(circle.getId()).setValue(circle.getNoOfBroadcasts());

            analyticsLogEvents.logEvents(context,"organic_view", "view_posts_clicked","circle_wall");

            SessionStorage.saveCircle((Activity) context, circle);
            SessionStorage.saveUser((Activity) context, user);
            context.startActivity(new Intent(context, CircleWall.class));
            ((Activity) context).finish();
        });

        if(user.getNoOfReadDiscussions() < circle.getNoOfNewDiscussions()){
            holder.newDiscussionDisplay.setText((circle.getNoOfNewDiscussions() - user.getNoOfReadDiscussions()) + "");
            holder.newDiscussionDisplay.setVisibility(View.VISIBLE);
        }

        //update new notifs value
        holder.shareCircles.setOnClickListener(view -> {
            showShareCirclePopup(circle);
        });

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(circle.getTimestamp());
        String date = DateFormat.format("dd MMM, yyyy", cal).toString();
        holder.tv_circleCreatedDateWB.setText(date);
    }


    @Override
    public int getItemCount() {
        return MycircleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_MycircleName, tv_circleCreatorName, tv_circleCreatedDateWB, newNotifAlert,
                membersCount, newApplicantsDisplay, newDiscussionDisplay;
        private LinearLayout container;
        private Button shareCircles;

        private View divider;

        public ViewHolder(View view) {
            super(view);
            newApplicantsDisplay = view.findViewById(R.id.newApplicantsDisplay);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircleName);
            tv_circleCreatorName = view.findViewById(R.id.wbcircle_creatorName);
            shareCircles = view.findViewById(R.id.wb_share_circle_button);
            tv_circleCreatedDateWB = view.findViewById(R.id.circle_created_date);
            newNotifAlert = view.findViewById(R.id.newNotifAlertTV);
            divider = view.findViewById(R.id.wb_divider_line);
            membersCount = view.findViewById(R.id.wb_members_count_button);
            newDiscussionDisplay = view.findViewById(R.id.newDiscussionDisplay);
        }
    }

    private void showShareCirclePopup(Circle c) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage = "\nCome join my circle: " + c.getName() + "\n\n";
            shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + c.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }
}
