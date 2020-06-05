package circleapp.circlepackage.circle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Login.EntryPage;
import circleapp.circlepackage.circle.ObjectModels.User;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //To set the Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.RGB_565);
//        getSupportActionBar().hide();


        readFromFile(getApplicationContext());

    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("user.txt");

            if ( inputStream != null || !inputStream.equals("") ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                User user = new Gson().fromJson(ret, User.class);
                SessionStorage.saveUser(MainActivity.this, user);

                startActivity(new Intent(MainActivity.this, ExploreTabbedActivity.class));
                finish();

            } else {
                startActivity(new Intent(MainActivity.this, EntryPage.class));
                finish();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            startActivity(new Intent(MainActivity.this, EntryPage.class));
            finish();
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            startActivity(new Intent(MainActivity.this, EntryPage.class));
            finish();
        }

        return ret;
    }

    @Override
    protected void onStart() {
        super.onStart();

        readFromFile(getApplicationContext());

    }
}