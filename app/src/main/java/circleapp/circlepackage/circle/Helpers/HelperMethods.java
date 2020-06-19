package circleapp.circlepackage.circle.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateFormat;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;

public class HelperMethods {

    public static int returnIndexOfCircleList(List<Circle> circleList, Circle circle){
        int position = 0;
        for(Circle c : circleList){
            if(c.getId().equals(circle.getId()))
                return position;

            position++;
        }
        return position;
    }

    public static boolean isMemberOfCircle (Circle circle, String uID){
        boolean isMember = false;
        if(circle.getMembersList()!=null){
            for(String memberId : circle.getMembersList().keySet()){
                if(memberId.equals(uID))
                    isMember = true;
            }
        }
        return isMember;
    }

    public static boolean listContainsCircle (List<Circle> circleList, Circle circle){
        boolean containsCircle = false;
        for(Circle c : circleList){
            if(c.getId().equals(circle.getId()))
                containsCircle = true;
        }
        return containsCircle;
    }

    public static void showShareCirclePopup(Circle c, Activity activity) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage = "\nCome join my circle: " + c.getName() + "\n\n";
            shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + c.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            activity.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }

    public static int numberOfApplicants(Circle c, User user){
        int numOfApplicants = 0;
        if(c.getApplicantsList()!=null && user.getUserId().equals(c.getCreatorID())){
            numOfApplicants = c.getApplicantsList().size();
        }
        return numOfApplicants;
    }

    public static int newNotifications(Circle c, User user){
        int newNotifs = 0;
        if(user.getNotificationsAlert() != null && user.getNotificationsAlert().containsKey(c.getId())){
            int userRead = user.getNotificationsAlert().get(c.getId());

            if(c.getNoOfBroadcasts() > userRead)
                newNotifs = c.getNoOfBroadcasts() - userRead;

        }
        return newNotifs;
    }

    public static String convertIntoDateFormat(String dateDormat, long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return DateFormat.format("", cal).toString();
    }

    public static GradientDrawable gradientRectangleDrawableSetter(int radius){
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }
}
