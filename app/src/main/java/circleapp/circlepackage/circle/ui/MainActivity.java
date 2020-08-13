package circleapp.circlepackage.circle.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.MainActivityViewModel;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.ui.Login.OnBoarding.get_started_first_page;
import circleapp.circlepackage.circle.R;

public class MainActivity extends AppCompatActivity {

    private GlobalVariables globalVariables = new GlobalVariables();
    private MainActivityViewModel mainActivityViewModel;
    private String circleId;
    private Circle circle;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        circleId = getIntent().getStringExtra("circleId");

        setUserUpdatesObserver();
        mainActivityViewModel = ViewModelProviders.of( this).get(MainActivityViewModel.class);
        mainActivityViewModel.setPersistenceEnabled();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUserUpdatesObserver(){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) this).get(UserViewModel.class);

            LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(globalVariables.getAuthenticationToken().getCurrentUser().getUid());

            liveData.observe((LifecycleOwner) this, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    //for workbench display
                    if(user.getActiveCircles()!=null)
                        globalVariables.setInvolvedCircles(user.getCreatedCircles()+user.getActiveCircles().size());
                    else
                        globalVariables.setInvolvedCircles(user.getCreatedCircles());
                    mainActivityViewModel.saveUserToSession(user);
                    updateToken(user);
                    sendUserToHome(user);
                } else {
                    sendUserToLogin();
                }
            });
        }
        else {
            startOnBoarding();
        }
    }

    private void updateToken(User user) {
        String temp_token = FirebaseInstanceId.getInstance().getToken();
        if(temp_token != null && user != null){
            if (!user.getToken_id().equals(temp_token)){
                user.setToken_id(temp_token);
                globalVariables.getFBDatabase().getReference("Users").child(user.getUserId()).setValue(user).addOnCompleteListener(task -> {
                    Log.d("Main","Token Updates");
                });
            }else {
                Log.d("Main","Old Token");
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendUserToHome(User user){
        Intent i = new Intent(MainActivity.this, ExploreTabbedActivity.class);
        Uri intentUri = getIntent().getData();
        if (intentUri != null) {
            String url = getIntent().getData().toString();
            i.putExtra("imagelink", url);
        }
        if(circleId!=null)
            goToCircleFromNotif(user);
        startActivity(i);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void goToCircleFromNotif(User user){
        MyCirclesViewModel tempViewModel = ViewModelProviders.of(this).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circleId);
        tempLiveData.observe((LifecycleOwner) this, dataSnapshot -> {
            Circle circleTemp = dataSnapshot.getValue(Circle.class);
            if (circleTemp != null&&circleTemp.getMembersList()!=null) {
                circle = circleTemp;
                if (circle.getMembersList().containsKey(user.getUserId())) {
                    globalVariables.saveCurrentCircle(circle);
                    startActivity(new Intent(MainActivity.this, CircleWall.class));
                    ((Activity) MainActivity.this).finishAfterTransition();
                }
            }
        });
    }

    private void startOnBoarding(){
        startActivity(new Intent(MainActivity.this, get_started_first_page.class));
        finish();
    }

    private void sendUserToLogin(){
        startActivity(new Intent(MainActivity.this, EntryPage.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.saveFilters(this, null);
    }
}