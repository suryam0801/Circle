package circleapp.circlepackage.circle.data.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;

public class LocationsViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference LOCATIONS_REF = database.getReference("/Locations");

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsLocationsSingleValueLiveData(String district) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(LOCATIONS_REF.child(district));
        return liveUserData;
    }
}