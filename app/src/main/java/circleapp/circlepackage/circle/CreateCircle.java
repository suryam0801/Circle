package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView typePrompt, tagPrompt;
    private Button btn_createCircle, btn_previewCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton acceptanceButton;
    private ChipGroup interestTagsDisplay;
    private Button interestTagAdd, typeInfoButton, tagInfoButton;
    private AutoCompleteTextView interestTagEntry;
    private User user;

    private FirebaseDatabase database;
    private DatabaseReference tags, circleDB, userDB;
    private FirebaseAuth currentUser;

    //locationTags and location-interestTags retrieved from database. interest tags will be display according to selected location tags
    private List<String> dbInterestTags = new ArrayList<>(); //interestTags will be added by parsing through HashMap LocIntTags
    private HashMap<String, Object> locIntTags = new HashMap<>();


    private List<String> autoCompleteItemsList = new ArrayList<>();

    private List<String> selectedInterests = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_create_circle);

        user = SessionStorage.getUser(CreateCircle.this);

        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        interestTagsDisplay = findViewById(R.id.create_circle_interest_tags_display);
        interestTagEntry = findViewById(R.id.create_circle_interest_tags_entry);
        interestTagAdd = findViewById(R.id.create_circle_interest_tag_add_button);
        typeInfoButton = findViewById(R.id.circle_type_info_button);
        tagInfoButton = findViewById(R.id.circle_tag_info_button);
        typePrompt = findViewById(R.id.circle_type_tip_prompt);
        tagPrompt = findViewById(R.id.circle_tag_tip_prompt);


        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users").child(user.getUserId());

        tags.child("locationInterestTags").child(user.getDistrict()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locIntTags = (HashMap<String, Object>) snapshot.getValue();
                displayInterestTagDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btn_createCircle.setOnClickListener(view -> {
            String cName = circleNameEntry.getText().toString();
            String cDescription = circleDescriptionEntry.getText().toString();

            if (!cName.isEmpty() || !cDescription.isEmpty() || !selectedInterests.isEmpty()) {
                createCirlce(cName, cDescription);
            } else {
                Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(CreateCircle.this, ExploreTabbedActivity.class));
            finish();
        });

        typeInfoButton.setOnClickListener(view -> typePrompt.setVisibility(View.VISIBLE));
        tagInfoButton.setOnClickListener(view -> tagPrompt.setVisibility(View.VISIBLE));

    }

    public void displayInterestTagDialog() {
        User user = SessionStorage.getUser(CreateCircle.this);

        for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
            List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
            Log.d(TAG, "ENTRY FOR LOC INT " + entry.toString());
            for (String interest : tempInterests) {
                if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                    if (entry.getKey().trim().equals(user.getWard().trim()))
                        dbInterestTags.add(0, interest);
                    else
                        dbInterestTags.add(interest);

                    autoCompleteItemsList.add("#" + interest);
                }
            }

            String[] arr = autoCompleteItemsList.toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, arr);
            interestTagEntry.setThreshold(1);
            interestTagEntry.setAdapter(adapter);
            //this for loop is used when the user wants to edit interest tag choices
            for (String interest : selectedInterests) { //add selected interests as options even if they are not in the location-interest list
                if (!dbInterestTags.contains(interest)) {
                    dbInterestTags.add(interest);
                    setInterestTag(interest, interestTagsDisplay);
                }
            }
        }
        //set tags in ward order
        for (String tag : dbInterestTags)
            setInterestTag(tag, interestTagsDisplay);

        interestTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        interestTagEntry.requestFocus();
                        interestTagEntry.setText("#");
                        interestTagEntry.setSelection(interestTagEntry.getText().length());
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

        interestTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String interestTag = interestTagEntry.getText().toString().replace("#", "");
                if (!interestTag.isEmpty()) {
                    selectedInterests.add(interestTag);
                    if (!dbInterestTags.contains(interestTag)) {
                        dbInterestTags.add(interestTag);
                        setInterestTag(interestTag, interestTagsDisplay);
                    } else {
                        interestTagsDisplay.removeViewAt(dbInterestTags.indexOf(interestTag));
                        setInterestTag(interestTag, interestTagsDisplay);
                    }
                }
            }
        });
    }

    public void setInterestTag(final String name, ChipGroup chipGroupLocation) {
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
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);

        if (selectedInterests.contains(name)) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
            chip.setTextColor(Color.BLACK);
        }

        chip.setOnClickListener(view -> {
            if (chip.getChipBackgroundColor().getDefaultColor() == -9655041) {
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
                chip.setTextColor(Color.BLACK);
                selectedInterests.remove(chip.getText().toString().replace("#", ""));
            } else {
                chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                chip.setTextColor(Color.WHITE);
                selectedInterests.add(chip.getText().toString().replace("#", ""));
            }

            Log.d(TAG, "INTEREST TAG LIST: " + selectedInterests.toString());
        });

        if (selectedInterests.contains(name))
            chipGroupLocation.addView(chip, 0);
        else
            chipGroupLocation.addView(chip);

        interestTagEntry.setText("#");
        interestTagEntry.setSelection(interestTagEntry.getText().length());
    }

    public void createCirlce(String cName, String cDescription) {

        User user = SessionStorage.getUser(CreateCircle.this);
        circleDB = database.getReference("Circles");
        currentUser = FirebaseAuth.getInstance();

        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);

        String myCircleID = circleDB.push().getKey();
        String creatorUserID = currentUser.getCurrentUser().getUid();
        String acceptanceType = acceptanceButton.getText().toString();
        if(acceptanceType.equals("Public"))
            acceptanceType = "Automatic";
        else if(acceptanceType.equals("Private"))
            acceptanceType = "Review";

        String creatorName = currentUser.getCurrentUser().getDisplayName();

        //convert selectedInterests into hashmap
        HashMap<String, Boolean> interestHashmap = new HashMap<>();
        if (selectedInterests.isEmpty()) {
            interestHashmap.put("null", true);
        } else {
            for (String interest : selectedInterests)
                interestHashmap.put(interest, true);
        }

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);

        //updating circles
        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, creatorUserID, creatorName,
                interestHashmap, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(),0);

        circleDB.child(myCircleID).setValue(circle);

        //updating tags
        for (String i : selectedInterests) {
            tags.child("interestTags").child(user.getDistrict()).child(i).setValue(true);
            tags.child("locationInterestTags").child(user.getDistrict().trim()).child(user.getWard().trim()).child(i).setValue(true);
        }

        //updating user
        int createdProjects = user.getCreatedCircles();
        createdProjects = createdProjects + 1;
        userDB.child(currentUser.getUid()).child("createdProjects").setValue(createdProjects);

        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        String userJsonString = new Gson().toJson(user);
        storeUserFile(userJsonString, getApplicationContext());

        userDB.child("createdCircles").setValue(currentCreatedNo);

        //navigate back to explore. new circle will be available in workbench
        startActivity(new Intent(CreateCircle.this, ExploreTabbedActivity.class));
        finish();
    }

    private void storeUserFile(String data, Context context) {
        context.deleteFile("user.txt");
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    @Override
    public void onBackPressed() {
        Intent about_intent = new Intent(this, ExploreTabbedActivity.class);
        startActivity(about_intent);
        finish();
    }
}