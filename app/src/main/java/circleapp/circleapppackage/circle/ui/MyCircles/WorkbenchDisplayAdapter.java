package circleapp.circleapppackage.circle.ui.MyCircles;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circleapppackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circleapppackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;
    private GlobalVariables globalVariables = new GlobalVariables();

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull WorkbenchDisplayAdapter.ViewHolder holder, int position) {

        Circle circle = MycircleList.get(position);
        User user = globalVariables.getCurrentUser();
        HelperMethodsUI.createDefaultCircleIcon(circle,context,holder.backgroundPic);

        setUIElements(holder,circle,user);
        setNewApplicantsIndicator(holder,circle,user);
        setNewNotifsIndicator(holder,circle,user);
        setButtonListeners(holder,circle,user);
    }

    private void setUIElements(WorkbenchDisplayAdapter.ViewHolder holder, Circle circle, User user){
        holder.tv_MycircleName.setText(circle.getName());
        if(circle.getLastActivityTimeStamp()!=0){
            String message = HelperMethodsUI.getTimeElapsed(System.currentTimeMillis(),circle.getLastActivityTimeStamp());
            holder.lastSeenActivity.setText("Last Active: " + message);
        }
    }

    private void setNewApplicantsIndicator(WorkbenchDisplayAdapter.ViewHolder holder, Circle circle, User user){
        //setting new applicants
        if (HelperMethodsUI.numberOfApplicants(circle, user) > 0) {
            holder.newApplicantsDisplay.setVisibility(View.VISIBLE);
            Typeface typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
            holder.tv_MycircleName.setTypeface(typeface);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setNewNotifsIndicator(WorkbenchDisplayAdapter.ViewHolder holder, Circle circle, User user){
        //read for new notifs and set counter
        int newNotifs = HelperMethodsUI.newNotifications(circle, user);
        int newCommentsNotif = HelperMethodsUI.newCommentNotifications(circle, user);
        if (newNotifs > 0) {
            holder.newActivityDisplay.setVisibility(View.VISIBLE);
            Typeface typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
            holder.tv_MycircleName.setTypeface(typeface);
        }
        else if(newCommentsNotif>0){
            holder.newActivityDisplay.setVisibility(View.VISIBLE);
            Typeface typeface = ResourcesCompat.getFont(context, R.font.roboto_bold);
            holder.tv_MycircleName.setTypeface(typeface);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setButtonListeners(WorkbenchDisplayAdapter.ViewHolder holder, Circle circle, User user){
        //Read notification count updated on going to circle wall
        holder.container.setOnClickListener(view -> {
            clearNotifications(user, circle);
            globalVariables.saveCurrentCircle(circle);
            //If user enters circle wall for first time
            actionOnFirstTimeEntry();
        });

        //bring up share bottom sheet
        holder.shareCirclesButton.setOnClickListener(view -> {
            globalVariables.saveCurrentCircle(circle);
            InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
            bottomSheet.show((((FragmentActivity) context).getSupportFragmentManager()), "exampleBottomSheet");
        });
    }

    private void clearNotifications(User user, Circle circle){
        HashMap<String, Integer> notifStore;
        if (user.getNotificationsAlert() != null) { //if the user has notification info from other circles

            notifStore = new HashMap<>(user.getNotificationsAlert());
            notifStore.put(circle.getId(), circle.getNoOfBroadcasts());

        } else { //first time when a user is opening any circle

            notifStore = new HashMap<>();
            notifStore.put(circle.getId(), circle.getNoOfBroadcasts());
        }

        //Save user
        UserRepository userRepository = new UserRepository();
        userRepository.updateUserNotifsIndicator(user, notifStore);
        globalVariables.saveCurrentCircle(circle);
        user.setNotificationsAlert(notifStore);
        globalVariables.saveCurrentUser(user);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void actionOnFirstTimeEntry(){
        SharedPreferences prefs = context.getSharedPreferences("com.mycompany.myAppName", context.MODE_PRIVATE);
        if (prefs.getBoolean("firstWall", true)) {
            context.startActivity(new Intent(context, CircleWallBackgroundPicker.class));
            ((Activity) context).finishAfterTransition();
            prefs.edit().putBoolean("firstWall", false).commit();
        } else {
            context.startActivity(new Intent(context, CircleWall.class));
            ((Activity) context).finishAfterTransition();
        }
    }


    @Override
    public int getItemCount() {
        return MycircleList.size();
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_MycircleName, lastSeenActivity;
        private CircleImageView backgroundPic, newActivityDisplay, newApplicantsDisplay;
        private LinearLayout container;
        private ImageButton shareCirclesButton;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircleName);
            lastSeenActivity = view.findViewById(R.id.last_activity_circle);
            shareCirclesButton = view.findViewById(R.id.wb_share_circle_button);
            backgroundPic = view.findViewById(R.id.background_image_workbench);
            newActivityDisplay = view.findViewById(R.id.new_activity_indicator);
            newApplicantsDisplay = view.findViewById(R.id.new_applicants_indicator);
        }
    }

}