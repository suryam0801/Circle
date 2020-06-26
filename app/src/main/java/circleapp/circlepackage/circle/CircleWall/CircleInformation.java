package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Explore.CircleDisplayAdapter;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.PersonelDisplay.MemberListAdapter;
import circleapp.circlepackage.circle.R;

public class CircleInformation extends AppCompatActivity {

    private ImageView banner, logo;
    private TextView creatorName, circleName, circleDescription;
    private RecyclerView membersDisplay;
    private FirebaseDatabase database;
    private DatabaseReference circlesPersonelDB;
    private List<Subscriber> memberList;
    private Circle circle;
    private LinearLayout noPermissionToViewMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_information);

        circle = SessionStorage.getCircle(CircleInformation.this);

        database = FirebaseDatabase.getInstance();
        circlesPersonelDB = database.getReference("CirclePersonel").child(circle.getId()); //circle.getId()

        banner = findViewById(R.id.circle_info_circle_banner);
        logo = findViewById(R.id.circle_info_circle_logo);
        creatorName = findViewById(R.id.circle_info_creator_name);
        circleName = findViewById(R.id.circle_info_circle_name);
        circleDescription = findViewById(R.id.circle_info_circle_description);
        membersDisplay = findViewById(R.id.circle_info_members_display);
        noPermissionToViewMembers = findViewById(R.id.circle_info_members_not_available);

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

        //setting circle banner
        setBannerBackground(circle.getCategory());


        //setting members display
        if (!circle.getMembersList().keySet().contains(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            noPermissionToViewMembers.setVisibility(View.VISIBLE);
        else if (circle.getMembersList() != null && circle.getMembersList().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()))
            loadMembersList();

    }

    private void loadMembersList() {
        membersDisplay.setLayoutManager(new LinearLayoutManager(this));
        memberList = new ArrayList<>(); //initialize membersList
        final RecyclerView.Adapter adapter = new MemberListAdapter(this, memberList);
        membersDisplay.setAdapter(adapter);

        circlesPersonelDB.child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                memberList.add(subscriber);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Subscriber subscriber = dataSnapshot.getValue(Subscriber.class);
                List<Subscriber> tempList = new ArrayList<>(memberList);
                int position = HelperMethods.returnIndexOfSubscriber(tempList, subscriber);
                //when data is changed, check if object already exists. If exists delete and rewrite it to avoid duplicates.
                memberList.remove(position);
                adapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
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