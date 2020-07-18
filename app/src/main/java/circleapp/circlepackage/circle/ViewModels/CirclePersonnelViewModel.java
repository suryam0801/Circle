package circleapp.circlepackage.circle.ViewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseQueryLiveData;

public class CirclePersonnelViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference CIRCLES_PERSONEL_REF = database.getReference("/CirclePersonel");

    @NonNull
    public LiveData<String[]> getDataSnapsCirclePersonelLiveData(String circleId, String membersOrApplicants) {
        FirebaseQueryLiveData liveWorkBenchCircleData = new FirebaseQueryLiveData(CIRCLES_PERSONEL_REF.child(circleId).child(membersOrApplicants));
        return liveWorkBenchCircleData;
    }

}
