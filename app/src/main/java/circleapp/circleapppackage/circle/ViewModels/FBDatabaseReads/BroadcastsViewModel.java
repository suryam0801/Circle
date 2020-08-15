package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circleapppackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circleapppackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class BroadcastsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).orderByChild("latestCommentTimestamp"));
        return liveExploreCircleData;
    }

    @NonNull
    public LiveData<DataSnapshot> getParticularBroadcastLiveData(String circleId, String broadcastId) {
        FirebaseSingleValueRead liveExploreCircleData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId).child(broadcastId));
        return liveExploreCircleData;
    }

}
