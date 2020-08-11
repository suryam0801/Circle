package circleapp.circlepackage.circle.ui.CircleWall;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class FullPageImageDisplay extends AppCompatActivity {

    private int indexOfBroadcast = 0;
    private ImageButton backButton;
    private LinearLayout qrCodeHeader;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_image_display);
        backButton = findViewById(R.id.bck_fullpage_image_btn);
        qrCodeHeader = findViewById(R.id.qr_code_layout);

        String uri = getIntent().getStringExtra("uri");
        indexOfBroadcast = getIntent().getIntExtra("indexOfBroadcast",0);
        Boolean qrCode = getIntent().getBooleanExtra("QRCode",false);

        if(qrCode)
            qrCodeHeader.setVisibility(View.VISIBLE);

        PhotoView photoView = findViewById(R.id.full_page_photo_view);
        Glide.with(this)
                .load(uri)
                .into(photoView);
        backButton.setOnClickListener(v->{
            onBackPressed();
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        Intent intent = new Intent(this, CircleWall.class);
        intent.putExtra("indexOfBroadcast", indexOfBroadcast);
        startActivity(intent);
    }
}