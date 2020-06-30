package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseRetrievalViewModel extends ViewModel {
    private static final DatabaseReference CIRCLES_REF =
            FirebaseDatabase.getInstance().getReference("/Circles");

    private final FirebaseQueryLiveData liveCircleData = new FirebaseQueryLiveData(CIRCLES_REF);

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsCircleLiveData() {
        return liveCircleData;
    }
}
