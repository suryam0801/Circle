package circleapp.circlepackage.circle.Login;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;


import circleapp.circlepackage.circle.Helpers.RuntimePermissionHelper;
import circleapp.circlepackage.circle.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class EntryPage extends AppCompatActivity {

    private Button agreeContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_page);

        RuntimePermissionHelper runtimePermissionHelper = new RuntimePermissionHelper(EntryPage.this);

        agreeContinue = findViewById(R.id.agreeandContinueEntryPage);
        agreeContinue.setOnClickListener(view -> {
            runtimePermissionHelper.requestPermissionsIfDenied(ACCESS_FINE_LOCATION);
            startActivity(new Intent(EntryPage.this, PhoneLogin.class));
        });
    }
}
