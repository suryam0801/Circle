package circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circleapppackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class ContactsViewModel extends ViewModel {
    GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsContactsLiveData() {
        FirebaseSingleValueRead liveContactsData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference().child("Contacts"));
        return liveContactsData;
    }

}
