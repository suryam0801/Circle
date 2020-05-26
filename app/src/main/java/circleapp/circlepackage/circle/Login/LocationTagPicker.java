package circleapp.circlepackage.circle.Login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.R;

public class LocationTagPicker extends AppCompatActivity {

    private String TAG = LocationTagPicker.class.getSimpleName();

    //Declare all UI elements for the LocationTagPicker Activity
    private String loc, fName, lName, userId, downloadUri, contact;
    private List<String> suggestLocList = new ArrayList<>();
    private List<String> selectedLocList = new ArrayList<>();
    private List<String> loadlocList = new ArrayList<String>();
    ;
    private Button setInterestTags, locationTagAdd;
    private ChipGroup chipGroup;
    private AutoCompleteTextView locationTagEntry;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference tags;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_location_tag_picker);
        //To set the Fullscreen
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();

        // Initialize all UI elements in the LocationTagPicker activity
        setInterestTags = findViewById(R.id.setInterestTags);
        chipGroup = findViewById(R.id.location_tag_chip_group);
        locationTagEntry = (AutoCompleteTextView) findViewById(R.id.location_tags_entry);
        locationTagAdd = findViewById(R.id.location_tag_add_button);
        fName = getIntent().getStringExtra("fName");
        lName = getIntent().getStringExtra("lName");
        downloadUri = getIntent().getStringExtra("uri");
        contact = getIntent().getStringExtra("contact");

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        tags = database.getReference("Tags");
        tags.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> tagsDBRetrieved = (HashMap<String, Object>) snapshot.getValue();

                Log.d(TAG, tagsDBRetrieved.get("locationTags").toString());
                loadlocList = new ArrayList<>(((HashMap<String, Boolean>) tagsDBRetrieved.get("locationTags")).keySet());
                chipGroup.removeAllViews();
                if (!loadlocList.isEmpty()) {
                    for (String loc : loadlocList) {
                        setTag(loc);
                        suggestLocList.add("#"+loc);
                    }
                    Log.d(TAG,"Suggestion"+suggestLocList.toString());
                    String[] arr = suggestLocList.toArray(new String[0]);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line, arr);
                    locationTagEntry.setThreshold(1);
                    locationTagEntry.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //After Selecting loaction tags user has to set the interest tags
        setInterestTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedLocList.isEmpty()) {
                    Intent intent = new Intent(LocationTagPicker.this, InterestTagPicker.class);
                    intent.putExtra("fName", fName);
                    intent.putExtra("lName", lName);
                    intent.putExtra("contact", contact);
                    intent.putExtra("locationTags", selectedLocList.toString());
                    if (downloadUri != null)
                        intent.putExtra("uri", downloadUri.toString());

                    startActivity(intent);
                } else {
                    Toast.makeText(LocationTagPicker.this, "Select tags before you continue", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Touch listener to autoenter the # as prefix when user try to enter the new location tag
        locationTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(locationTagEntry, InputMethodManager.SHOW_IMPLICIT);
                        locationTagEntry.setShowSoftInputOnFocus(true);
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


        //the add button in edittext to add the new location in the list
        locationTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTag = locationTagEntry.getText().toString().replace("#", "");
                if (!locationTag.isEmpty()) {
                    selectedLocList.add(locationTag);
                    if (!loadlocList.contains(locationTag))
                        loadlocList.add(locationTag);
                    setTag(locationTag);
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
        //chip Text Color
        chip.setTextColor(Color.BLACK);
        //chip backgroud color
        chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));

        if (selectedLocList.contains(name)) {
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
                    selectedLocList.remove(chip.getText().toString());
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedLocList.add(chip.getText().toString());
                }

                Log.d(TAG, "SELECTED LOCATION TAG LIST: " + selectedLocList.toString());
            }

        });

        //Add a chip to the Chipgroup
        chipGroup.addView(chip);

        //set the # in edittext after adding the new location
        locationTagEntry.setText("#");
        locationTagEntry.setSelection(locationTagEntry.getText().length());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Getting the Location access permission from the user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        } else {

        }
    }
}
