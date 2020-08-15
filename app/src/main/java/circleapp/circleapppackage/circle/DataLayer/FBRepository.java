package circleapp.circleapppackage.circle.DataLayer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.ReportAbuse;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ui.MainActivity;

public class FBRepository {
    private GlobalVariables globalVariables = new GlobalVariables();

    public void setPersistenceEnabled(Context context, boolean toggle){
        SharedPreferences persistenceCheckPrefs = context.getSharedPreferences("PERSISTENCECHECK", Activity.MODE_PRIVATE);
        if (persistenceCheckPrefs.getBoolean(MainActivity.class.getCanonicalName(), true)) {
            persistenceCheckPrefs.edit().putBoolean(MainActivity.class.getCanonicalName(),false).apply();
            globalVariables.getFBDatabase().setPersistenceEnabled(toggle);
        }
    }

    public void signOutAuth() {
        globalVariables.getAuthenticationToken().signOut();
    }

    public FirebaseAuth getAuthToken() {
        return globalVariables.getAuthenticationToken();
    }

    public void addDistrict(String district) {
        globalVariables.getFBDatabase().getReference("Locations").child(district).setValue(true);
    }
    public void addContact(String phn_num,String uid) {
        globalVariables.getFBDatabase().getReference("Contacts").child(phn_num).setValue(uid);
    }

    public void createReportAbuse(Context context, String circleID, String broadcastID, String commentID, String creatorID, String userID, String reportType) {
        String id = HelperMethodsUI.uuidGet();
        ReportAbuse reportAbuse = new ReportAbuse(id, circleID, broadcastID, commentID, creatorID, userID, reportType);
        if (globalVariables.getAuthenticationToken().getCurrentUser().getUid() == creatorID) {
            Toast.makeText(context, "Stop Reporting your own Content", Toast.LENGTH_SHORT).show();
        } else {

            globalVariables.getFBDatabase().getReference("ReportAbuse").child(id).setValue(reportAbuse);
        }
    }

}
