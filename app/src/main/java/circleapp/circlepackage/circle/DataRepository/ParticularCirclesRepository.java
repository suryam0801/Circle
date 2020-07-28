package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class ParticularCirclesRepository extends FirebaseSingleValueRead {
    private GlobalVariables globalVariables = new GlobalVariables();
    public ParticularCirclesRepository() {
        super("/Circles");
    }
    @NonNull
    public LiveData<DataSnapshot> getDataSnapsParticularCircleLiveData(String circleId) {
        FirebaseSingleValueRead liveparticularCircleData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Circles").child(circleId));
        return liveparticularCircleData;
    }
}
