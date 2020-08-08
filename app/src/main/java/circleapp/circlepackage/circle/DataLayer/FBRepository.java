package circleapp.circlepackage.circle.DataLayer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Contacts;
import circleapp.circlepackage.circle.Model.ObjectModels.ReportAbuse;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.MainActivity;

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

    public void makeFeedbackEntry(Map<String, Object> map) {
        globalVariables.getFBDatabase().getReference("UserFeedback").child(globalVariables.getCurrentUser().getDistrict()).push().setValue(map);
    }

    public void addDistrict(String district) {
        globalVariables.getFBDatabase().getReference("Locations").child(district).setValue(true);
    }
    public void addContact(Contacts contacts) {
        globalVariables.getFBDatabase().getReference("Contacts").child(HelperMethodsUI.uuidGet()).setValue(contacts);
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
