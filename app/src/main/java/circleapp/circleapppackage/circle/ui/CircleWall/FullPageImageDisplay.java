package circleapp.circleapppackage.circle.ui.CircleWall;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageView.FullPageBroadcastCardView;

public class FullPageImageDisplay extends AppCompatActivity {

    private int indexOfBroadcast = 0;
    private ImageButton backButton;
    private LinearLayout qrCodeHeader;
    private TextView qrCodeHeaderTxt;
    private GlobalVariables globalVariables = new GlobalVariables();
    private int discussionPos = -1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_image_display);
        backButton = findViewById(R.id.bck_fullpage_image_btn);
        qrCodeHeader = findViewById(R.id.qr_code_layout);
        qrCodeHeaderTxt = findViewById(R.id.full_page_image_header_text);

        discussionPos = getIntent().getIntExtra("indexOfComment",0);
        String uri = getIntent().getStringExtra("uri");
        indexOfBroadcast = getIntent().getIntExtra("indexOfBroadcast",0);
        Boolean qrCode = getIntent().getBooleanExtra("QRCode",false);

        if(qrCode)
            qrCodeHeader.setVisibility(View.VISIBLE);
        qrCodeHeaderTxt.setText("Scan QR Code to join "+globalVariables.getCurrentCircle().getName());

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
        if(discussionPos!=-1){
                Intent intent = new Intent(this, FullPageBroadcastCardView.class);
                intent.putExtra("broadcastPosition", discussionPos);
                startActivity(intent);
                finishAfterTransition();
        }
        else {
            finishAfterTransition();
            Intent intent = new Intent(this, CircleWall.class);
            intent.putExtra("indexOfBroadcast", indexOfBroadcast);
            startActivity(intent);
        }
    }
}