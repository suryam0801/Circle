package circleapp.circlepackage.circle.Helpers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.DataLayer.CirclePersonnelRepository;
import circleapp.circlepackage.circle.DataLayer.CircleRepository;
import circleapp.circlepackage.circle.DataLayer.FBRepository;
import circleapp.circlepackage.circle.DataLayer.NotificationRepository;
import circleapp.circlepackage.circle.DataLayer.UserRepository;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Notification;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HelperMethodsBL {
    private static GlobalVariables globalVariables = new GlobalVariables();

    //BL
    public static String getCircleIdFromShareURL(String url) {
        String lines[] = url.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            Log.d("URL", lines[i]);
        }
        url = url.replace("https://worfo.app.link/8JMEs34W96/?", "");
        return url;
    }
    //BL
    public static void initializeNewCommentsAlertTimestamp(Broadcast b, User user) {
        HashMap<String, Long> commentTimeStampTemp;
        if (user.getNewTimeStampsComments() == null) {
            //first time viewing any comments
            commentTimeStampTemp = new HashMap<>();
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);
        } else if (user.getNewTimeStampsComments() != null && !user.getNewTimeStampsComments().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            commentTimeStampTemp = new HashMap<>(user.getNewTimeStampsComments());
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);
        }
        UserRepository userRepository = new UserRepository();
        userRepository.updateUserNewTimeStampComments(user.getUserId(), b.getId(), b.getLatestCommentTimestamp());
        globalVariables.saveCurrentUser(user);
    }
    //BL
    public static void initializeNewReadComments(Broadcast b, User user) {
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() == null) {
            //first time viewing any comments
            userNoReadComments = new HashMap<>();
            userNoReadComments.put(b.getId(), b.getNumberOfComments());
            user.setNoOfReadDiscussions(userNoReadComments);
        } else if (user.getNoOfReadDiscussions() != null && !user.getNoOfReadDiscussions().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.put(b.getId(), 0);
            user.setNoOfReadDiscussions(userNoReadComments);
        }
        UserRepository userRepository = new UserRepository();
        userRepository.updateUserNewReadComments(user.getUserId(), b.getId(), b.getNumberOfComments());
        globalVariables.saveCurrentUser(user);
    }
    //BL
    public static void updateUserFields(Broadcast broadcast, String navFrom, User user) {
        HashMap<String, Integer> tempNoOfDiscussion;
        UserRepository userRepository = new UserRepository();
        if (user.getNoOfReadDiscussions() != null)
            tempNoOfDiscussion = new HashMap<>(user.getNoOfReadDiscussions());
        else
            tempNoOfDiscussion = new HashMap<>();

        switch (navFrom) {
            case "create":
                //updating userReadDiscussions after creating the comment
                int updateDiscussionInt;
                if (tempNoOfDiscussion.containsKey(broadcast.getId()))
                    updateDiscussionInt = tempNoOfDiscussion.get(broadcast.getId());
                else
                    updateDiscussionInt = 0;
                tempNoOfDiscussion.put(broadcast.getId(), updateDiscussionInt + 1);
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                userRepository.updateUser(user);
                break;

            case "view":
                tempNoOfDiscussion.put(broadcast.getId(), broadcast.getNumberOfComments());
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                userRepository.updateUser(user);
                break;
        }

        //updating user latest timestamp for that comment
        HashMap<String, Long> tempCommentTimeStamps;
        if (user.getNewTimeStampsComments() != null)
            tempCommentTimeStamps = new HashMap<>(user.getNewTimeStampsComments());
        else
            tempCommentTimeStamps = new HashMap<>();

        tempCommentTimeStamps.put(broadcast.getId(), broadcast.getLatestCommentTimestamp());
        user.setNewTimeStampsComments(tempCommentTimeStamps);
        userRepository.updateUser(user);
    }

    //BL
    public static void pushFCM(String state, String application_state, String tokenId, Notification notification, Broadcast broadcast, String message, String name, String subscriberName, String token_id, String circlename) {
        String apiurl = "https://circle-d8cc7.web.app/api/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiurl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);

        switch (state)
        {
            case "comment":
                String title  = "New Comment added in "+ notification.getCircleName();
                String body_comment = name+" " + ":" + " "+message ;
                Call<ResponseBody> call_comment = api.sendpushNotification(tokenId,title,body_comment);
                call_comment.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        try {
                            Log.d("Push",response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                break;
            case "broadcast":
                String title_broadcast  = "New Post added in "+ notification.getCircleName();
                String body =broadcast.getTitle();
                Call<ResponseBody> call = api.sendpushNotification(tokenId,title_broadcast,body);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        try {
                            Log.d("Push",response.body().string()+"::"+title_broadcast+body);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                break;
            case "new_applicant":
                String title_applicant  = "New Applicant in "+ circlename;
                String body_applicant =name;
                Call<ResponseBody> call_applicant = api.sendpushNotification(token_id,title_applicant,body_applicant);
                call_applicant.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        try {
                            Log.d("Push",response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                break;

            case "applicant":
                String titleapplicant  = "Your Request for "+ notification.getCircleName();
                String bodyapplicant = application_state;
                Call<ResponseBody> callapplicant = api.sendpushNotification(tokenId,titleapplicant,bodyapplicant);
                callapplicant.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                        try {
                            Log.d("Push",response.body().string()+"::"+titleapplicant+bodyapplicant);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                break;

        }
    }
    public static void sendUserApplicationToCreator(User user, Subscriber subscriber, Circle circle){
        CirclePersonnelRepository circlePersonnelRepository = new CirclePersonnelRepository();
        circlePersonnelRepository.applyOrJoin(circle, user, subscriber);
    }

    public static void writeReportAbuse(Context context,String circleID,String broadcastID,String commentID,String  creatorID,String  userID,String  reportType){
        FBRepository fbRepository = new FBRepository();
        fbRepository.createReportAbuse(context, circleID, broadcastID, commentID, creatorID, userID, reportType);
    }

    public static void navToNotificationSource(Context mContext, Notification notif, int position, String broadcastId){
        NotificationRepository notificationRepository = new NotificationRepository();
        notificationRepository.NotifyOnclickListener(mContext, notif, position, broadcastId);
    }

    public static void deleteCircle(Circle circle, User user){
        CircleRepository circleRepository = new CircleRepository();
        circleRepository.deleteCircle(circle, user);
    }

    public static void exitCircle(Circle circle, User user){
        CircleRepository circleRepository = new CircleRepository();
        circleRepository.exitCircle(circle, user);
    }

    public static void makeNewFeedback(Map<String, Object> map) {

        FBRepository fbRepository = new FBRepository();
        fbRepository.makeFeedbackEntry(map);
    }
}
