package circleapp.circleapppackage.circle.ui.CreateCircle;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Contacts;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.CreateCircle.AddPeopleInterface;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.ContactsViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.UserViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class AddPeopleBottomSheetDiologue extends BottomSheetDialogFragment {
    private BottomSheetBehavior mBehavior;
    private RecyclerView listView ;
    private List<Contacts> contactsList = new ArrayList<>();
    private Button doneBtn;
    private ImageButton backBtn;
    private Cursor cursor ;
    private String name, phonenumber ;
    private ContactsViewModel contactsViewModel;
    private GlobalVariables globalVariables = new GlobalVariables();
    private LiveData<DataSnapshot> liveData;
    private Activity activity;
    private AddPeopleInterface addPeopleInterface;
    private Boolean isCircleWall;
    private Circle circle;
    private String ownUserId;
    private HashMap<String, String > userTokens = new HashMap<>();

    public AddPeopleBottomSheetDiologue(Activity activity, Boolean isCircleWall) {
        this.activity = activity;
        this.isCircleWall = isCircleWall;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.activity_add_people, null);

        dialog.setContentView(view);
        ownUserId = globalVariables.getCurrentUser().getUserId();
        circle = globalVariables.getCurrentCircle();
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
        listView = view.findViewById(R.id.contactView);
        doneBtn = view.findViewById(R.id.done_add_people);
        backBtn = view.findViewById(R.id.back_add_btn);
        addPeopleInterface = (AddPeopleInterface) activity;
//        LoadContacts();
        doneBtn.setOnClickListener(v->{
//            onBackPressed();
            addPeopleInterface.contactsInterface(globalVariables.getUsersList());
            dismiss();
            globalVariables.setUserTokens(userTokens);
        });
        backBtn.setOnClickListener(v->{
//            onBackPressed();
            dismiss();
        });
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    // View is expended
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    // View is collapsed
                }

                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
//                    globalVariables.setTempUsersList(null);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        return dialog;
    }
    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        LoadContacts();
    }
    public void LoadContacts(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
        listView.setHasFixedSize(true);
        listView.setLayoutManager(layoutManager);
        AddPeopleAdapter addPeopleAdapter = new AddPeopleAdapter(contactsList, activity);
        listView.setAdapter(addPeopleAdapter);
        GetContactsIntoArrayList(addPeopleAdapter);
    }
    public void GetContactsIntoArrayList(AddPeopleAdapter addPeopleAdapter){

        cursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null, null, null);
        HashMap<String, String> localContactsList = new HashMap<>();

        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            phonenumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phonenumber = phonenumber.replaceAll(" ", "");
            if(phonenumber.length()>=10)
                localContactsList.put(phonenumber.substring(phonenumber.length()-10),name);
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
                    if(userId.equals(ownUserId))
                        continue;
                    String phn = messageSnapshot.getKey();
                    phn = phn.substring(phn.length()-10);
                    if(localContactsList.containsKey(phn.replaceAll(" ",""))){
                        if (!contactsList.contains(new Contacts(phn,localContactsList.get(phn)))){
                            if(isCircleWall){
                                if(!circle.getMembersList().containsKey(userId)){
                                    contactsList.add(new Contacts(phn,localContactsList.get(phn)));
                                    addPeopleAdapter.notifyDataSetChanged();
                                    tempUsersList.add(userId);
                                    addToUserTokens(userId);
                                }
                            }
                            else {
                                contactsList.add(new Contacts(phn,localContactsList.get(phn)));
                                addPeopleAdapter.notifyDataSetChanged();
                                tempUsersList.add(userId);
                                addToUserTokens(userId);
                            }
                        }else{
                            contactsList.remove(new Contacts(phn,localContactsList.get(phn)));
                            addPeopleAdapter.notifyDataSetChanged();
                            tempUsersList.remove(userId);
                            removeFromUsersList(userId);
                        }

                    }
                }

                globalVariables.setTempUsersList(tempUsersList);
            }
            else {
                Log.d("Contacts","Data not exist");
            }
        });
    }

    private void addToUserTokens(String userId){

        UserViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) activity).get(UserViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsUserValueCirlceLiveData(userId);
        tempLiveData.observe((LifecycleOwner) activity, dataSnapshot -> {
            User temp = dataSnapshot.getValue(User.class);
            if (temp != null) {
                String tokenId = temp.getToken_id();
                userTokens.put(userId,tokenId);
            }
        });
    }

    private void removeFromUsersList(String userId){
        userTokens.remove(userId);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        globalVariables.setTempUsersList(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        contactsList = new ArrayList<>();
        liveData.removeObservers(this);
    }
}
