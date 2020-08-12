package circleapp.circlepackage.circle.ui.PersonelDisplay;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.CreateCircle.AddPeopleInterface;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circlepackage.circle.ui.CreateCircle.AddPeopleBottomSheetDiologue;
import circleapp.circlepackage.circle.ui.CreateCircle.CreateCircle;
import circleapp.circlepackage.circle.ui.Explore.ExploreFragment;
import circleapp.circlepackage.circle.ui.Feedback.FeedbackFragment;
import circleapp.circlepackage.circle.ui.MyCircles.WorkbenchFragment;
import circleapp.circlepackage.circle.ui.Notifications.NotificationFragment;

public class PersonelDisplay extends AppCompatActivity implements AddPeopleInterface {

    private ImageButton back, addMembersBtn;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);

        back = findViewById(R.id.bck_applicants_display);
        addMembersBtn = findViewById(R.id.add_members_btn);
        RecyclerView recyclerView = findViewById(R.id.allApplicants_RV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Button listeners
        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        });

        addMembersBtn.setOnClickListener(v->{
            Permissions.check(this, new String[]{Manifest.permission.READ_CONTACTS}, null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    showContacts();
                }
            });
        });
    }

    private void showContacts(){
        AddPeopleBottomSheetDiologue bottomSheetDiologue = new AddPeopleBottomSheetDiologue(this, true);
        bottomSheetDiologue.show(getSupportFragmentManager(), bottomSheetDiologue.getTag());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        switch (item.getItemId()) {
            case R.id.applicants_nav_item:
                selectedFragment = new ApplicantsFragment();
                break;
            case R.id.members_nav_item:
                selectedFragment = new MembersFragment();
                break;

        }
        assert selectedFragment != null;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_circle_members,
                selectedFragment).commit();
        return true;
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
    }

    @Override
    public void contactsInterface(List<String> tempUsersList) {
        Toast.makeText(this,"Added members successfully!",Toast.LENGTH_SHORT).show();
    }
}
