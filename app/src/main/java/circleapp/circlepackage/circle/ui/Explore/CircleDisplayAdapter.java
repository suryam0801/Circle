package circleapp.circlepackage.circle.ui.Explore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

import circleapp.circlepackage.circle.DataLayer.CirclePersonnelRepository;
import circleapp.circlepackage.circle.ui.CircleWall.CircleInformation;
import circleapp.circlepackage.circle.ui.CircleWall.CircleWall;
import circleapp.circlepackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class CircleDisplayAdapter extends RecyclerView.Adapter<CircleDisplayAdapter.ViewHolder> {
    private List<Circle> circleList;
    private Context context;
    private Dialog circleJoinDialog;
    private User user;
    private GlobalVariables globalVariables = new GlobalVariables();

    public CircleDisplayAdapter() {
    }

    //contructor to set latestCircleList and context for Adapter
    public CircleDisplayAdapter(Context context, List<Circle> circleList, User user) {
        this.context = context;
        this.circleList = circleList;
        this.user = user;
        circleJoinDialog = new Dialog(context);
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
        Log.d("efljknwefwe", currentCircle.toString());
        HelperMethodsUI.createDefaultCircleIcon(currentCircle,context,viewHolder.circleLogo);


        //check if circle acceptance is review
        if (currentCircle.getAcceptanceType().equalsIgnoreCase("review"))
            viewHolder.join.setText("Apply");
        //check if user has already applied to the circle
        boolean isApplicant = HelperMethodsUI.ifUserApplied(currentCircle, user.getUserId());
        if (isApplicant) {
            viewHolder.join.setText("Pending Approval");
            viewHolder.join.setBackground(context.getResources().getDrawable(R.drawable.unpressable_button));
            viewHolder.join.setTextColor(Color.parseColor("#828282"));
        }

        //set the details of each circle to its respective card.
        viewHolder.tv_circleName.setText(currentCircle.getName());
        viewHolder.tv_creatorName.setText(currentCircle.getCreatorName());
        viewHolder.tv_circleDesc.setText(currentCircle.getDescription());

        String date = HelperMethodsUI.convertIntoDateFormat("dd MMM, yyyy", currentCircle.getTimestamp());
        viewHolder.tv_createdDate.setText(date);

        //onclick for join and share
        viewHolder.join.setOnClickListener(view ->
        {
            if (!isApplicant)
                applyOrJoin(currentCircle);
            else if (currentCircle.getApplicantsList() == null)
                applyOrJoin(currentCircle);
        });

        //bring up sharelink
        viewHolder.shareLayout.setOnClickListener(view -> {
            globalVariables.saveCurrentCircle(currentCircle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity) context).getSupportFragmentManager()), "exampleBottomSheet");
        });
        //bring up share link
        viewHolder.shareButton.setOnClickListener(view -> {
            globalVariables.saveCurrentCircle(currentCircle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity) context).getSupportFragmentManager()), "exampleBottomSheet");
        });
        //Open circle information
        viewHolder.container.setOnClickListener(view -> {
            globalVariables.saveCurrentCircle(currentCircle);
            Intent intent = new Intent(context, CircleInformation.class);
            intent.putExtra("exploreIndex", i);
            context.startActivity(intent);
        });

        viewHolder.categoryDisplay.setText(currentCircle.getCategory());
        circleCategory = currentCircle.getCategory();

        setBannerBackground(circleCategory, viewHolder);
    }

    private void setBannerBackground(String circleCategory, ViewHolder viewHolder) {
        switch (circleCategory) {
            case "Events":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_events)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Apartments & Communities":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_apartment_and_communities)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Sports":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_sports)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Friends & Family":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_friends_and_family)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Food & Entertainment":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_food_and_entertainment)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Science & Tech":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_science_and_tech_background)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Gaming":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_gaming)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Health & Fitness":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_health_and_fitness)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "Students & Clubs":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_students_and_clubs)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "The Circle App":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.admin_circle_banner)).centerCrop().into(viewHolder.bannerImage);
                break;
            case "General":
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_general)).centerCrop().into(viewHolder.bannerImage);
                break;
            default:
                Glide.with(context).load(ContextCompat.getDrawable(context, R.drawable.banner_custom_circle)).centerCrop().into(viewHolder.bannerImage);
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
        private ImageView bannerImage;
        private ImageButton shareButton, reportAbuseBroadcast;
        ;
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

        Subscriber subscriber = new Subscriber(user, System.currentTimeMillis());
        CirclePersonnelRepository circlePersonnelRepository = new CirclePersonnelRepository();

        circlePersonnelRepository.applyOrJoin(circle, user, subscriber);
        SendNotification.sendApplication("new_applicant", user, circle, subscriber);
        if (circle.getAcceptanceType().equalsIgnoreCase("automatic")) {
            title.setText("Successfully Joined!");
            description.setText("Congratulations! You are now an honorary member of " + circle.getName() + ". You can view and get access to your circle from your wall. Enjoy being part of this circle!");
        }

        closeDialogButton.setOnClickListener(view -> {
            if (circle.getAcceptanceType().equalsIgnoreCase("review")) {
                circleJoinDialog.dismiss();
            } else {
                globalVariables.saveCurrentCircle(circle);
                context.startActivity(new Intent(context, CircleWall.class));
                ((Activity) context).finish();
                circleJoinDialog.dismiss();
            }
        });

        circleJoinDialog.show();

    }

}
