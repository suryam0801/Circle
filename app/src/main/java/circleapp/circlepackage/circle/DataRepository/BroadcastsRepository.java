package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class BroadcastsRepository extends FirebaseQueryLiveData {
    private GlobalVariables globalVariables = new GlobalVariables();

    public BroadcastsRepository() {
        super("/Broadcasts");
    }

    @NonNull
    public LiveData<String[]> getDataSnapsBroadcastLiveData(String circleId) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Broadcasts").child(circleId));
        return liveExploreCircleData;
    }
}
