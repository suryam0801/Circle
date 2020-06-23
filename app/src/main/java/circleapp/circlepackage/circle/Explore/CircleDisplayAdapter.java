package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.rpc.Help;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Notification.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;

public class CircleDisplayAdapter extends RecyclerView.Adapter<CircleDisplayAdapter.ViewHolder> {
    private List<Circle> circleList;
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference circlesDB, usersDB;
    private Dialog circleJoinDialog;
    private User user;
    AnalyticsLogEvents analyticsLogEvents;

    public CircleDisplayAdapter() {}

    //contructor to set latestCircleList and context for Adapter
    public CircleDisplayAdapter(Context context, List<Circle> circleList, User user) {
        this.context = context;
        this.circleList = circleList;
        this.user = user;
        circleJoinDialog = new Dialog(context);
        database = FirebaseDatabase.getInstance();

        user = SessionStorage.getUser((Activity) context);
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference().child("Users").child(user.getUserId());

        analyticsLogEvents = new AnalyticsLogEvents();
    }

    @Override
    public CircleDisplayAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circle_card_display_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CircleDisplayAdapter.ViewHolder viewHolder, int i) {

        Circle currentCircle = circleList.get(i);

        //check if circle acceptance is review
        if (currentCircle.getAcceptanceType().equalsIgnoreCase("review"))
            viewHolder.join.setText("Apply");

        boolean isApplicant = HelperMethods.ifUserApplied(currentCircle, user.getUserId());
        if (isApplicant) {
            viewHolder.join.setText("Pending Request");
            viewHolder.join.setBackground(context.getResources().getDrawable(R.drawable.unpressable_button));
        }


        //set the details of each circle to its respective card.
        viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        viewHolder.tv_circleName.setText(currentCircle.getName());
        viewHolder.tv_creatorName.setText("By " + currentCircle.getCreatorName());
        viewHolder.tv_circleDesc.setText(currentCircle.getDescription());

        String date = HelperMethods.convertIntoDateFormat("dd MMM, yyyy", currentCircle.getTimestamp());
        viewHolder.tv_createdDate.setText(date);

        //onclick for join and share
        viewHolder.join.setOnClickListener(view -> {
            if (!isApplicant)
                applyOrJoin(currentCircle);
            else if (currentCircle.getApplicantsList() == null)
                applyOrJoin(currentCircle);
        });

        viewHolder.share.setOnClickListener(view -> {
            HelperMethods.showShareCirclePopup(currentCircle, (Activity) context);
        });

        viewHolder.categoryDisplay.setText(currentCircle.getCategory());
    }

    @Override
    public int getItemCount() {
        return circleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_circleName, tv_creatorName, tv_circleDesc, tv_createdDate, categoryDisplay;
        private LinearLayout container;
        private ImageButton share;
        private Button join;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            tv_createdDate = view.findViewById(R.id.explore_circle_created_date);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            share = view.findViewById(R.id.circle_card_share);
            join = view.findViewById(R.id.circle_card_join);
            categoryDisplay = view.findViewById(R.id.circle_category);
        }
    }

    private void applyOrJoin(final Circle circle) {

        circleJoinDialog.setContentView(R.layout.apply_popup_layout);
        circleJoinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button closeDialogButton = circleJoinDialog.findViewById(R.id.completedDialogeDoneButton);
        TextView title = circleJoinDialog.findViewById(R.id.applyConfirmationTitle);
        TextView description = circleJoinDialog.findViewById(R.id.applyConfirmationDescription);

        Subscriber subscriber = new Subscriber(user.getUserId(), user.getName(),
                user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

        if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
            analyticsLogEvents.logEvents(context, "circle_apply", "apply_explore", "on_button_click");
            //adding userID to applicants list
            circlesDB.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            SendNotification.sendnotification("new_applicant", circle.getId(), circle.getName(), circle.getCreatorID());
        } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
            database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
            //adding userID to members list in circlesReference
            analyticsLogEvents.logEvents(context, "circle_join", "open_circle", "on_button_click");
            circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
            int nowActive = user.getActiveCircles() + 1;
            usersDB.child("activeCircles").setValue((nowActive));
        }


        if (circle.getAcceptanceType().equalsIgnoreCase("automatic")) {
            title.setText("Successfully Joined!");
            description.setText("Congratulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Enjoy being part of this circle!");
        }

        closeDialogButton.setOnClickListener(view -> {
            if (circle.getAcceptanceType().equalsIgnoreCase("review")) {
                circleJoinDialog.dismiss();
            } else {
                SessionStorage.saveCircle((Activity) context, circle);
                context.startActivity(new Intent(context, CircleWall.class));
                ((Activity) context).finish();
                circleJoinDialog.dismiss();
            }
        });

        circleJoinDialog.show();

    }

}
