package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;

public class BroadcastsViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference BROADCASTS_REF = database.getReference("/Broadcasts");

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(BROADCASTS_REF.child(circleId));
        return liveExploreCircleData;
    }
}
