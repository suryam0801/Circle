package circleapp.circlepackage.circle.Login;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class InterestTagPicker extends AppCompatActivity {

    private String TAG = InterestTagPicker.class.getSimpleName();

    //Declare all UI elements for the InterestTagPicker Activity
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private Button register;
    private String fName, lName, userId, downloadUri, contact, ward, district;
    private List<String> locationTags = new ArrayList<>(), selectedInterestTags = new ArrayList<>();
    private List<String> dbInterestTags = new ArrayList<>();
    private List<String> autoCompleteItemsList = new ArrayList<>();
    private AutoCompleteTextView interestTagsEntry;
    private User user;
    private ChipGroup chipGroup;
    private Button interestTagAdd;
    private FirebaseDatabase database;
    private DatabaseReference tags, usersDB;

    private HashMap<String, Object> locIntTags = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_interest_tag_picker);
        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        register = findViewById(R.id.registerButton);
        chipGroup = findViewById(R.id.interest_tag_chip_group);
        interestTagsEntry = (AutoCompleteTextView) findViewById(R.id.interest_tags_entry);
        interestTagAdd = findViewById(R.id.interest_tag_add_button);

        //Getting Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");

        fName = getIntent().getStringExtra("fName");
        lName = getIntent().getStringExtra("lName");

        downloadUri = getIntent().getStringExtra("uri");
        contact = getIntent().getStringExtra("contact");
        ward = getIntent().getStringExtra("ward");
        district = getIntent().getStringExtra("district");

        tags.child("locationInterestTags").child(district).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    locIntTags = (HashMap<String, Object>) snapshot.getValue();

                    Log.d(TAG, "LOC INT TAGS: " + locIntTags.toString());

                    chipGroup.removeAllViews();
                    for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
                        Log.d(TAG, "ENTRY FOR LOC INT " + entry.toString());
                        List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
                        for (String interest : tempInterests) {
                            if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                                if(entry.getKey().trim().equals(ward.trim()))
                                    dbInterestTags.add(0, interest);
                                else
                                    dbInterestTags.add(interest);

                                autoCompleteItemsList.add("#" + interest);
                            }
                        }

                        String[] arr = autoCompleteItemsList.toArray(new String[0]);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, arr);
                        interestTagsEntry.setThreshold(1);
                        interestTagsEntry.setAdapter(adapter);
                        //this for loop is used when the user wants to edit interest tag choices
                        for (String interest : selectedInterestTags) { //add selected interests as options even if they are not in the location-interest list
                            if (!dbInterestTags.contains(interest)) {
                                dbInterestTags.add(interest);
                                setTag(interest);
                            }
                        }
                    }
                    //set tags in ward order
                    for(String tag : dbInterestTags)
                        setTag(tag);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //The function to register the Users with their appropriate details
                UserReg();
            }
        });

        //Touch listener to autoenter the # as prefix when user try to enter the new interest tag
        interestTagsEntry.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(interestTagsEntry, InputMethodManager.SHOW_IMPLICIT);
                        interestTagsEntry.setShowSoftInputOnFocus(true);
                        interestTagsEntry.setText("#");
                        interestTagsEntry.setSelection(interestTagsEntry.getText().length());
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;

            }
        });


        //the add button in edittext to add the new interest in the list
        interestTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String interestTag = interestTagsEntry.getText().toString().replace("#", "");
                if (!interestTag.isEmpty()) {
                    selectedInterestTags.add(interestTag);
                    if (!dbInterestTags.contains(interestTag)) {
                        dbInterestTags.add(interestTag);
                        setTag(interestTag);
                    } else {
                        chipGroup.removeViewAt(dbInterestTags.indexOf(interestTag));
                        setTag(interestTag);
                    }
                }
            }
        });
    }

    // Function to add a chip to chipgroup
    private void setTag(final String name) {
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setRippleColor(ColorStateList.valueOf(Color.WHITE));
        chip.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3,
                        getResources().getDisplayMetrics()
                ),
                paddingDp, paddingDp, paddingDp);
        //chip name
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);

        if (selectedInterestTags.contains(name.replace("#", ""))) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
            chip.setTextColor(Color.BLACK);
        }
        //onclick listener to chip for select the chip from the chipgroup
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chip.getChipBackgroundColor().getDefaultColor() == -9655041) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
                    chip.setTextColor(Color.BLACK);
                    selectedInterestTags.remove(chip.getText().toString().replace("#", ""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedInterestTags.add(chip.getText().toString().replace("#", ""));

                }

                Log.d(TAG, "INTEREST TAG LIST: " + selectedInterestTags.toString());
            }

        });
        //Add a chip to the Chipgroup
        if(selectedInterestTags.contains(name))
            chipGroup.addView(chip, selectedInterestTags.indexOf(name));
        else
            chipGroup.addView(chip);

        //set the # in edittext after adding the new location
        interestTagsEntry.setText("#");
        interestTagsEntry.setSelection(interestTagsEntry.getText().length());
    }

    public void UserReg() {

        //Ensure the textboxes are not empty
        if (!TextUtils.isEmpty(fName) && !TextUtils.isEmpty(lName)) {
            //getting the current user id
            userId = firebaseAuth.getInstance().getCurrentUser().getUid();

            //Merging the fname and lname to set the displayname to the user for easy access
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fName + " " + lName)
                    .build();

            //update the user display name
            firebaseAuth.getCurrentUser().updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(InterestTagPicker.this, "User Registered Successfully", Toast.LENGTH_LONG).show();
                                //Adding the user to collection
                                addUser();
                            } else {
                                Toast.makeText(InterestTagPicker.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                //to signout the current firebase user
                                firebaseAuth.signOut();
                                //delete the user details
                                firebaseAuth.getCurrentUser().delete();
                            }
                        }
                    });

        } else {
            Toast.makeText(InterestTagPicker.this, "Enter Valid details", Toast.LENGTH_LONG).show();
        }
    }

    //function that adds the user to the firestore
    private void addUser() {

        usersDB = database.getReference("Users");

        // storing the tokenid for the notification purposes
        String token_id = FirebaseInstanceId.getInstance().getToken();

        //set interest and location tags as hashmaps
        HashMap<String, Boolean> locationTagHashmap = new HashMap<>();
        for (String locationTemp : locationTags)
            locationTagHashmap.put(locationTemp, true);

        HashMap<String, Boolean> interestTagHashmap = new HashMap<>();
        for (String interestTemp : selectedInterestTags)
            interestTagHashmap.put(interestTemp, true);


        //checking the dowloadUri to store the profile pic
        //if the downloadUri id null then 'default' value is stored
        if (downloadUri != null) {
            //creaeting the user object
            user = new User(fName, lName, contact, downloadUri, interestTagHashmap, userId, 0, 0, 0, token_id, ward, district);
        } else {
            user = new User(fName, lName, contact, "default", interestTagHashmap, userId, 0, 0, 0, token_id, ward, district);
        }

        for (String i : selectedInterestTags) {
            tags.child("interestTags").child(i).setValue(true);
            tags.child("locationInterestTags").child(district.trim()).child(ward.trim()).child(i).setValue(true);
        }

        //store user in realtime database. (testing possible options for fastest retrieval)
        usersDB.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                db.collection("Users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //if the user registered and profile updated successfully  the mainActivity will be opened
                                startActivity(new Intent(InterestTagPicker.this, Explore.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed to create user", Toast.LENGTH_LONG).show();
                            }
                        });
                finish();
            }
        });



        //store the current user locally for fastest retrieval of only current user
/*        final SharedPreferences sharedPreferences = getSharedPreferences("LocalUserPermaStore", MODE_PRIVATE);
        String string = new Gson().toJson(user);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("myUserDetails", string);
        editor.commit();*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        chipGroup.removeAllViews();
    }
}
