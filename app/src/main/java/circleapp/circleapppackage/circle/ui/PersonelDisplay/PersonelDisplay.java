package circleapp.circleapppackage.circle.ui.PersonelDisplay;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import circleapp.circleapppackage.circle.DataLayer.CircleRepository;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.CreateCircle.AddPeopleInterface;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circleapppackage.circle.ui.CreateCircle.AddPeopleBottomSheetDiologue;

public class PersonelDisplay extends AppCompatActivity implements AddPeopleInterface {

    private ImageButton back, addMembersBtn;
    private GlobalVariables globalVariables = new GlobalVariables();
    private Circle circle;
    private User user;
    private BottomNavigationView bottomNav;
    private LiveData<DataSnapshot> tempLiveData;
    private LiveData<DataSnapshot> circlesPersonelLiveData;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personel_display);

        back = findViewById(R.id.bck_applicants_display);
        addMembersBtn = findViewById(R.id.add_members_btn);
        bottomNav = findViewById(R.id.bottom_navigation_circle_members);
        circle = globalVariables.getCurrentCircle();
        user = globalVariables.getCurrentUser();

        //Button listeners
        back.setOnClickListener(view -> {
            finishAfterTransition();
            startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
        });

        addMembersBtn.setOnClickListener(v->{
            globalVariables.setUsersList(null);
            Permissions.check(this, new String[]{Manifest.permission.READ_CONTACTS}, null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    showContacts();
                }
            });
        });
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setSelectedItemId(R.id.members_nav_item);
        setCircleObserver();
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

    private void setCircleObserver(){
        circle = globalVariables.getCurrentCircle();
        MyCirclesViewModel tempViewModel = ViewModelProviders.of(this).get(MyCirclesViewModel.class);
        tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) this, dataSnapshot -> {
            Circle circleTemp = dataSnapshot.getValue(Circle.class);
            if (circleTemp != null&&circleTemp.getMembersList()!=null) {
                circle = circleTemp;
                if (circle.getMembersList().containsKey(user.getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void addMembersToCirclePersonel() {
        if(globalVariables.getUsersList()!=null){
            for(String userId: globalVariables.getUsersList()) {
                UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) this).get(UserViewModel.class);
                circlesPersonelLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(userId);
                circlesPersonelLiveData.observe((LifecycleOwner) this, dataSnapshot -> {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        Subscriber subscriber = new Subscriber(user, System.currentTimeMillis());
                        HelperMethodsBL.updateCirclePersonel(subscriber, circle.getId());
                    }
                });
            }
            CircleRepository circleRepository = new CircleRepository();
            circleRepository.addUsersToCircle(circle,"normal");
            Intent intent = getIntent();
            finishAfterTransition();
            startActivity(intent);
            Toast.makeText(this,"Added members successfully!",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        startActivity(new Intent(PersonelDisplay.this, CircleWall.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void contactsInterface(List<String> tempUsersList) {
        addMembersToCirclePersonel();
    }
}
