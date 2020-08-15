package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circleapppackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class UserViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsUserValueCirlceLiveData(String uid) {
        FirebaseSingleValueRead liveUserData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference("/Users").child(uid));
        return liveUserData;
    }
}
