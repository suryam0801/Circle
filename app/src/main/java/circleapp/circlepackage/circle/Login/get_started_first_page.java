package circleapp.circlepackage.circle.Login;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import circleapp.circlepackage.circle.MainActivity;
import circleapp.circlepackage.circle.R;

public class get_started_first_page extends AppCompatActivity {

    TextView skip;
    Button start;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_started_first_page);
        skip = findViewById(R.id.skip_get_started);
        start = findViewById(R.id.getStartedButton);

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(get_started_first_page.this, EntryPage.class));
                finish();
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(get_started_first_page.this, get_started_second_page.class));
                finish();
            }
        });

    }
}

