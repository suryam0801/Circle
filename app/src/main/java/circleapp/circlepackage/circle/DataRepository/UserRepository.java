package circleapp.circlepackage.circle.DataRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

    public class UserRepository extends FirebaseSingleValueRead {

        private GlobalVariables globalVariables = new GlobalVariables();
    public UserRepository(){
        super("/Users");
    }

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsUserValueLiveData(String uid) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Users").child(uid));
        return liveUserData;
    }
}
