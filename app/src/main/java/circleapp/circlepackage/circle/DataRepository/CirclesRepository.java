package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class CirclesRepository extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsMultipleCircles(String userId) {
        String childQueryPath = "membersList/" + userId;
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Circles").orderByChild(childQueryPath).equalTo(true));
        return liveWorkBenchCircleData;
    }
    @NonNull
    public LiveData<String[]> getDataSnapsMultipleCirclesInLocation(String location) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Circles").orderByChild("circleDistrict").equalTo(location));
        return liveExploreCircleData;
    }

}
