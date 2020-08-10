package circleapp.circlepackage.circle.ui.CreateCircle;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
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
    private Button doneBtn;
    private ImageButton backBtn;
    private Cursor cursor ;
    private String name, phonenumber ;
    private ContactsViewModel contactsViewModel;
    private GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<DataSnapshot> liveData;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);
        listView = findViewById(R.id.contactView);
        doneBtn = findViewById(R.id.done_add_people);
        backBtn = findViewById(R.id.back_add_btn);
        globalVariables.setUsersList(null);
        LoadContacts();
        doneBtn.setOnClickListener(v->{
            onBackPressed();
        });
        backBtn.setOnClickListener(v->{
            onBackPressed();
        });
    }

    public void LoadContacts(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(layoutManager);
        AddPeopleAdapter addPeopleAdapter = new AddPeopleAdapter(contactsList, this);
        listView.setAdapter(addPeopleAdapter);
        GetContactsIntoArrayList(addPeopleAdapter);
    }

    public void GetContactsIntoArrayList(AddPeopleAdapter addPeopleAdapter){

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
                List<String > tempUsersList = new ArrayList<>();
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    userId = messageSnapshot.getValue().toString();
                    String phn = messageSnapshot.getKey();
                    phn = phn.substring(phn.length()-10);
                    if(cont.containsKey(phn)){
                        contactsList.add(new Contacts(phn,cont.get(phn)));
                        addPeopleAdapter.notifyDataSetChanged();
                        tempUsersList.add(userId);
                    }
                }
                globalVariables.setTempUsersList(tempUsersList);
                Log.d("ContactInServer",tempUsersList.toString());
                Log.d("ContactInServerRecycler",contactsList.toString());
            }
            else {
                Log.d("Contacts","Data not exist");
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed(){
        finishAfterTransition();
        globalVariables.setTempUsersList(null);
        super.onBackPressed();
    }
}