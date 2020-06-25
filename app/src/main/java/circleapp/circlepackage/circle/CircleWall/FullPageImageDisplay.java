package circleapp.circlepackage.circle.CircleWall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.R;

public class FullPageImageDisplay extends AppCompatActivity {

    int indexOfBroadcast = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_image_display);

        String uri = getIntent().getStringExtra("uri");

        indexOfBroadcast = getIntent().getIntExtra("indexOfBroadcast",0);

        PhotoView photoView = findViewById(R.id.full_page_photo_view);
        Glide.with(this)
                .load(uri)
                .into(photoView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CircleWall.class);
        intent.putExtra("indexOfBroadcast", indexOfBroadcast);
        startActivity(intent);
        finish();
    }
}