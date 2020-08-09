package circleapp.circlepackage.circle.ui.CreateCircle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.ContactsViewModel;

public class AddPeople extends AppCompatActivity {
    private RecyclerView listView ;
    ArrayList<String> StoreContacts , temp_num,temp_name;
    ArrayAdapter<String> arrayAdapter ;
    Cursor cursor ;
    String name, phonenumber ;
    public  static final int RequestPermissionCode  = 1 ;
    Button button;
    private RecyclerView.Adapter contactAdaptor;
    ContactsViewModel contactsViewModel;
    private LiveData<DataSnapshot> liveData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_people);

        listView = findViewById(R.id.contactView);

        button = (Button)findViewById(R.id.button1);

        StoreContacts = new ArrayList<String>();
        temp_num = new ArrayList<String>();
        temp_name = new ArrayList<String>();

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

        RecyclerView wbrecyclerView = findViewById(R.id.contactView);
        wbrecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager wblayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        wbrecyclerView.setLayoutManager(wblayoutManager);
//        arrayAdapter = new ArrayAdapter<String>(
//                AddPeople.this,
//                R.layout.contact_items_listview,
//                R.id.textView, StoreContacts
//        );
//
//        listView.setAdapter(arrayAdapter);
    }

    public void GetContactsIntoArrayList(){

        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);
        ArrayList<String> finallist = new ArrayList<String>();
        while (cursor.moveToNext()) {

            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            StoreContacts.add(phonenumber);

        }
        liveData = contactsViewModel.getDataSnapsContactsLiveData();
        liveData.observe(this, dataSnapshot ->{
            if (dataSnapshot.exists()) {
                String message;
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    message = messageSnapshot.getValue().toString();
                    String phn = messageSnapshot.getKey();
                    Log.d("Contacts", message + "$$" + phn);
                    temp_num.add(phn);
                    temp_name.add(message);
                }
                Log.d("Contacts", temp_num.toString() + "$$");
//                for (String temp : StoreContacts){
//                    if (temp.length()>10){
//                        String lastTenDigits = temp.substring(temp.length() - 10);
//                        StoreContacts.remove(temp);
//                        StoreContacts.add(lastTenDigits);
//                    }
//                }
//                for (String temp : tempList){
//                    if (temp.length()>10){
//                        String lastTenDigits = temp.substring(temp.length() - 10);
//                        tempList.remove(temp);
//                        tempList.add(lastTenDigits);
//                    }
//                }
                for (String i : temp_num) {
                    Log.d("Contacts", i + " " + i.substring(3));
                    for (String j : StoreContacts) {
                        if (i.length() >= 10 && j.length() >= 10) {
                            if (i.substring(i.length() - 10).contains(j.substring(j.length() - 10))) {
                                finallist.add(i);
//                             String lastTenDigits = j.substring(j.length() - 10);
                                Log.d("Contacts", finallist.toString() + "$$");
                                contactAdaptor = new AddPeopleAdapter(finallist,this);

//                                arrayAdapter = new ArrayAdapter<String>(
//                                        AddPeople.this,
//                                        R.layout.contact_items_listview,
//                                        R.id.textView, finallist
//                                );

//                                listView.setAdapter(arrayAdapter);
                            }
                        }
                    }

                }

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