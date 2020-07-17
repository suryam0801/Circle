package circleapp.circlepackage.circle.ui.Login.OnBoarding;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.R;

public class get_started_third_page extends AppCompatActivity {

    Button startentry;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_started_third_page);

        startentry = findViewById(R.id.startEntryButton);

        startentry.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                finishAfterTransition();
                startActivity(new Intent(get_started_third_page.this, EntryPage.class));
            }
        });

    }
}
