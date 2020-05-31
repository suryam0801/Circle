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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView tv_selectInterestTags, interestTagSelectTitle;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton acceptanceButton;
    private ChipGroup interestTagsDisplay;
    private User user;

    private FirebaseDatabase database;
    private DatabaseReference tags, circleDB,userDB;
    private FirebaseAuth currentUser;

    //locationTags and location-interestTags retrieved from database. interest tags will be display according to selected location tags
    private List<String>  dbInterestTags = new ArrayList<>(); //interestTags will be added by parsing through HashMap LocIntTags
    private HashMap<String, Object> locIntTags = new HashMap<>();


    private List<String> autoCompleteItemsList = new ArrayList<>();
    private Dialog interestTagDialog;

    private List<String> selectedInterests = new ArrayList<>();

    //UI elements for location tag selector popup and interest tag selector popup
    private AutoCompleteTextView interestTagEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        setContentView(R.layout.activity_create_circle);

        user = SessionStorage.getUser(CreateCircle.this);

        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        tv_selectInterestTags = findViewById(R.id.create_circle_addinteresttags);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        interestTagsDisplay = findViewById(R.id.selected_interest_tags_display);
        interestTagSelectTitle = findViewById(R.id.create_circle_interest_tag_select_title);

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users");

        tags.child("locationInterestTags").child(user.getDistrict()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locIntTags = (HashMap<String, Object>) snapshot.getValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        btn_createCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cName = circleNameEntry.getText().toString();
                String cDescription = circleDescriptionEntry.getText().toString();

                if (!cName.isEmpty() || !cDescription.isEmpty() || !selectedInterests.isEmpty()) {
                    createCirlce(cName, cDescription);
                } else {
                    Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });


        tv_selectInterestTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialogue for selecting interest tags
                displayInterestTagDialog();
            }
        });

        interestTagSelectTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedInterests.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select Location Tags First", Toast.LENGTH_LONG).show();
                } else {
                    //show dialogue for selecting interest tags
                    dbInterestTags.clear();
                    interestTagsDisplay.removeAllViews();
                    displayInterestTagDialog();
                }

            }
        });


    }

    public void displayInterestTagDialog() {
        User user = SessionStorage.getUser(CreateCircle.this);
        interestTagDialog = new Dialog(CreateCircle.this);
        interestTagDialog.setContentView(R.layout.activity_create_circle_interesttag_dialog); //set dialog view
        interestTagDialog.setCanceledOnTouchOutside(false);
        interestTagDialog.setCancelable(false);

        //initialize elements in popup dialog
        final Button finalizeInterestTag = interestTagDialog.findViewById(R.id.circle_finalize_interest_tags);
        final ChipGroup interestChipGroupPopup = interestTagDialog.findViewById(R.id.circle_interest_tag_chip_group);
        interestTagEntry = interestTagDialog.findViewById(R.id.circle_interest_tags_entry);
        final Button interestTagAdd = interestTagDialog.findViewById(R.id.circle_interest_tag_add_button);

        for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
            List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
            Log.d(TAG, "ENTRY FOR LOC INT " + entry.toString());
            for (String interest : tempInterests) {
                if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                    if(entry.getKey().trim().equals(user.getWard().trim()))
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
                    setInterestTag(interest, interestChipGroupPopup);
                }
            }
        }
        //set tags in ward order
        for(String tag : dbInterestTags)
            setInterestTag(tag,interestChipGroupPopup);

        finalizeInterestTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedInterests.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select A Tag", Toast.LENGTH_LONG).show();
                } else {
                    interestTagDialog.dismiss();
                    tv_selectInterestTags.setVisibility(View.GONE);
                    interestTagSelectTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_black_24dp, 0);
                    for (String interest : selectedInterests)
                        setInterestTag(interest, interestTagsDisplay);
                }
            }
        });

        interestTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(interestTagEntry, InputMethodManager.SHOW_IMPLICIT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            interestTagEntry.setShowSoftInputOnFocus(true);
                        }
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
                        setInterestTag(interestTag, interestChipGroupPopup);
                    } else {
                        interestChipGroupPopup.removeViewAt(dbInterestTags.indexOf(interestTag));
                        setInterestTag(interestTag, interestChipGroupPopup);
                    }
                }
            }
        });

        interestTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        interestTagDialog.show();
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
        if(!name.contains("#"))
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

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chip.getChipBackgroundColor().getDefaultColor() == -9655041) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
                    chip.setTextColor(Color.BLACK);
                    selectedInterests.remove(chip.getText().toString().replace("#",""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedInterests.add(chip.getText().toString().replace("#",""));
                }

                Log.d(TAG, "INTEREST TAG LIST: " + selectedInterests.toString());
            }

        });

        if(selectedInterests.contains(name))
            chipGroupLocation.addView(chip, selectedInterests.indexOf(name));
        else
            chipGroupLocation.addView(chip);

        interestTagEntry.setText("#");
        interestTagEntry.setSelection(interestTagEntry.getText().length());
    }

    public void createCirlce(String cName, String cDescription) {

        User user = SessionStorage.getUser(CreateCircle.this);
        circleDB = database.getReference("Circles");
        currentUser=FirebaseAuth.getInstance();

        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);

        String myCircleID = circleDB.push().getKey();
        String creatorUserID = currentUser.getCurrentUser().getUid();
        String acceptanceType = acceptanceButton.getText().toString();
        String creatorName = currentUser.getCurrentUser().getDisplayName();

        //convert selectedInterests into hashmap
        HashMap<String, Boolean> interestHashmap = new HashMap<>();
        for(String interest : selectedInterests)
            interestHashmap.put(interest, true);

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);

        //updating circles
        Circle circle = new Circle (myCircleID, cName, cDescription, acceptanceType, creatorUserID, creatorName, interestHashmap, tempUserForMemberList, null);
        circleDB.child(user.getDistrict()).child(myCircleID).setValue(circle);

        //updating tags
        for (String i : selectedInterests) {
            tags.child("interestTags").child(user.getDistrict()).child(i).setValue(true);
            tags.child("locationInterestTags").child(user.getDistrict().trim()).child(user.getWard().trim()).child(i).setValue(true);
        }

        //updating user
        int createdProjects = user.getCreatedCircles();
        createdProjects = createdProjects + 1;
        userDB.child(currentUser.getUid()).child("createdProjects").setValue(createdProjects);

        //navigate back to explore. new circle will be available in workbench
        startActivity(new Intent(CreateCircle.this, Explore.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent about_intent = new Intent(this, Explore.class);
        startActivity(about_intent);
        finish();
    }
}
