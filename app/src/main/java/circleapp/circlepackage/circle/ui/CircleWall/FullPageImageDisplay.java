package circleapp.circlepackage.circle.ui.CircleWall;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class FullPageImageDisplay extends AppCompatActivity {

    private int indexOfBroadcast = 0;

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        finishAfterTransition();
        Intent intent = new Intent(this, CircleWall.class);
        intent.putExtra("indexOfBroadcast", indexOfBroadcast);
        startActivity(intent);
    }
}