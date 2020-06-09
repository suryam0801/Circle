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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class WorkbenchDisplayAdapter extends RecyclerView.Adapter<WorkbenchDisplayAdapter.ViewHolder> {

    private List<Circle> MycircleList;
    private Context context;

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
                break;
            case 1:
                wbLayoutBackground.setColor(Color.parseColor("#FFD1E9"));
                wbLineBackground.setColor(Color.parseColor("#FF38A2"));
                wbButtonBackground.setColor(Color.parseColor("#FF38A2"));
                wbShareButtonBackground.setColor(Color.parseColor("#FF38A2"));
                holder.tv_MycircleName.setTextColor(Color.parseColor("#FF38A2"));
                holder.tv_circleCreatorName.setTextColor(Color.parseColor("#FF38A2"));
                holder.tv_circleCreatedDateWB.setTextColor(Color.parseColor("#FF38A2"));

                break;
            case 2:
                wbLayoutBackground.setColor(Color.parseColor("#FFDDBB"));
                wbLineBackground.setColor(Color.parseColor("#FF9C38"));
                wbButtonBackground.setColor(Color.parseColor("#FF9C38"));
                wbShareButtonBackground.setColor(Color.parseColor("#FF9C38"));
                holder.tv_MycircleName.setTextColor(Color.parseColor("#FF9C38"));
                holder.tv_circleCreatorName.setTextColor(Color.parseColor("#FF9C38"));
                holder.tv_circleCreatedDateWB.setTextColor(Color.parseColor("#FF9C38"));
                break;
        }

        //set the details of each circle to its respective card.
        holder.container.setAnimation(AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
        holder.container.setBackground(wbLayoutBackground);
        holder.divider.setBackground(wbLineBackground);
        holder.buttonContainer.setBackground(wbShareButtonBackground);
        holder.circleWallNav.setBackground(wbButtonBackground);
        holder.tv_MycircleName.setText(circle.getName());
        holder.tv_circleCreatorName.setText(circle.getCreatorName());

        holder.shareCircles.setOnClickListener(view -> showShareCirclePopup(circle));

        holder.circleWallNav.setOnClickListener(view -> {
            if (user.getNotificationsAlert() != null) {
                HashMap<String, Long> notifHashMap = new HashMap<>(user.getNotificationsAlert());
                notifHashMap.put(circle.getId(), System.currentTimeMillis());
                user.setNotificationsAlert(notifHashMap);
                SessionStorage.saveUser((Activity) context, user);
            } else {
                HashMap<String, Long> notifHashMap = new HashMap<>();
                notifHashMap.put(circle.getId(), System.currentTimeMillis());
                user.setNotificationsAlert(notifHashMap);
                SessionStorage.saveUser((Activity) context, user);
            }

            SessionStorage.saveCircle((Activity) context, circle);
            context.startActivity(new Intent(context, CircleWall.class));
        });

        // comparator
        if (user.getNotificationsAlert() != null && user.getNotificationsAlert().containsKey(circle.getId())) {
            HashMap<String, Long> timeStamps = user.getNotificationsAlert();
            long currentUserTimestamp = timeStamps.get(circle.getId());
            if (currentUserTimestamp < circle.getNotificationTimeStamp())
                holder.newNotifAlert.setVisibility(View.VISIBLE);
        } else {

        }

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
        private TextView tv_MycircleName, tv_circleCreatorName, tv_circleCreatedDateWB, newNotifAlert;
        private LinearLayout container, buttonContainer;
        private ImageButton shareCircles;
        private Button circleWallNav;

        private View divider;

        public ViewHolder(View view) {
            super(view);
            container = view.findViewById(R.id.wbContainer);
            tv_MycircleName = view.findViewById(R.id.wbcircleName);
            tv_circleCreatorName = view.findViewById(R.id.wbcircle_creatorName);
            shareCircles = view.findViewById(R.id.wb_share_circle_button);
            tv_circleCreatedDateWB = view.findViewById(R.id.circle_created_date);
            newNotifAlert = view.findViewById(R.id.newNotifAlertTV);
            circleWallNav = view.findViewById(R.id.wb_go_to_wall);
            divider = view.findViewById(R.id.wb_divider_line);
            buttonContainer = view.findViewById(R.id.button_background_wb);
        }
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

}
