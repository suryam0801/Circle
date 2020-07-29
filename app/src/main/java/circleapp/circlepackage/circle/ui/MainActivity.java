package circleapp.circlepackage.circle.ui;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circlepackage.circle.ViewModels.LoginViewModels.MainActivityViewModel;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.ui.Login.EntryPage.EntryPage;
import circleapp.circlepackage.circle.ui.Login.OnBoarding.get_started_first_page;
import circleapp.circlepackage.circle.R;

public class MainActivity extends AppCompatActivity {

    private GlobalVariables globalVariables = new GlobalVariables();
    private MainActivityViewModel mainActivityViewModel;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);

        setUserUpdatesObserver();
        mainActivityViewModel = ViewModelProviders.of( this).get(MainActivityViewModel.class);
        mainActivityViewModel.setPersistenceEnabled();
    }
    private void setUserUpdatesObserver(){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            UserViewModel viewModel = ViewModelProviders.of((FragmentActivity) this).get(UserViewModel.class);

            LiveData<DataSnapshot> liveData = viewModel.getDataSnapsUserValueCirlceLiveData(globalVariables.getAuthenticationToken().getCurrentUser().getUid());

            liveData.observe((LifecycleOwner) this, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    mainActivityViewModel.saveUserToSession(user);
                    sendUserToHome();
                } else {
                    sendUserToLogin();
                }
            });
        }
        else {
            startOnBoarding();
        }
    }

    private void sendUserToHome(){
        Intent i = new Intent(MainActivity.this, ExploreTabbedActivity.class);
        Uri intentUri = getIntent().getData();
        if (intentUri != null) {
            String url = getIntent().getData().toString();
            i.putExtra("imagelink", url);
        }
        startActivity(i);
        finish();
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