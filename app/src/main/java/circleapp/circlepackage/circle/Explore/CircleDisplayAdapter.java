package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
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

        GradientDrawable wbItemBackground = HelperMethods.gradientRectangleDrawableSetter(30);

        GradientDrawable moreMembersColor = HelperMethods.gradientRectangleDrawableSetter(130);

        GradientDrawable dividerColor = HelperMethods.gradientRectangleDrawableSetter(1);

        switch (i % 3) {
            case 0:
                wbItemBackground.setColor(context.getResources().getColor(R.color.solidPurple));
                moreMembersColor.setColor(context.getResources().getColor(R.color.transparentPurple));
                dividerColor.setColor(context.getResources().getColor(R.color.transparentPurple));
                break;
            case 1:
                wbItemBackground.setColor(context.getResources().getColor(R.color.solidPink));
                moreMembersColor.setColor(context.getResources().getColor(R.color.transparentPink));
                dividerColor.setColor(context.getResources().getColor(R.color.transparentPink));
                break;
            case 2:
                wbItemBackground.setColor(context.getResources().getColor(R.color.solidTurquoise));
                moreMembersColor.setColor(context.getResources().getColor(R.color.transparentTurqoise));
                dividerColor.setColor(context.getResources().getColor(R.color.transparentTurqoise));
                break;
        }

        //set the details of each circle to its respective card.
        viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        viewHolder.tv_circleName.setText(currentCircle.getName());
        viewHolder.tv_creatorName.setText("By " + currentCircle.getCreatorName());
        viewHolder.tv_circleDesc.setText(currentCircle.getDescription());
        viewHolder.container.setBackground(wbItemBackground);
        viewHolder.membersCount.setBackground(moreMembersColor);
        viewHolder.divider.setBackground(dividerColor);

        String date = HelperMethods.convertIntoDateFormat("dd MMM, yyyy", currentCircle.getTimestamp());
        viewHolder.tv_createdDate.setText(date);

        if (currentCircle.getMembersList() != null)
            viewHolder.membersCount.setText("+" + currentCircle.getMembersList().size());
        else {
            viewHolder.threeMemberPicContainer.setVisibility(View.GONE);
            viewHolder.membersCount.setText("Be the first to join");
        }

        //onclick for join and share
        viewHolder.join.setOnClickListener(view -> {
            if (!isApplicant)
                applyOrJoin(viewHolder, currentCircle);
            else if (currentCircle.getApplicantsList() == null)
                applyOrJoin(viewHolder, currentCircle);
        });

        viewHolder.share.setOnClickListener(view -> {
            HelperMethods.showShareCirclePopup(currentCircle, (Activity) context);
        });
    }

    @Override
    public int getItemCount() {
        return circleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_circleName, tv_creatorName, tv_circleDesc, tv_createdDate;
        private LinearLayout container;
        private RelativeLayout threeMemberPicContainer;
        private ImageButton share;
        private Button join, membersCount;
        private View divider;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            threeMemberPicContainer = view.findViewById(R.id.three_memers_prof_pic);
            tv_createdDate = view.findViewById(R.id.circle_created_date);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            share = view.findViewById(R.id.circle_card_share);
            join = view.findViewById(R.id.circle_card_join);
            membersCount = view.findViewById(R.id.members_count_button);
            divider = view.findViewById(R.id.project_item_divider);
        }
    }

    private void applyOrJoin(ViewHolder viewHolder, final Circle circle) {

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
