package circleapp.circlepackage.circle.ui.CreateCircle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import circleapp.circlepackage.circle.Model.ObjectModels.Contacts;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.ContactsViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.NotificationsViewModel;

public class AddPeople extends AppCompatActivity {
    ListView listView ;
    ArrayList<String> StoreContacts ,tempList;
    ArrayAdapter<String> arrayAdapter ;
    Cursor cursor ;
    String name, phonenumber ;
    public  static final int RequestPermissionCode  = 1 ;
    Button button;
    ContactsViewModel contactsViewModel;
    private LiveData<DataSnapshot> liveData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);

        listView = (ListView)findViewById(R.id.listview1);

        button = (Button)findViewById(R.id.button1);

        StoreContacts = new ArrayList<String>();
        tempList= new ArrayList<String>();

        EnableRuntimePermission();
        contactsViewModel = ViewModelProviders.of(this).get(ContactsViewModel.class);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadContacts();
    }

    public void LoadContacts(){
        EnableRuntimePermission();
        GetContactsIntoArrayList();
        arrayAdapter = new ArrayAdapter<String>(
                AddPeople.this,
                R.layout.contact_items_listview,
                R.id.textView, StoreContacts
        );

        listView.setAdapter(arrayAdapter);
    }

    public void GetContactsIntoArrayList(){

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);

        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            StoreContacts.add(name + " "  + ":" + " " + phonenumber);

        }
        liveData = contactsViewModel.getDataSnapsContactsLiveData();
        liveData.observe(this, dataSnapshot ->{
            if (dataSnapshot.exists()){
                Contacts contacts = dataSnapshot.getValue(Contacts.class);
                Log.d("Contact",contacts.getPhn_number()+"::::::::"+contacts.getUid());
                Log.d("Contacts",contacts.toString());
            }
            else {
                Log.d("Contacts","Data not exist");
            }
        });



        cursor.close();

    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                AddPeople.this,
                Manifest.permission.READ_CONTACTS))
        {

//            Toast.makeText(AddPeople.this,"CONTACTS permission allows us to Access CONTACTS app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(AddPeople.this,new String[]{
                    Manifest.permission.READ_CONTACTS}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

//                    Toast.makeText(AddPeople.this,"Permission Granted, Now your application can access CONTACTS.", Toast.LENGTH_LONG).show();

                } else {

//                    Toast.makeText(AddPeople.this,"Permission Canceled, Now your application cannot access CONTACTS.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}