package circleapp.circlepackage.circle.Explore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class CircleDisplayAdapter extends RecyclerView.Adapter<CircleDisplayAdapter.ViewHolder> {
    private List<Circle> circleList;
    private Context context;
    private FirebaseDatabase database;
    private DatabaseReference circlesDB, usersDB;
    private Dialog circleJoinDialog;
    private FirebaseAuth currentUser;
    private User user;

    //contructor to set latestCircleList and context for Adapter
    public CircleDisplayAdapter(Context context, List<Circle> circleList, User user) {
        this.context = context;
        this.circleList = circleList;
        this.user = user;
        circleJoinDialog = new Dialog(context);
        database = FirebaseDatabase.getInstance();

        currentUser = FirebaseAuth.getInstance();
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference().child("Users").child(currentUser.getCurrentUser().getUid());
    }

    @Override
    public CircleDisplayAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circle_card_display_view, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CircleDisplayAdapter.ViewHolder viewHolder, int i) {

        Circle current = circleList.get(i);

        //check if circle acceptance is review
        if (current.getAcceptanceType().equalsIgnoreCase("review"))
            viewHolder.join.setText("Apply");

        if (current.getApplicantsList() != null && current.getApplicantsList().keySet().contains(currentUser.getUid())) {
            viewHolder.join.setText("Pending Request");
            viewHolder.join.setBackground(context.getResources().getDrawable(R.drawable.unpressable_button));
        }

        GradientDrawable wbItemBackground = new GradientDrawable();
        wbItemBackground.setShape(GradientDrawable.RECTANGLE);
        wbItemBackground.setCornerRadius(30.0f);

        GradientDrawable moreMembersColor = new GradientDrawable();
        moreMembersColor.setShape(GradientDrawable.RECTANGLE);
        moreMembersColor.setCornerRadius(130.0f);

        GradientDrawable dividerColor = new GradientDrawable();
        dividerColor.setShape(GradientDrawable.LINE);

        String chipColor = "";

        switch (i % 3) {
            case 0:
                wbItemBackground.setColor(Color.parseColor("#A274FF"));
                moreMembersColor.setColor(Color.parseColor("#AE85FF"));
                dividerColor.setColor(Color.parseColor("#AE85FF"));
                chipColor = "#7344D4";
                break;
            case 1:
                wbItemBackground.setColor(Color.parseColor("#FE42AE"));
                moreMembersColor.setColor(Color.parseColor("#FF6DC1"));
                dividerColor.setColor(Color.parseColor("#FF6DC1"));
                chipColor = "#D42D8D";
                break;
            case 2:
                wbItemBackground.setColor(Color.parseColor("#3CD2C3"));
                moreMembersColor.setColor(Color.parseColor("#5FDFD2"));
                dividerColor.setColor(Color.parseColor("#5FDFD2"));
                chipColor = "#13A697";
                break;
        }

        //set the details of each circle to its respective card.
        viewHolder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        viewHolder.tv_circleName.setText(current.getName());
        viewHolder.tv_creatorName.setText(current.getCreatorName());
        viewHolder.tv_circleDesc.setText(current.getDescription());
        viewHolder.container.setBackground(wbItemBackground);
        viewHolder.membersCount.setBackground(moreMembersColor);
        viewHolder.divider.setBackground(dividerColor);

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(current.getTimestamp());
        String date = DateFormat.format("dd MMM, yyyy", cal).toString();
        viewHolder.tv_createdDate.setText(date);

        //set the chips
        if(current.getInterestTags().keySet().contains("sample")){
            if(current.getName().contains("Runner")){
                setInterestTag("running",viewHolder.circleDisplayTags, chipColor);
                setInterestTag(current.getCircleDistrict().replaceAll(" ", "") + "Running",viewHolder.circleDisplayTags, chipColor);
                setInterestTag("earlymorningrunning",viewHolder.circleDisplayTags, chipColor);
            } else if(current.getName().contains("Recipe")) {
                setInterestTag("cooking",viewHolder.circleDisplayTags, chipColor);
                setInterestTag(current.getCircleDistrict().replaceAll(" ", "") + "Recipes",viewHolder.circleDisplayTags, chipColor);
                setInterestTag("recipes",viewHolder.circleDisplayTags, chipColor);
            } else {
                setInterestTag("Welcome",viewHolder.circleDisplayTags, chipColor);
                setInterestTag("Introduction",viewHolder.circleDisplayTags, chipColor);
                setInterestTag("Tutorial",viewHolder.circleDisplayTags, chipColor);
                setInterestTag("Tutorial",viewHolder.circleDisplayTags, chipColor);
            }
        } else {
            for (String name : current.getInterestTags().keySet())
                setInterestTag(name, viewHolder.circleDisplayTags, chipColor);
        }


        viewHolder.join.setOnClickListener(view -> {
            if (current.getApplicantsList() != null && !current.getApplicantsList().keySet().contains(currentUser.getUid()))
                applyOrJoin(viewHolder, current);
            else if (current.getApplicantsList() == null)
                applyOrJoin(viewHolder, current);
        });

        viewHolder.share.setOnClickListener(view -> {
            showShareCirclePopup(current);
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
        private ChipGroup circleDisplayTags;
        private ImageButton share;
        private Button join, membersCount;
        private View divider;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            tv_createdDate = view.findViewById(R.id.circle_created_date);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            circleDisplayTags = view.findViewById(R.id.circle_display_tags);
            share = view.findViewById(R.id.circle_card_share);
            join = view.findViewById(R.id.circle_card_join);
            membersCount = view.findViewById(R.id.members_count_button);
            divider = view.findViewById(R.id.project_item_divider);
        }
    }

    public void setInterestTag(final String name, ChipGroup chipGroupLocation, String chipColor) {
        final Chip chip = new Chip(context);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                context.getResources().getDisplayMetrics()
        );
        chip.setRippleColor(ColorStateList.valueOf(Color.WHITE));
        chip.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3,
                        context.getResources().getDisplayMetrics()
                ),
                paddingDp, paddingDp, paddingDp);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.parseColor(chipColor),
                Color.parseColor(chipColor),
                Color.parseColor(chipColor),
                Color.parseColor(chipColor)
        };

        ColorStateList myList = new ColorStateList(states, colors);

        chip.setChipBackgroundColor(myList);
        chip.setChipCornerRadius(60);
        chip.setChipMinHeight(100);
        chip.setTextColor(Color.WHITE);
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);


        chipGroupLocation.addView(chip);
    }

    private void showShareCirclePopup(Circle c) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage = "\nLet me recommend you this application\n\n";
            //https://play.google.com/store/apps/details?id=
            shareMessage = "www.circleneighborhoodapp.com/" + c.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            context.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }

    private void applyOrJoin(ViewHolder viewHolder, final Circle circle) {

        circleJoinDialog.setContentView(R.layout.apply_popup_layout);
        Button closeDialogButton = circleJoinDialog.findViewById(R.id.completedDialogeDoneButton);
        TextView title = circleJoinDialog.findViewById(R.id.applyConfirmationTitle);
        TextView description = circleJoinDialog.findViewById(R.id.applyConfirmationDescription);

        Subscriber subscriber = new Subscriber(user.getUserId(), user.getFirstName() + " " + user.getLastName(),
                user.getProfileImageLink(), user.getToken_id(), System.currentTimeMillis());

        boolean adminCircle = false;
        if (circle.getId().equals("adminCircle")) {
            SessionStorage.saveCircle((Activity) context, circle);
            context.startActivity(new Intent(context, CircleWall.class));
            adminCircle = true;
            ((Activity) context).finish();
        } else {

            if (("review").equalsIgnoreCase(circle.getAcceptanceType())) {
                database.getReference().child("CirclePersonel").child(circle.getId()).child("applicants").child(user.getUserId()).setValue(subscriber);
                //adding userID to applicants list
                circlesDB.child(circle.getId()).child("applicantsList").child(user.getUserId()).setValue(true);
            } else if (("automatic").equalsIgnoreCase(circle.getAcceptanceType())) {
                database.getReference().child("CirclePersonel").child(circle.getId()).child("members").child(user.getUserId()).setValue(subscriber);
                //adding userID to members list in circlesReference
                circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).setValue(true);
                int nowActive = user.getActiveCircles() + 1;
                usersDB.child("activeCircles").setValue((nowActive));
            }
            circleJoinDialog.dismiss();

        }

        if (circle.getAcceptanceType().equalsIgnoreCase("review"))
            viewHolder.join.setText("Apply");
        else {
            title.setText("Successfully Joined!");
            description.setText("Congradulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Enjoy being part of this circle!");
        }

        circleJoinDialog.setOnDismissListener(dialogInterface -> {
        });

        closeDialogButton.setOnClickListener(view -> {
            SessionStorage.saveCircle((Activity) context, circle);

            context.startActivity(new Intent(context, CircleWall.class));
            circleJoinDialog.dismiss();
        });

        circleJoinDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (adminCircle == false) {
            circleJoinDialog.show();
        }
    }

    public void showCompletionDialog(){

    }

}