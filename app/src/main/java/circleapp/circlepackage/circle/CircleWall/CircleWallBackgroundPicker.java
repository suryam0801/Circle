package circleapp.circlepackage.circle.CircleWall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import circleapp.circlepackage.circle.Helpers.SessionStorage;
import circleapp.circlepackage.circle.R;

public class CircleWallBackgroundPicker extends AppCompatActivity {

    ImageView bg1, bg2, bg3, bg4, bg5, bg6, bg7, bg8, bg9, bg10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall_background_picker);

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

        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_1)).centerCrop().into(bg1);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_2)).centerCrop().into(bg2);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_3)).centerCrop().into(bg3);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_4)).centerCrop().into(bg4);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_5)).centerCrop().into(bg5);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_6)).centerCrop().into(bg6);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_7)).centerCrop().into(bg7);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_8)).centerCrop().into(bg8);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_9)).centerCrop().into(bg9);
        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.circle_wall_background_white)).centerCrop().into(bg10);

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

    public void backToCircleWall(){
        startActivity(new Intent(CircleWallBackgroundPicker.this, CircleWall.class));
        finish();
    }
}