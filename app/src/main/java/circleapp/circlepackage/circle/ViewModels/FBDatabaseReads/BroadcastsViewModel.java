package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class BroadcastsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId));
        return liveExploreCircleData;
    }

    @NonNull
    public LiveData<DataSnapshot> getParticularBroadcastLiveData(String circleId, String broadcastId) {
        FirebaseSingleValueRead liveExploreCircleData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId));
        return liveExploreCircleData;
    }

}
