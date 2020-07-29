package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class LocationsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsLocationsSingleValueLiveData(String district) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("Locations").child(district));
        return liveUserData;
    }
}