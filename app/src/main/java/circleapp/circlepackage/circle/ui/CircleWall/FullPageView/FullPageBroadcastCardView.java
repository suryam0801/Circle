package circleapp.circlepackage.circle.ui.CircleWall.FullPageView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.List;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circlepackage.circle.ui.CircleWall.CircleInformation;
import circleapp.circlepackage.circle.ui.CircleWall.CircleWallBackgroundPicker;
import circleapp.circlepackage.circle.ui.CircleWall.InviteFriendsBottomSheet;
import circleapp.circlepackage.circle.ui.PersonelDisplay.PersonelDisplay;

public class FullPageBroadcastCardView extends AppCompatActivity implements InviteFriendsBottomSheet.BottomSheetListener {

    private Circle circle;
    private List<Broadcast> broadcastList;
    private int initialBroadcastPosition;
    private TextView banner;
    private ImageButton back;
    private LinearLayout parentLayout;
    private ImageButton moreOptions, viewApplicants;
    private RecyclerView recyclerView;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Dialog reportAbuseDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_broadcast_card_view);

        setUIElements();
        setParentBgImage();
        //Go back to home
        back.setOnClickListener(view -> {
            onBackPressed();
        });
        //Only for creator
        viewApplicants.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(getApplicationContext(), PersonelDisplay.class));
        });
        //Drop down menu
        moreOptions.setOnClickListener(view -> {
            setPopupMenu();
        });
        //Snapping the recyclerview
        SnapHelper snapHelper = new PagerSnapHelper();
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        final RecyclerView.Adapter adapter = new FullPageBroadcastCardAdapter(this, broadcastList, circle, initialBroadcastPosition);
        recyclerView.setAdapter(adapter);

        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.scrollToPosition(initialBroadcastPosition);
    }
    private void setUIElements(){
        reportAbuseDialog = new Dialog(this);
        recyclerView = findViewById(R.id.full_page_broadcast_card_recycler_view);
        banner = findViewById(R.id.full_page_broadcast_banner_name);
        back = findViewById(R.id.bck_fullpage_broadcast);
        parentLayout = findViewById(R.id.full_page_broadcast_parent_layout);
        moreOptions = findViewById(R.id.full_page_broadcast_more_options);
        viewApplicants = findViewById(R.id.full_page_broadcast_applicants_display_creator);

        broadcastList = globalVariables.getCurrentBroadcastList();
        circle = globalVariables.getCurrentCircle();
        initialBroadcastPosition = getIntent().getIntExtra("broadcastPosition", 0);
        //set applicants button visible
        if (circle.getCreatorID().equals(globalVariables.getCurrentUser().getUserId()))
            viewApplicants.setVisibility(View.VISIBLE);
        banner.setText(circle.getName());

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setPopupMenu(){
        PopupMenu popup = new PopupMenu(this, moreOptions);
        popup.getMenuInflater()
                .inflate(R.menu.circle_wall_menu, popup.getMenu());
        if (circle.getCreatorID().equals(globalVariables.getCurrentUser().getUserId()))
            popup.getMenu().findItem(R.id.deleteCircleMenuBar).setVisible(true);
        else
            popup.getMenu().findItem(R.id.exitCircleMenuBar).setVisible(true);
        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Change wallpaper":
                    finishAfterTransition();
                    startActivity(new Intent(this, CircleWallBackgroundPicker.class));
                    break;
                case "Invite a friend":
                    InviteFriendsBottomSheet bottomSheet = new InviteFriendsBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
                    break;
                case "Report Abuse":
                    HelperMethodsUI.showReportAbusePopup(reportAbuseDialog, this, circle.getId(), "", "", circle.getCreatorID(), globalVariables.getCurrentUser().getUserId());
                    break;
                case "Exit circle":
                    HelperMethodsUI.showExitDialog(this, circle, globalVariables.getCurrentUser());
                    break;
                case "Delete circle":
                    HelperMethodsUI.showDeleteDialog(this, circle, globalVariables.getCurrentUser());
                    break;
                case "Circle Information":
                    startActivity(new Intent(this, CircleInformation.class));
                    break;
            }
            return true;
        });
        popup.show();
    }
    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "shareLink":
                HelperMethodsUI.showShareCirclePopup(circle, this);
                break;
            case "copyLink":
                HelperMethodsUI.copyLinkToClipBoard(circle, this);
                break;
        }
    }


    public void setParentBgImage() {
        String bg = SessionStorage.getCircleWallBgImage(this);
        if (bg != null) {
            switch (bg) {
                case "bg1":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_1));
                    break;
                case "bg2":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_2));
                    break;
                case "bg3":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_3));
                    break;
                case "bg4":
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_4));
                    break;
                case "bg5":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_5));
                    break;
                case "bg6":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_6));
                    break;
                case "bg7":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_7));
                    break;
                case "bg8":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_8));
                    break;
                case "bg9":
                    banner.setTextColor(Color.WHITE);
                    back.setImageResource(R.drawable.ic_chevron_left_white_24dp);
                    moreOptions.setImageResource(R.drawable.ic_baseline_more_white_vert_24);
                    viewApplicants.setImageResource(R.drawable.ic_baseline_group_white_18);
                    parentLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_9));
                    break;
                case "bg10":
                    banner.setTextColor(Color.BLACK);
                    parentLayout.setBackgroundColor(Color.WHITE);
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        recyclerView.removeAllViews();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(getApplicationContext(), CircleWall.class));
    }
}
