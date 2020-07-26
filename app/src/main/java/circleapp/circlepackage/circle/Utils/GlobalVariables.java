package circleapp.circlepackage.circle.Utils;

import android.app.Application;
import android.net.Uri;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import circleapp.circlepackage.circle.data.LocalObjectModels.LoginUserObject;
import circleapp.circlepackage.circle.data.LocalObjectModels.TempLocation;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class GlobalVariables extends Application {
    public GlobalVariables(){
    }

    private static Uri intentUri;
    private static FirebaseDatabase database;
    private static User currentUser;
    private static Circle currentCircle;
    private static Broadcast currentBroadcast;
    private static List<Broadcast> currentBroadcastList;
    private static LoginUserObject currentLoginUserObject;
    private static TempLocation currentTempLocation;


    public FirebaseDatabase getDatabaseInstance(){
        return database.getInstance();
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public void saveCurrentUser(User user){
        this.currentUser = user;
    }

    public Circle getCurrentCircle(){
        return currentCircle;
    }

    public void saveCurrentCircle(Circle circle){
        this.currentCircle = circle;
    }

    public void saveCurrentBroadcast(Broadcast broadcast){this.currentBroadcast = broadcast;}

    public Broadcast getCurrentBroadcast(){return currentBroadcast;}

    public List<Broadcast> getCurrentBroadcastList(){return currentBroadcastList;}

    public void saveCurrentBroadcastList(List<Broadcast> broadcastList){this.currentBroadcastList = broadcastList;}

    public static LoginUserObject getCurrentLoginUserObject() {
        return currentLoginUserObject;
    }

    public static void saveCurrentLoginUserObject(LoginUserObject currentLoginUserObject) {
        GlobalVariables.currentLoginUserObject = currentLoginUserObject;
    }

    public static TempLocation getCurrentTempLocation() {
        return currentTempLocation;
    }

    public static void saveCurrentTempLocation(TempLocation currentTempLocation) {
        GlobalVariables.currentTempLocation = currentTempLocation;
    }
    public Uri getIntentUri() { return intentUri; }
    public void saveIntentUri(Uri intentUri) { this.intentUri = intentUri; }
}
