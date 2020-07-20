package circleapp.circlepackage.circle.ViewModels.LoginViewModels.UserRegistration;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.data.FBDatabaseReads.LocationsViewModel;

public class ReadExistingLocations extends ViewModel {
    IsLocationExistsListener isLocationExistsListener;
    public void setIsLocationExistsListener(IsLocationExistsListener isLocationExistsListener) {
        this.isLocationExistsListener = isLocationExistsListener;
    }
    public void readLocationDB(Activity activity, String district) {
        LocationsViewModel viewModel = ViewModelProviders.of((FragmentActivity) activity).get(LocationsViewModel.class);

        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsLocationsSingleValueLiveData(district);
        liveData.observe((LifecycleOwner) activity, dataSnapshot -> {
            if (dataSnapshot.exists()) {
                isLocationExistsListener.isLocationExists(true);
            } else {
                isLocationExistsListener.isLocationExists(false);
            }
        });
    }
}
