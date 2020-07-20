package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.LocalObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.MemberListAdapter;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.data.FBDatabaseReads.CirclePersonnelViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class CircleInformation extends AppCompatActivity {

    private ImageView banner;
    private CircleImageView logo;
    private TextView creatorName, circleName, circleDescription;
    private ListView membersDisplay;
    private List<Subscriber> memberList;
    private Circle circle;
    private LinearLayout noPermissionToViewMembers, noMembersDisplay;
    private User user;
    private ImageButton back;
    LiveData<String[]> liveData;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_information);

        circle = SessionStorage.getCircle(CircleInformation.this);
        user = SessionStorage.getUser(CircleInformation.this);

        banner = findViewById(R.id.circle_info_circle_banner);
        logo = findViewById(R.id.circle_info_circle_logo);
        creatorName = findViewById(R.id.circle_info_creator_name);
        circleName = findViewById(R.id.circle_info_circle_name);
        circleDescription = findViewById(R.id.circle_info_circle_description);
        membersDisplay = findViewById(R.id.circle_info_members_display);
        noPermissionToViewMembers = findViewById(R.id.circle_info_members_not_available);
        noMembersDisplay = findViewById(R.id.circle_info_empty_membersList);
        back = findViewById(R.id.bck_circle_information);

        creatorName.setText(circle.getCreatorName());
        circleName.setText(circle.getName());
        circleDescription.setText(circle.getDescription());

        char firstLetter = circle.getName().charAt(0);
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color = generator.getColor(circle.getName());
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter+"",color);
        //setting circle logo
        if (!circle.getBackgroundImageLink().equals("default")) {
            Glide.with(this).load(circle.getBackgroundImageLink()).into(logo);
        } else {
            logo.setBackground(drawable);
        }

        //back button
        back.setOnClickListener(view -> {
            int indexValue = getIntent().getIntExtra("exploreIndex", -1);
            if (indexValue == -1) {
                finishAfterTransition();
                startActivity(new Intent(CircleInformation.this, CircleWall.class));
            } else {
                finishAfterTransition();
                Intent intent = new Intent(CircleInformation.this, ExploreTabbedActivity.class);
                intent.putExtra("exploreIndex", indexValue);
                startActivity(intent);
            }
        });

        //setting circle banner
        setBannerBackground(circle.getCategory());

        //setting members display
        if (circle.getMembersList() != null && circle.getMembersList().keySet().contains(user.getUserId())) {
            noPermissionToViewMembers.setVisibility(View.GONE);
            loadMembersList();
        } else if (circle.getAcceptanceType().equalsIgnoreCase("review")) {
            noMembersDisplay.setVisibility(View.GONE);
            noPermissionToViewMembers.setVisibility(View.VISIBLE);
        } else {
            noMembersDisplay.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onPause() {
        if(liveData!=null)
        liveData.removeObservers(this);
        super.onPause();
    }

    private void loadMembersList() {
        memberList = new ArrayList<>(); //initialize membersList
        final MemberListAdapter adapter = new MemberListAdapter(this, memberList);
        membersDisplay.setAdapter(adapter);

        CirclePersonnelViewModel viewModel = ViewModelProviders.of(this).get(CirclePersonnelViewModel.class);

        liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

        liveData.observe(this, returnArray -> {
            Subscriber subscriber = new Gson().fromJson(returnArray[0], Subscriber.class);
            memberList.add(subscriber);
            adapter.notifyDataSetChanged();
            HelperMethods.setListViewHeightBasedOnChildren(membersDisplay);

        });
    }

    private void setBannerBackground(String circleCategory) {
        switch (circleCategory) {
            case "Events":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_events)).centerCrop().into(banner);
                break;
            case "Apartments & Communities":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_apartment_and_communities)).centerCrop().into(banner);
                break;
            case "Sports":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_sports)).centerCrop().into(banner);
                break;
            case "Friends & Family":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_friends_and_family)).centerCrop().into(banner);
                break;
            case "Food & Entertainment":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_food_and_entertainment)).centerCrop().into(banner);
                break;
            case "Science & Tech":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_science_and_tech_background)).centerCrop().into(banner);
                break;
            case "Gaming":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_gaming)).centerCrop().into(banner);
                break;
            case "Health & Fitness":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_health_and_fitness)).centerCrop().into(banner);
                break;
            case "Students & Clubs":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_students_and_clubs)).centerCrop().into(banner);
                break;
            case "The Circle App":
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.admin_circle_banner)).centerCrop().into(banner);
                break;
            default:
                Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.banner_custom_circle)).centerCrop().into(banner);
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        int indexValue = getIntent().getIntExtra("exploreIndex", -1);
        if (indexValue == -1) {
            finishAfterTransition();
            startActivity(new Intent(CircleInformation.this, CircleWall.class));
        } else {
            finishAfterTransition();
            Intent intent = new Intent(CircleInformation.this, ExploreTabbedActivity.class);
            intent.putExtra("exploreIndex", indexValue);
            startActivity(intent);
        }
    }
}