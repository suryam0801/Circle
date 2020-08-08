package circleapp.circlepackage.circle.ViewModels.FBDatabaseReads;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.DataLayer.FirebaseQueryLiveData;
import circleapp.circlepackage.circle.DataLayer.FirebaseSingleValueRead;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class ContactsViewModel extends ViewModel {
    GlobalVariables globalVariables = new GlobalVariables();

    @NonNull
    public LiveData<DataSnapshot> getDataSnapsContactsLiveData() {
        FirebaseSingleValueRead liveContactsData = new FirebaseSingleValueRead(globalVariables.getFBDatabase().getReference().child("/Contacts"));
//        Log.d("Contacts",liveContactsData.getValue().toString()+" $$$");
        return liveContactsData;
    }

}
