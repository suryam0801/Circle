package circleapp.circlepackage.circle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.testing.FakeAppUpdateManager;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
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

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

public class MainActivity extends AppCompatActivity {

    private GlobalVariables globalVariables = new GlobalVariables();
    private MainActivityViewModel mainActivityViewModel;
    private String circleId;
    private Circle circle;
    private int APP_UPDATE_PROGRESS = 700;
    private AppUpdateManager appUpdateManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        circleId = getIntent().getStringExtra("circleId");
        appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);
        checkForUpdates();

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

    private void checkForUpdates(){
        // Creates instance of the manager.
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(MainActivity.this);

// Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                Log.d("UptoDate","App");
                try {
                    startUpdate(appUpdateInfo);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                // Request the update.
            }
        });
    }

    private void startUpdate(AppUpdateInfo appUpdateInfo) throws IntentSender.SendIntentException {
        appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                IMMEDIATE,
                this,
                APP_UPDATE_PROGRESS
                );

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_UPDATE_PROGRESS) {
            if (resultCode != RESULT_OK) {
                checkForUpdates();
                // If the update is cancelled or fails,
                // you can request to start the update again.
            }
        }
    }

    // Checks that the update is not stalled during 'onResume()'.
// However, you should execute this check at all entry points into the app.
    @Override
    protected void onResume() {
        super.onResume();

        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {
                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            IMMEDIATE,
                                            this,
                                            APP_UPDATE_PROGRESS);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.saveFilters(this, null);
    }
}