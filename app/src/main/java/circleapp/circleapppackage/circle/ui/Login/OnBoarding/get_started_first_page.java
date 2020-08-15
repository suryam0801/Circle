package circleapp.circleapppackage.circle.ui.Login.OnBoarding;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.ui.Login.EntryPage.EntryPage;

public class get_started_first_page extends AppCompatActivity {

    TextView skip;
    Button start;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_started_first_page);
        skip = findViewById(R.id.skip_get_started);
        start = findViewById(R.id.getStartedButton);
        HelperMethodsUI.increaseTouchArea(skip);

        skip.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                finishAfterTransition();
                startActivity(new Intent(get_started_first_page.this, EntryPage.class));
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                finishAfterTransition();
                startActivity(new Intent(get_started_first_page.this, get_started_second_page.class));
            }
        });

    }
}

