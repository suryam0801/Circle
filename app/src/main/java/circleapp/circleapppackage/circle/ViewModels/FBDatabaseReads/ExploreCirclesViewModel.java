package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circleapppackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class ExploreCirclesViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<String[]> getDataSnapsExploreCircleLiveData(String location) {
        FirebaseQueryLiveData liveExploreCircleData = new FirebaseQueryLiveData(globalVariables.getFBDatabase().getReference("/Circles").orderByChild("circleDistrict").equalTo(location));
        return liveExploreCircleData;
    }
}