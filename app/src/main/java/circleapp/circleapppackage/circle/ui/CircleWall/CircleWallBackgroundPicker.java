package circleapp.circleapppackage.circle.ui.CircleWall;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import circleapp.circleapppackage.circle.Helpers.SessionStorage;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class CircleWallBackgroundPicker extends AppCompatActivity {

    private ImageView bg1, bg2, bg3, bg4, bg5, bg6, bg7, bg8, bg9, bg10;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall_background_picker);

        setWallpapers();
        wallpaperClickListeners();

    }
    private void setWallpapers(){
        bg1 = findViewById(R.id.circlWallBackground1);
        bg2 = findViewById(R.id.circlWallBackground2);
        bg3 = findViewById(R.id.circlWallBackground3);
        bg4 = findViewById(R.id.circlWallBackground4);
        bg5 = findViewById(R.id.circlWallBackground5);
        bg6 = findViewById(R.id.circlWallBackground6);
        bg7 = findViewById(R.id.circlWallBackground7);
        bg8 = findViewById(R.id.circlWallBackground8);
        bg9 = findViewById(R.id.circlWallBackground9);
        bg10 = findViewById(R.id.circlWallBackground10);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_1)).encodeQuality(50).centerCrop().into(bg1);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_2)).encodeQuality(50).centerCrop().into(bg2);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_3)).encodeQuality(50).centerCrop().into(bg3);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_4)).encodeQuality(50).centerCrop().into(bg4);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_5)).encodeQuality(50).centerCrop().into(bg5);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_6)).encodeQuality(50).centerCrop().into(bg6);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_7)).encodeQuality(50).centerCrop().into(bg7);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_8)).encodeQuality(50).centerCrop().into(bg8);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_9)).encodeQuality(50).centerCrop().into(bg9);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_white)).encodeQuality(50).centerCrop().into(bg10);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void wallpaperClickListeners(){
        bg1.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg1");
            backToCircleWall();
        });
        bg2.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg2");
            backToCircleWall();
        });
        bg3.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg3");
            backToCircleWall();
        });
        bg4.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg4");
            backToCircleWall();
        });
        bg5.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg5");
            backToCircleWall();
        });
        bg6.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg6");
            backToCircleWall();
        });
        bg7.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg7");
            backToCircleWall();
        });
        bg8.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg8");
            backToCircleWall();
        });
        bg9.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg9");
            backToCircleWall();
        });
        bg10.setOnClickListener(view -> {
            SessionStorage.saveCircleWallBgImage(CircleWallBackgroundPicker.this, "bg10");
            backToCircleWall();
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void backToCircleWall(){
        finishAfterTransition();
        startActivity(new Intent(CircleWallBackgroundPicker.this, CircleWall.class));
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed(){
        finishAfterTransition();
        startActivity(new Intent(CircleWallBackgroundPicker.this, CircleWall.class));
    }
}