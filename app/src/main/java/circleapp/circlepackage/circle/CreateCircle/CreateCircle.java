package circleapp.circlepackage.circle.CreateCircle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Helpers.AnalyticsLogEvents;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class CreateCircle extends AppCompatActivity {

    private String TAG = CreateCircle.class.getSimpleName();

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleNameEntry, circleDescriptionEntry;
    private TextView typePrompt;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton acceptanceButton;
    private Button typeInfoButton;
    private User user;

    private FirebaseDatabase database;
    private DatabaseReference tags, circleDB, userDB;
    private FirebaseAuth currentUser;

    AnalyticsLogEvents analyticsLogEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); //disables onscreen keyboard popup each time activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_create_circle);

        analyticsLogEvents = new AnalyticsLogEvents();
        user = SessionStorage.getUser(CreateCircle.this);

        //Initialize all UI elements in the CreateCircle activity
        circleNameEntry = findViewById(R.id.create_circle_Name);
        circleDescriptionEntry = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        typeInfoButton = findViewById(R.id.circle_type_info_button);
        typePrompt = findViewById(R.id.circle_type_tip_prompt);

        analyticsLogEvents.logEvents(CreateCircle.this, "create_circle", "new_circle","on_button_click");

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users").child(user.getUserId());

        btn_createCircle.setOnClickListener(view -> {
            String cName = circleNameEntry.getText().toString().trim();
            String cDescription = circleDescriptionEntry.getText().toString().trim();
            if (!cName.isEmpty() && !cDescription.isEmpty()) {
                analyticsLogEvents.logEvents(CreateCircle.this, "created_circle", "new_circle_created","on_button_click");
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

    }

    public void createCirlce(String cName, String cDescription) {

        User user = SessionStorage.getUser(CreateCircle.this);
        circleDB = database.getReference("Circles");
        currentUser = FirebaseAuth.getInstance();
        int radioId = acceptanceGroup.getCheckedRadioButtonId();
        acceptanceButton = findViewById(radioId);

        String category = getIntent().getStringExtra("category_name");

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
        if(category.contains("&")){
            Scanner scanner = new Scanner(category);
            scanner.useDelimiter("&");
            interestHashmap.put(scanner.next(), true);
            interestHashmap.put(scanner.next(), true);
        } else {
            interestHashmap.put(category, true);
        }

        HashMap<String, Boolean> tempUserForMemberList = new HashMap<>();
        tempUserForMemberList.put(creatorUserID, true);

        //updating circles
        Circle circle = new Circle(myCircleID, cName, cDescription, acceptanceType, creatorUserID, creatorName,
                interestHashmap, tempUserForMemberList, null, user.getDistrict(), user.getWard(),
                System.currentTimeMillis(),0, 0);

        circleDB.child(myCircleID).setValue(circle);

        int currentCreatedNo = user.getCreatedCircles() + 1;
        user.setCreatedCircles(currentCreatedNo);
        SessionStorage.saveUser(CreateCircle.this, user);
        userDB.child("createdCircles").setValue(currentCreatedNo);

        //navigate back to explore. new circle will be available in workbench
        Intent intent = new Intent(CreateCircle.this, CircleWall.class);
        intent.putExtra("fromCircleWall", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent about_intent = new Intent(this, ExploreTabbedActivity.class);
        analyticsLogEvents.logEvents(CreateCircle.this, "create_circle", "new_circle_bounce","on_back_press");
        startActivity(about_intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}