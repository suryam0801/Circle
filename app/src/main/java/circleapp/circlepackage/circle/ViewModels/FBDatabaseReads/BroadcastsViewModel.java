package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circlepackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class BroadcastsViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId));
        return liveExploreCircleData;
    }
}
