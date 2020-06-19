package circleapp.circlepackage.circle.Login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import circleapp.circlepackage.circle.R;

public class get_started_second_page extends AppCompatActivity {
    Button next;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_started_second_page);

        next = findViewById(R.id.nextButton);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(get_started_second_page.this, get_started_third_page.class));
                finish();
            }
        });
    }
}
