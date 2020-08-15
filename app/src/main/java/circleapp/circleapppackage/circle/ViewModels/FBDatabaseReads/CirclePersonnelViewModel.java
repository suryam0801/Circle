package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circleapppackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class CirclePersonnelViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsCirclePersonelLiveData(String circleId, String membersOrApplicants) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/CirclePersonel").child(circleId).child(membersOrApplicants));
        return liveWorkBenchCircleData;
    }

}
