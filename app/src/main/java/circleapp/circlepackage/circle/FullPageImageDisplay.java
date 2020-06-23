package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class FullPageImageDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_page_image_display);

        String uri = getIntent().getStringExtra("uri");

        PhotoView photoView = findViewById(R.id.full_page_photo_view);
        Glide.with(this)
                .load(uri)
                .into(photoView);
    }
}