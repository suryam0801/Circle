package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Explore.CircleDisplayAdapter;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.MemberListAdapter;
import circleapp.circlepackage.circle.R;
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

        //setting circle logo
        if (!circle.getBackgroundImageLink().equals("default")) {
            Glide.with(this).load(circle.getBackgroundImageLink()).into(logo);
        } else {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_circle_logo));
            Glide.with(this)
                    .load(ContextCompat.getDrawable(this, profilePic))
                    .into(logo);
        }

        //back button
        back.setOnClickListener(view -> {
            int indexValue = getIntent().getIntExtra("exploreIndex", -1);
            if (indexValue == -1) {
                startActivity(new Intent(CircleInformation.this, CircleWall.class));
                finish();
            } else {
                Intent intent = new Intent(CircleInformation.this, ExploreTabbedActivity.class);
                intent.putExtra("exploreIndex", indexValue);
                startActivity(intent);
                finish();
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

    private void loadMembersList() {
        memberList = new ArrayList<>(); //initialize membersList
        final MemberListAdapter adapter = new MemberListAdapter(this, memberList);
        membersDisplay.setAdapter(adapter);

        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of(this).get(FirebaseRetrievalViewModel.class);

        LiveData<String[]> liveData = viewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");

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
}