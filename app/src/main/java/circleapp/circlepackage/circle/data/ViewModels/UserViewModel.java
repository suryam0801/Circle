package circleapp.circlepackage.circle.data.ViewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;

public class UserViewModel extends ViewModel {
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final DatabaseReference USERS_REF = database.getReference("/Users");

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsUserValueCirlceLiveData(String uid) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(USERS_REF.child(uid));
        return liveUserData;
    }
}
