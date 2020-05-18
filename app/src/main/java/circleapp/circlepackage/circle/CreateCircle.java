package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.chip.ChipGroup;

public class CreateCircle extends AppCompatActivity {

    //Declare all UI elements for the CreateCircle Activity
    private EditText circleName, circleDescription;
    private TextView tv_selectInterestTags, tv_selectLocationTags;
    private Button btn_createCircle;
    private ImageButton back;
    private RadioGroup acceptanceGroup;
    private RadioButton automaticAcceptance, reviewAcceptance;
    private ChipGroup locationTagsDisplay, interestTagsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_circle);

        //Initialize all UI elements in the CreateCircle activity
        circleName = findViewById(R.id.create_circle_Name);
        circleDescription = findViewById(R.id.create_circle_Description);
        acceptanceGroup = findViewById(R.id.acceptanceRadioGroup);
        automaticAcceptance = findViewById(R.id.acceptanceTypeAutomatic);
        reviewAcceptance = findViewById(R.id.acceptanceTypeReview);
        tv_selectInterestTags = findViewById(R.id.create_circle_addinteresttags);
        tv_selectLocationTags = findViewById(R.id.create_circle_addlocationtags);
        btn_createCircle = findViewById(R.id.create_circle_submit);
        back = findViewById(R.id.bck_create);
        locationTagsDisplay = findViewById(R.id.selected_location_tags_display);
        interestTagsDisplay = findViewById(R.id.selected_interest_tags_display);

    }
}
