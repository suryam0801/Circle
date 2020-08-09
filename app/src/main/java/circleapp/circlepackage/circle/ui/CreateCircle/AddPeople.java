package circleapp.circlepackage.circle.ui.CreateCircle;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import circleapp.circlepackage.circle.Model.ObjectModels.Contacts;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.ContactsViewModel;

public class AddPeople extends AppCompatActivity {
    private RecyclerView listView ;
    private List<Contacts> contactsList = new ArrayList<>();
    Cursor cursor ;
    String name, phonenumber ;
    ContactsViewModel contactsViewModel;
    GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<DataSnapshot> liveData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);

        listView = findViewById(R.id.contactView);
        LoadContacts();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void LoadContacts(){
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        listView.setLayoutManager(wblayoutManager);
        AddPeopleAdapter addPeopleAdapter = new AddPeopleAdapter(contactsList, this);
        listView.setAdapter(addPeopleAdapter);
        GetContactsIntoArrayList();
    }

    public void GetContactsIntoArrayList(){

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);
        HashMap<String, String> cont = new HashMap<>();

        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if(phonenumber.length()>=10)
                cont.put(phonenumber.substring(phonenumber.length()-10),name);

        }
        cursor.close();

        contactsViewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);
        liveData = contactsViewModel.getDataSnapsContactsLiveData();
        liveData.observe(this, dataSnapshot ->{
            if (dataSnapshot.exists()) {
                String userId;
                List<String > usersList = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    userId = messageSnapshot.getValue().toString();
                    String phn = messageSnapshot.getKey();
                    phn = phn.substring(phn.length()-10);
                    if(cont.containsKey(phn)){
                        contactsList.add(new Contacts(phn,cont.get(phn)));
                        usersList.add(userId);
                    }
                }
                globalVariables.setUsersList(usersList);
                Log.d("ContactInServer",contactsList.toString());
            }
            else {
                Log.d("Contacts","Data not exist");
            }
        });
    }
}