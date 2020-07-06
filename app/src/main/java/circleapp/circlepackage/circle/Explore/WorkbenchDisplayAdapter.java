package circleapp.circlepackage.circle.Explore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;
    int noOfUnreadDiscussions, tempUserRead;

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

        if (!circle.getBackgroundImageLink().equals("default"))
            Glide.with(context).load(circle.getBackgroundImageLink()).into(holder.backgroundPic);
        else {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
            Glide.with(context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(holder.backgroundPic);
        }

        //set the details of each circle to its respective card.
        //holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.tv_MycircleName.setText(circle.getName());
        if (circle.getCreatorID().equals(user.getUserId()))
            holder.tv_circleCreatorName.setText("Me");
        holder.tv_circleCreatorName.setText(circle.getCreatorName());


        //setting new applicants
        if (HelperMethods.numberOfApplicants(circle, user) > 0) {
            GradientDrawable itemBackgroundApplicant = HelperMethods.gradientRectangleDrawableSetter(80);
            itemBackgroundApplicant.setColor(context.getResources().getColor(R.color.request_alert_color));
            holder.newApplicantsDisplay.setVisibility(View.VISIBLE);
            holder.newApplicantsDisplay.setBackground(itemBackgroundApplicant);
            holder.newApplicantsDisplay.setText(Integer.toString(circle.getApplicantsList().size()));
        }

        //read for new notifs
        int newNotifs = HelperMethods.newNotifications(circle, user);
        if (newNotifs > 0) {
            GradientDrawable itemBackgroundNotif = HelperMethods.gradientRectangleDrawableSetter(80);
            itemBackgroundNotif.setColor(context.getResources().getColor(R.color.broadcast_alert_color));
            holder.newNotifAlert.setText(newNotifs + "");
            holder.newNotifAlert.setBackground(itemBackgroundNotif);
            holder.newNotifAlert.setVisibility(View.VISIBLE);
        }

        holder.container.setOnClickListener(view -> {
            if (user.getNotificationsAlert() != null) { //if the user has notification info from other circles

                HashMap<String, Integer> tempUserNotifStore = new HashMap<>(user.getNotificationsAlert());
                tempUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(tempUserNotifStore);

            } else { //first time when a user is opening any circle

                HashMap<String, Integer> newUserNotifStore = new HashMap<>();
                newUserNotifStore.put(circle.getId(), circle.getNoOfBroadcasts());
                user.setNotificationsAlert(newUserNotifStore);
            }

            FirebaseWriteHelper.updateUser(user, context);

            SessionStorage.saveCircle((Activity) context, circle);
            SessionStorage.saveUser((Activity) context, user);

            SharedPreferences prefs = context.getSharedPreferences("com.mycompany.myAppName", context.MODE_PRIVATE);
            if (prefs.getBoolean("firstWall", true)) {
                context.startActivity(new Intent(context, CircleWallBackgroundPicker.class));
                ((Activity) context).finish();
                prefs.edit().putBoolean("firstWall", false).commit();
            } else {
                context.startActivity(new Intent(context, CircleWall.class));
                ((Activity) context).finish();
            }
        });

        //update new notifs value
        holder.shareCirclesLayout.setOnClickListener(view -> {
            SessionStorage.saveCircle((Activity) context, circle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity) context).getSupportFragmentManager()), "exampleBottomSheet");
        });

        holder.shareCirclesButton.setOnClickListener(view -> {
            SessionStorage.saveCircle((Activity) context, circle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity) context).getSupportFragmentManager()), "exampleBottomSheet");
        });

        String timeElapsed = HelperMethods.getTimeElapsed(System.currentTimeMillis(), circle.getTimestamp());
        holder.tv_circleCreatedDateWB.setText("Joined " + timeElapsed);

        holder.categoryDisplay.setText(circle.getCategory());

        noOfUnreadDiscussions = 0;
        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of((FragmentActivity) context).get(FirebaseRetrievalViewModel.class);
        LiveData<String[]> liveData = viewModel.getDataSnapsBroadcastLiveData(circle.getId());
        liveData.observe((LifecycleOwner) context, returnArray -> {
            Broadcast broadcast = new Gson().fromJson(returnArray[0], Broadcast.class);
            if(user.getNoOfReadDiscussions().get(broadcast.getId())==null)
                tempUserRead = 0;
            else
                tempUserRead = user.getNoOfReadDiscussions().get(broadcast.getId());
            if (!user.getMutedBroadcasts().contains(broadcast.getId()))
                noOfUnreadDiscussions = noOfUnreadDiscussions + broadcast.getNumberOfComments()- tempUserRead;
            Log.d("noOfComments", noOfUnreadDiscussions+"");
            if(noOfUnreadDiscussions!=0){
                GradientDrawable itemBackgroundNotif = HelperMethods.gradientRectangleDrawableSetter(80);
                itemBackgroundNotif.setColor(context.getResources().getColor(R.color.comment_alert_color));
                holder.newDiscussionDisplay.setVisibility(View.VISIBLE);
                holder.newDiscussionDisplay.setBackground(itemBackgroundNotif);
                holder.newDiscussionDisplay.setText(noOfUnreadDiscussions+"");
            }
            else
                holder.newDiscussionDisplay.setVisibility(View.GONE);
        });
    }


    @Override
    public int getItemCount() {
        return MycircleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_MycircleName, tv_circleCreatorName, tv_circleCreatedDateWB,
                newNotifAlert, newApplicantsDisplay, newDiscussionDisplay, categoryDisplay;
        private CircleImageView backgroundPic;
        private LinearLayout container;
        private RelativeLayout shareCirclesLayout;
        private ImageButton shareCirclesButton;

        public ViewHolder(View view) {
            super(view);
            newApplicantsDisplay = view.findViewById(R.id.newApplicantsDisplay);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircleName);
            tv_circleCreatorName = view.findViewById(R.id.wbcircle_creatorName);
            shareCirclesLayout = view.findViewById(R.id.wb_share_circle_button_layout);
            shareCirclesButton = view.findViewById(R.id.wb_share_circle_button);
            tv_circleCreatedDateWB = view.findViewById(R.id.workbench_circle_created_date);
            newNotifAlert = view.findViewById(R.id.newNotifAlertTV);
            newDiscussionDisplay = view.findViewById(R.id.newDiscussionDisplay);
            categoryDisplay = view.findViewById(R.id.workbench_circle_category_display);
            backgroundPic = view.findViewById(R.id.background_image_workbench);
        }
    }

}