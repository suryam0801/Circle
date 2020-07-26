package circleapp.circlepackage.circle.ui;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.FirebaseApp;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.UpdateUserStatus;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.ui.Login.OnBoarding.get_started_first_page;
import circleapp.circlepackage.circle.R;

public class MainActivity extends AppCompatActivity {

    private String userStatus;
    private UpdateUserStatus updateUserStatus;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFirebaseApp();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);

        setUserUpdatesObserver();
        updateUserStatus.checkIfUserExists(this);
    }
    private void initFirebaseApp(){
        if (FirebaseApp.getApps(this)==null) {
            FirebaseApp.initializeApp(this);
        }
    }
    private void setUserUpdatesObserver(){

        updateUserStatus = ViewModelProviders.of( this).get(UpdateUserStatus.class);
        updateUserStatus.listenForUserUpdates(userStatus, this).observe((LifecycleOwner) this, userStatus -> {
            if(userStatus==null);
            else if(userStatus.equals("existing_user")){
                Intent i = new Intent(MainActivity.this, ExploreTabbedActivity.class);
                Uri intentUri = getIntent().getData();
                if (intentUri != null) {
                    String url = getIntent().getData().toString();
                    i.putExtra("imagelink", url);
                }
                startActivity(i);
                finish();
            }
            else if(userStatus.equals("repeat_user"))
            {
                startActivity(new Intent(MainActivity.this, EntryPage.class));
                finish();
            }
            else {
                startActivity(new Intent(MainActivity.this, get_started_first_page.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.saveFilters(this, null);
    }
}