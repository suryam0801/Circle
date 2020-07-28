package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CommentsRepository extends FirebaseQueryLiveData {
    private GlobalVariables globalVariables = new GlobalVariables();

    public CommentsRepository() {
        super("/BroadcastComments");
    }

    @NonNull
    public LiveData<String[]> getDataSnapsCommentsLiveData(String circleId, String broadcastId) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/BroadcastComments").child(circleId).child(broadcastId).orderByChild("timestamp"));
        return liveWorkBenchCircleData;
    }
}
