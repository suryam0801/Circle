package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CirclePersonnelRepository extends FirebaseQueryLiveData {
    private GlobalVariables globalVariables = new GlobalVariables();

    public CirclePersonnelRepository() {
        super("/CirclePersonel");
    }

    @NonNull
    public LiveData<String[]> getDataSnapsCirclePersonelLiveData(String circleId, String membersOrApplicants) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child(membersOrApplicants));
        return liveWorkBenchCircleData;
    }

}
