package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.List;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;

public class FullPageBroadcastCardView extends AppCompatActivity {

    private Circle circle;
    private List<Broadcast> broadcastList;
    int initialBroadcastPosition;
    private TextView banner;
    private ImageButton back;
    private LinearLayout parentLayout;
    private ImageButton moreOptions, viewApplicants;
    private RecyclerView recyclerView;
    private GlobalVariables globalVariables = new GlobalVariables();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_broadcast_card_view);

        recyclerView = findViewById(R.id.full_page_broadcast_card_recycler_view);
        banner = findViewById(R.id.full_page_broadcast_banner_name);
        back = findViewById(R.id.bck_fullpage_broadcast);
        parentLayout = findViewById(R.id.full_page_broadcast_parent_layout);
        moreOptions = findViewById(R.id.full_page_broadcast_more_options);
        viewApplicants = findViewById(R.id.full_page_broadcast_applicants_display_creator);

        broadcastList = globalVariables.getCurrentBroadcastList();
        circle = globalVariables.getCurrentCircle();
        initialBroadcastPosition = getIntent().getIntExtra("broadcastPosition", 0);


        setParentBgImage();

        banner.setText(circle.getName());
        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(FullPageBroadcastCardView.this, ExploreTabbedActivity.class));
        });
        //set applicants button visible
        if (circle.getCreatorID().equals(globalVariables.getCurrentUser().getUserId()))
            viewApplicants.setVisibility(View.VISIBLE);

        viewApplicants.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(this, PersonelDisplay.class));
        });

        moreOptions.setOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(this, moreOptions);
            popup.getMenuInflater()
                    .inflate(R.menu.circle_wall_menu, popup.getMenu());
            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "Change wallpaper":
                        finishAfterTransition();
                        startActivity(new Intent(this, CircleWallBackgroundPicker.class));
                        break;
                    case "Invite a friend":
                        HelperMethods.showShareCirclePopup(circle, this);
                        break;
                    case "Report Abuse":
                        break;
                    case "Exit circle":
                        break;
                    case "Delete circle":
                        break;
                    case "Circle Information":
                        break;
                }
                return true;
            });
            popup.show();
        });


        SnapHelper snapHelper = new PagerSnapHelper();
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        final RecyclerView.Adapter adapter = new FullPageBroadcastCardAdapter(this, broadcastList, circle, initialBroadcastPosition);
        recyclerView.setAdapter(adapter);

        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.smoothScrollToPosition(initialBroadcastPosition);
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
