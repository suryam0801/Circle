package circleapp.circleapppackage.circle.Utils;

import android.app.Application;
import android.net.Uri;
import android.view.View;

import com.github.lzyzsd.randomcolor.RandomColor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

import circleapp.circleapppackage.circle.Model.LocalObjectModels.LoginUserObject;
import circleapp.circleapppackage.circle.Model.LocalObjectModels.TempLocation;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;

public class GlobalVariables extends Application {
    public GlobalVariables(){
    }

    private static final FirebaseAuth authenticationToken = FirebaseAuth.getInstance();
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static final FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
    public static User currentUser;
    public static List<String > usersList, tempUsersList;
    public static Circle currentCircle;
    public static Broadcast currentBroadcast;
    public static List<Broadcast> currentBroadcastList;
    public static LoginUserObject currentLoginUserObject;
    public static TempLocation currentTempLocation;
    public static Uri TempdownloadLink;
    public int involvedCircles = 0;
    RandomColor randomColor = new RandomColor();
    public int[] colors = randomColor.randomColor(10);

    public static View view;

    public static View getEditView(){
        return view;
    }

    public static void setEditView(View view){
        GlobalVariables.view = view;
    }

    public static List<String > getUsersList() {
        return usersList;
    }

    public static void setUsersList( List<String > usersList) {
        GlobalVariables.usersList = usersList;
    }

    public static List<String> getTempUsersList() {
        return tempUsersList;
    }

    public static void setTempUsersList(List<String> tempUsersList) {
        GlobalVariables.tempUsersList = tempUsersList;
    }

    public int[] getColorsForUsername(){
        return colors;
    }

    public int getInvolvedCircles() {
        return involvedCircles;
    }

    public void setInvolvedCircles(int involvedCircles) {
        this.involvedCircles = involvedCircles;
    }

    public Uri getTempdownloadLink() {
        return TempdownloadLink;
    }

    public void setTempdownloadLink(Uri tempdownloadLink) {
        TempdownloadLink = tempdownloadLink;
    }

    public FirebaseAuth getAuthenticationToken() {
        return authenticationToken;
    }

    public FirebaseDatabase getFBDatabase() {
        return database;
    }

    public FirebaseStorage getFirebaseStorage() {
        return mFirebaseStorage;
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
}