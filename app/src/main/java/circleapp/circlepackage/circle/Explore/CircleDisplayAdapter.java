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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

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
        String circleCategory;
        if(!currentCircle.getBackgroundImageLink().equals("default"))
            Glide.with(context).load(currentCircle.getBackgroundImageLink()).into(viewHolder.circleLogo);
        else
        {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
            Glide.with(context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(viewHolder.circleLogo);
        }

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

        viewHolder.shareLayout.setOnClickListener(view -> {
            SessionStorage.saveCircle((Activity) context, currentCircle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity)context).getSupportFragmentManager()), "exampleBottomSheet");
        });
        viewHolder.shareButton.setOnClickListener(view -> {
            SessionStorage.saveCircle((Activity) context, currentCircle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity)context).getSupportFragmentManager()), "exampleBottomSheet");
        });

        viewHolder.categoryDisplay.setText(currentCircle.getCategory());
        circleCategory = currentCircle.getCategory();
        switch (circleCategory){
            case "Events":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_events);
                break;
            case "Apartments & Communities":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_apartment_and_communities);
                break;
            case "Sports":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_sports);
                break;
            case "Friends & Family":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_friends_and_family);
                break;
            case "Food & Entertainment":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_food_and_entertainment);
                break;
            case "Science & Tech":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_science_and_tech_background);
                break;
            case "Gaming":
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_gaming);
                break;
            default:
                viewHolder.bannerImage.setBackgroundResource(R.drawable.banner_own_circle);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return circleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_circleName, tv_creatorName, tv_circleDesc, tv_createdDate, categoryDisplay;
        private LinearLayout container, shareLayout;
        private RelativeLayout bannerImage;
        private ImageButton shareButton;
        private Button join;
        CircleImageView circleLogo;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.container);
            tv_createdDate = view.findViewById(R.id.explore_circle_created_date);
            tv_circleName = view.findViewById(R.id.circle_name);
            tv_creatorName = view.findViewById(R.id.circle_creatorName);
            tv_circleDesc = view.findViewById(R.id.circle_desc);
            shareLayout = view.findViewById(R.id.circle_card_share_layout);
            shareButton = view.findViewById(R.id.circle_card_share_button);
            join = view.findViewById(R.id.circle_card_join);
            categoryDisplay = view.findViewById(R.id.circle_category);
            circleLogo = view.findViewById(R.id.explore_circle_logo);
            bannerImage = view.findViewById(R.id.circle_banner_image);
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
