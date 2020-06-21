package circleapp.circlepackage.circle.Explore;

import android.annotation.SuppressLint;
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
import circleapp.circlepackage.circle.Helpers.HelperMethods;
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

    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull WorkbenchDisplayAdapter.ViewHolder holder, int position) {

        Circle circle = MycircleList.get(position);
        User user = SessionStorage.getUser((Activity) context);

        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("Users").child(user.getUserId());
        analyticsLogEvents = new AnalyticsLogEvents();

        GradientDrawable wbLayoutBackground = HelperMethods.gradientRectangleDrawableSetter(20);
        GradientDrawable wbLineBackground = HelperMethods.gradientRectangleDrawableSetter(20);
        GradientDrawable wbButtonBackground = HelperMethods.gradientRectangleDrawableSetter(50);
        GradientDrawable wbShareButtonBackground = HelperMethods.gradientRectangleDrawableSetter(500);

        int textViewColorCode = 0;
        switch (position % 3) {
            case 0:
                wbLayoutBackground.setColor(context.getResources().getColor(R.color.lightBlue));
                wbLineBackground.setColor(context.getResources().getColor(R.color.darkBlue));
                wbButtonBackground.setColor(context.getResources().getColor(R.color.darkBlue));
                wbShareButtonBackground.setColor(context.getResources().getColor(R.color.darkBlue));
                textViewColorCode = context.getResources().getColor(R.color.darkBlue);
                break;
            case 1:
                wbLayoutBackground.setColor(context.getResources().getColor(R.color.lightPink));
                wbLineBackground.setColor(context.getResources().getColor(R.color.darkPink));
                wbButtonBackground.setColor(context.getResources().getColor(R.color.darkPink));
                wbShareButtonBackground.setColor(context.getResources().getColor(R.color.darkPink));
                textViewColorCode = context.getResources().getColor(R.color.darkPink);
                break;
            case 2:
                wbLayoutBackground.setColor(context.getResources().getColor(R.color.lightOrange));
                wbLineBackground.setColor(context.getResources().getColor(R.color.darkOrange));
                wbButtonBackground.setColor(context.getResources().getColor(R.color.darkOrange));
                wbShareButtonBackground.setColor(context.getResources().getColor(R.color.darkOrange));
                textViewColorCode = context.getResources().getColor(R.color.darkOrange);
                break;
        }

        //setting textcolors
        holder.tv_MycircleName.setTextColor(textViewColorCode);
        holder.tv_circleCreatorName.setTextColor(textViewColorCode);
        holder.tv_circleCreatedDateWB.setTextColor(textViewColorCode);
        holder.membersCount.setTextColor(textViewColorCode);

        //set the details of each circle to its respective card.
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.container.setBackground(wbLayoutBackground);
        holder.divider.setBackground(wbLineBackground);
        holder.shareCircles.setBackground(wbShareButtonBackground);
        holder.tv_MycircleName.setText(circle.getName());
        holder.tv_circleCreatorName.setText(circle.getCreatorName());

        //setting members list
        if (circle.getMembersList() != null)
            holder.membersCount.setText("+" + circle.getMembersList().size());
        else
            holder.membersCount.setText("Circle is empty. Invite members!");

        //setting new applicants
        if(HelperMethods.numberOfApplicants(circle,user) > 0){
            holder.newApplicantsDisplay.setVisibility(View.VISIBLE);
            holder.newApplicantsDisplay.setText(circle.getApplicantsList().size());
        }

        //read for new notifs
        int newNotifs = HelperMethods.newNotifications(circle, user);
        if(newNotifs > 0){
            holder.newNotifAlert.setText(newNotifs+"");
            holder.newNotifAlert.setVisibility(View.VISIBLE);
        }

        //read for new disscussions
        if(user.getNoOfReadDiscussions() < circle.getNoOfNewDiscussions()){
            holder.newDiscussionDisplay.setText((circle.getNoOfNewDiscussions() - user.getNoOfReadDiscussions()) + "");
            holder.newDiscussionDisplay.setVisibility(View.VISIBLE);
        }


        holder.container.setOnClickListener(view -> {
            if (user.getNotificationsAlert() != null) { //if the user has notification info from other circles

                HashMap<String, Integer> tempUserNotifStore = new HashMap<>(user.getNotificationsAlert());
                tempUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(tempUserNotifStore);

                analyticsLogEvents.logEvents(context,"unread_tag", "view_posts_clicked","circle_wall");

            } else { //first time when a user is opening any circle

                HashMap<String, Integer> newUserNotifStore = new HashMap<>();
                newUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(newUserNotifStore);
            }

            userDB.child("notificationsAlert").child(circle.getId()).setValue(circle.getNoOfBroadcasts());

            analyticsLogEvents.logEvents(context,"organic_view", "view_posts_clicked","circle_wall");

            SessionStorage.saveCircle((Activity) context, circle);
            SessionStorage.saveUser((Activity) context, user);
            context.startActivity(new Intent(context, CircleWall.class));
            ((Activity) context).finish();
        });

        //update new notifs value
        holder.shareCircles.setOnClickListener(view -> {
            HelperMethods.showShareCirclePopup(circle, (Activity) context);
        });

        String date = HelperMethods.convertIntoDateFormat("dd MMM, yyyy", circle.getTimestamp());
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
}
