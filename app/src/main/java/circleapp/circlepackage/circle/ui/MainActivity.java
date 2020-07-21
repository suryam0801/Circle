package circleapp.circlepackage.circle.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.data.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.ui.Login.OnBoarding.get_started_first_page;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set dimensions for app
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
        setPersistenceEnabled();
        checkIfUserExists();

    }
    private void setPersistenceEnabled(){
        SharedPreferences persistenceCheckPrefs = getApplicationContext().getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (persistenceCheckPrefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            persistenceCheckPrefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            FirebaseWriteHelper.setPersistenceFb();
        }
    }

    private void checkIfUserExists(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            //notificationCountGetter();
            UserViewModel viewModel = ViewModelProviders.of(MainActivity.this).get(UserViewModel.class);

            LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(FirebaseAuth.getInstance().getCurrentUser().getUid());

            liveData.observe(MainActivity.this, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    SessionStorage.saveUser(MainActivity.this, user);
                    startActivity(new Intent(MainActivity.this, ExploreTabbedActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(MainActivity.this, EntryPage.class));
                    finish();
                }
            });
        } else {
            startActivity(new Intent(MainActivity.this, get_started_first_page.class));
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SessionStorage.saveFilters(this, null);
    }
}