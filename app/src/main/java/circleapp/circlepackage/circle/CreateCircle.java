package circleapp.circlepackage.circle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Objects;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView tv_selectInterestTags, tv_selectLocationTags, locationTagSelectTitle, interestTagSelectTitle;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton acceptanceButton;
    private ChipGroup locationTagsDisplay, interestTagsDisplay;

    private FirebaseDatabase database;
    private DatabaseReference tags, circleDB;
    private FirebaseAuth currentUser;
    private FirebaseFirestore db;

    //locationTags and location-interestTags retrieved from database. interest tags will be display according to selected location tags
    private List<String> dbLocationTags = new ArrayList<>(), dbInterestTags = new ArrayList<>(); //interestTags will be added by parsing through HashMap LocIntTags
    private HashMap<String, Object> locIntTags = new HashMap<>();

    private Dialog locationTagDialog, interestTagDialog;

    private List<String> selectedLocations = new ArrayList<>(), selectedInterests = new ArrayList<>();

    //UI elements for location tag selector popup and interest tag selector popup
    private EditText locationTagEntry, interestTagEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        setContentView(R.layout.activity_create_circle);

        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        tv_selectInterestTags = findViewById(R.id.create_circle_addinteresttags);
        tv_selectLocationTags = findViewById(R.id.create_circle_addlocationtags);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        locationTagsDisplay = findViewById(R.id.selected_location_tags_display);
        interestTagsDisplay = findViewById(R.id.selected_interest_tags_display);
        locationTagSelectTitle = findViewById(R.id.create_circle_location_tag_select_title);
        interestTagSelectTitle = findViewById(R.id.create_circle_interest_tag_select_title);

        database = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();
        tags = database.getReference("Tags");

        tags.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> tagsDBRetrieved = (HashMap<String, Object>) snapshot.getValue();

                dbLocationTags = new ArrayList<>(((HashMap<String, Boolean>) tagsDBRetrieved.get("locationTags")).keySet());
                locIntTags = (HashMap<String, Object>) tagsDBRetrieved.get("locationInterestTags");
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

                if (!cName.isEmpty() || !cDescription.isEmpty() || !selectedLocations.isEmpty() || !selectedInterests.isEmpty()) {
                    createCirlce(cName, cDescription);
                } else {
                    Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_selectLocationTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialogue for selecting location tags
                displayLocationTagDialog();
            }
        });

        tv_selectInterestTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialogue for selecting interest tags
                if (selectedLocations.isEmpty())
                    Toast.makeText(getApplicationContext(), "Select Location Tags First", Toast.LENGTH_LONG).show();
                else
                    displayInterestTagDialog();

            }
        });

        locationTagSelectTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialogue for selecting location tags
                locationTagsDisplay.removeAllViews();
                displayLocationTagDialog();
            }
        });

        interestTagSelectTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedLocations.isEmpty()) {
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
        interestTagDialog = new Dialog(CreateCircle.this);
        interestTagDialog.setContentView(R.layout.activity_create_circle_interesttag_dialog); //set dialog view

        //initialize elements in popup dialog
        final Button finalizeInterestTag = interestTagDialog.findViewById(R.id.circle_finalize_interest_tags);
        final ChipGroup interestChipGroupPopup = interestTagDialog.findViewById(R.id.circle_interest_tag_chip_group);
        interestTagEntry = interestTagDialog.findViewById(R.id.circle_interest_tags_entry);
        final Button interestTagAdd = interestTagDialog.findViewById(R.id.circle_interest_tag_add_button);

        //geting location tags and location-interestTags from the database
        for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
            if (selectedLocations.contains(entry.getKey())) {
                List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
                for (String interest : tempInterests) {
                    if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                        dbInterestTags.add(interest);
                        setInterestTag(interest, interestChipGroupPopup);
                    }
                }
                //this for loop is used when the user wants to edit interest tag choices
                for (String interest : selectedInterests) { //add selected interests as options even if they are not in the location-interest list
                    if (!dbInterestTags.contains(interest)) {
                        dbInterestTags.add(interest);
                        setInterestTag(interest, interestChipGroupPopup);
                    }
                }
            }
        }

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
                    if(!dbInterestTags.contains(interestTag))
                        dbInterestTags.add(interestTag);

                    setInterestTag(interestTag, interestChipGroupPopup);
                }
            }
        });

        interestTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        interestTagDialog.show();
    }

    public void setInterestTag(final String name, ChipGroup interestChipGroupPopup) {
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

        interestChipGroupPopup.addView(chip);
        interestTagEntry.setText("#");
        interestTagEntry.setSelection(interestTagEntry.getText().length());
    }

    private void displayLocationTagDialog() {
        locationTagDialog = new Dialog(CreateCircle.this);
        locationTagDialog.setContentView(R.layout.activity_create_circle_locationtag_dialog); //set dialog view

        //initialize elements in popup dialog
        final Button finalizeLocationTag = locationTagDialog.findViewById(R.id.circle_finalize_location_tags);
        final ChipGroup locationChipGroupPopup = locationTagDialog.findViewById(R.id.circle_location_tag_chip_group);
        locationTagEntry = locationTagDialog.findViewById(R.id.circle_location_tags_entry);
        final Button locationTagAdd = locationTagDialog.findViewById(R.id.circle_location_tag_add_button);


        if (!dbLocationTags.isEmpty()) {
            for (String loc : dbLocationTags)
                setLocationTag(loc, locationChipGroupPopup);
        }

        finalizeLocationTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedLocations.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Select A Tag", Toast.LENGTH_LONG).show();
                } else {
                    locationTagDialog.dismiss();
                    tv_selectLocationTags.setVisibility(View.GONE);
                    locationTagSelectTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_black_24dp, 0);
                    for (String location : selectedLocations)
                        setLocationTag(location, locationTagsDisplay);
                    Log.d(TAG, "LOCATION TAG LIST: " + selectedLocations.toString());
                }
            }
        });

        locationTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(locationTagEntry, InputMethodManager.SHOW_IMPLICIT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            locationTagEntry.setShowSoftInputOnFocus(true);
                        }
                        locationTagEntry.setText("#");
                        locationTagEntry.setSelection(locationTagEntry.getText().length());
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

        locationTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTag = locationTagEntry.getText().toString().replace("#", "");
                if (!locationTag.isEmpty()) {
                    selectedLocations.add(locationTag);
                    if(!dbLocationTags.contains(locationTag))
                        dbLocationTags.add(locationTag);
                    setLocationTag(locationTag, locationChipGroupPopup);
                }
            }
        });

        locationTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        locationTagDialog.show();

    }

    private void setLocationTag(final String name, ChipGroup locationChipGroupPopup) {
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

        if (selectedLocations.contains(name)) {
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
                    selectedLocations.remove(chip.getText().toString().replace("#",""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedLocations.add(chip.getText().toString().replace("#", ""));
                }

            }

        });

        locationChipGroupPopup.addView(chip);
        locationTagEntry.setText("#");
        locationTagEntry.setSelection(locationTagEntry.getText().length());
    }

    public void createCirlce(String cName, String cDescription) {

        circleDB = database.getReference("Circles");
        currentUser=FirebaseAuth.getInstance();

        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);

        String myCircleID = circleDB.push().getKey();
        String creatorUserID = currentUser.getCurrentUser().getUid();
        String acceptanceType = acceptanceButton.getText().toString();
        String creatorName = currentUser.getCurrentUser().getDisplayName();

        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, creatorUserID, creatorName, selectedLocations, selectedInterests);

        for (String l : selectedLocations)
            tags.child("locationTags").child(l).setValue(true);
        for (String i : selectedInterests)
            tags.child("interestTags").child(i).setValue(true);


        for (String loc : selectedLocations) {
            for(String i : selectedInterests) {
                tags.child("locationInterestTags").child(loc).child(i).setValue(true);
            }
        }

        //add circle in users realtime database
        circleDB.push().setValue(circle);

        User user = SessionStorage.getUser(CreateCircle.this);

        int createdProjects = user.getCreatedProjects();
        createdProjects = createdProjects + 1;

        //update createdCircle count in user profile
        db.collection("Users").document(currentUser.getInstance().getUid()).update("createdProjects", createdProjects)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "JOB SUCCESSFUL!!!!");
                onBackPressed();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent about_intent = new Intent(this, Explore.class);
        startActivity(about_intent);
        about_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
}
