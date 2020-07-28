package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class LocationsRepository extends FirebaseSingleValueRead {
    private GlobalVariables globalVariables = new GlobalVariables();

    public LocationsRepository() {
        super("Locations");
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsLocationsSingleValueLiveData(String district) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("Locations").child(district));
        return liveUserData;
    }
}