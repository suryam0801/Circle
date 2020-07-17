package circleapp.circlepackage.circle.Helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.data.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.Comment;
import circleapp.circlepackage.circle.data.ObjectModels.Notification;
import circleapp.circlepackage.circle.data.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HelperMethods {

    public static int returnIndexOfCircleList(List<Circle> circleList, Circle circle) {
        int counter = 0;
        int position = -1;
        for (Circle c : circleList) {
            if (c.getId().equals(circle.getId()))
                position = counter;

            counter++;
        }
        return position;
    }

    public static int returnIndexOfBroadcast(List<Broadcast> broadcastList, Broadcast broadcast) {
        int position = 0;
        for (Broadcast b : broadcastList) {
            if (broadcast.getId().equals(b.getId()))
                return position;

            position++;
        }
        return position;
    }

    public static int returnIndexOfSubscriber(List<Subscriber> subscriberList, Subscriber subscriber) {
        int position = 0;
        for (Subscriber s : subscriberList) {
            if (subscriber.getId().equals(s.getId()))
                return position;
            position++;
        }
        return position;
    }

    public static boolean isMemberOfCircle(Circle circle, String uID) {
        boolean isMember = false;
        if (circle.getMembersList() != null) {
            for (String memberId : circle.getMembersList().keySet()) {
                if (memberId.equals(uID))
                    isMember = true;
            }
        }
        return isMember;
    }

    public static boolean listContainsCircle(List<Circle> circleList, Circle circle) {
        boolean containsCircle = false;
        for (Circle c : circleList) {
            if (c.getId().equals(circle.getId()))
                containsCircle = true;
        }
        return containsCircle;
    }

    public static boolean listContainsBroadcast(List<Broadcast> broadcastList, Broadcast broadcast) {
        boolean containsBroadcast = false;
        for (Broadcast b : broadcastList) {
            if (b.getId().equals(broadcast.getId()))
                containsBroadcast = true;
        }
        return containsBroadcast;
    }

    public static int numberOfApplicants(Circle c, User user) {
        int numOfApplicants = 0;
        if (c.getApplicantsList() != null && user.getUserId().equals(c.getCreatorID())) {
            numOfApplicants = c.getApplicantsList().size();
        }
        return numOfApplicants;
    }

    public static boolean ifUserApplied(Circle c, String userId) {
        boolean isApplicant = false;
        if (c.getApplicantsList() != null && c.getApplicantsList().keySet().contains(userId))
            isApplicant = true;

        return isApplicant;
    }

    public static int newNotifications(Circle c, User user) {
        int newNotifs = 0;
        if (user.getNotificationsAlert() != null && user.getNotificationsAlert().containsKey(c.getId())) {
            int userRead = user.getNotificationsAlert().get(c.getId());

            if (c.getNoOfBroadcasts() > userRead)
                newNotifs = c.getNoOfBroadcasts() - userRead;

        }
        return newNotifs;
    }

    public static int returnNoOfCommentsPostTimestamp(List<Comment> commentList, long timestamp) {
        int counter = 0;
        for (Comment comment : commentList) {
            if (comment.getTimestamp() > timestamp)
                ++counter;
        }

        return counter;
    }

    public static int returnNumberOfReadCommentsForBroadcast(User user, Broadcast broadcast) {
        int commentNumberReturnVal = 0;
        if (user.getNoOfReadDiscussions() != null && user.getNoOfReadDiscussions().containsKey(broadcast.getId()))
            commentNumberReturnVal = user.getNoOfReadDiscussions().get(broadcast.getId());

        return commentNumberReturnVal;
    }

    public static String getCircleIdFromShareURL(String url) {
        String lines[] = url.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            Log.d("URL", lines[i]);
        }
        url = url.replace("https://worfo.app.link/8JMEs34W96/?", "");
        return url;
    }

    public static String convertIntoDateFormat(String dateDormat, long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return DateFormat.format("", cal).toString();
    }

    public static String getTimeElapsed(long currentTime, long createdTime) {
        String timeElapsedReturnString = "";

        long days = TimeUnit.MILLISECONDS.toDays(currentTime - createdTime);
        long hours = TimeUnit.MILLISECONDS.toHours(currentTime - createdTime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentTime - createdTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentTime - createdTime);

        if (seconds < 60) {
            timeElapsedReturnString = seconds + "s ago";
        } else if (minutes >= 1 && minutes < 60) {
            timeElapsedReturnString = minutes + "m ago";
        } else if (hours >= 1 && hours < 24) {
            timeElapsedReturnString = hours + "h ago";
        } else if (days >= 1 && days < 365) {
            if (days >= 7)
                timeElapsedReturnString = (days / 7) + "w ago";
            else
                timeElapsedReturnString = days + "d ago";
        }

        return timeElapsedReturnString;
    }

    public static GradientDrawable gradientRectangleDrawableSetter(int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
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

    public static void copyLinkToClipBoard(Circle circle, Activity activity) {
        String shareMessage = "\nCome join my circle: " + circle.getName() + "\n\n";
        shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + circle.getId();
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Clip Copied", shareMessage);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(activity.getApplicationContext(), "Share Link Copied", Toast.LENGTH_SHORT).show();
    }


    public static void showReportAbusePopup(Dialog reportAbuseDialog, Context context, String circleID, String broadcastID, String commentID, String creatorID, String userID) {
        reportAbuseDialog.setContentView(R.layout.report_abuse_popup);
        final Button reportButton = reportAbuseDialog.findViewById(R.id.report_abuse_confirm_button);
        final Button cancel = reportAbuseDialog.findViewById(R.id.report_abuse_cancel_button);
        CheckBox spam_check = (CheckBox) reportAbuseDialog.findViewById(R.id.report_spam);
        CheckBox violent_check = (CheckBox) reportAbuseDialog.findViewById(R.id.report_violent);
        CheckBox sex_check = (CheckBox) reportAbuseDialog.findViewById(R.id.report_sex);

        reportButton.setOnClickListener(view -> {
            String reportType = "";
            if (spam_check.isChecked())
                reportType = "spam";
            if (violent_check.isChecked())
                reportType = "violence";
            if (sex_check.isChecked())
                reportType = "sex";

            if (!reportType.equals("")) {
                reportAbuseDialog.dismiss();
                FirebaseWriteHelper.createReportAbuse(context, circleID, broadcastID, commentID, creatorID, userID, reportType);
                Toast.makeText(context, "Thanks for making Circle a better place!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Please choose the reason", Toast.LENGTH_SHORT).show();
            }
        });

        cancel.setOnClickListener(view -> {
            reportAbuseDialog.dismiss();
        });

        reportAbuseDialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.popup_height)); //width,height
        reportAbuseDialog.getWindow().setGravity(Gravity.CENTER);
        reportAbuseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        reportAbuseDialog.show();
    }

    public static void OrderNotification(Context context, TextView prevnotify, Notification notification, List<Notification> previousNotifs, List<Notification> thisWeekNotifs, NotificationAdapter adapterPrevious, NotificationAdapter adapterThisWeek, RecyclerView previousListView, RecyclerView thisWeekListView) {
        String currentTimeStamp = getCurrentTimeStamp();
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(thisWeekNotifs.size(), StaggeredGridLayoutManager.HORIZONTAL );
        thisWeekListView.setLayoutManager(gridLayoutManager);
        Scanner scan = new Scanner(currentTimeStamp);
        scan.useDelimiter("-");
        int currentDay = Integer.parseInt(scan.next());
        int currentMonth = Integer.parseInt(scan.next());
        String date = notification.getDate();
        scan = new Scanner(date);
        scan.useDelimiter("-");
        int notificationDay = Integer.parseInt(scan.next());
        int notificationMonth = Integer.parseInt(scan.next());

        if (Math.abs(notificationDay - currentDay) > 6 || Math.abs(notificationMonth - currentMonth) >= 1)
            previousNotifs.add(0, notification);
        else
            thisWeekNotifs.add(0, notification);

        if (previousNotifs.size() == 0) {
            prevnotify.setVisibility(View.INVISIBLE);
        } else {
            prevnotify.setVisibility(View.VISIBLE);
            StaggeredGridLayoutManager prevnotifygridLayoutManager = new StaggeredGridLayoutManager(previousNotifs.size(), StaggeredGridLayoutManager.HORIZONTAL );
            thisWeekListView.setLayoutManager(prevnotifygridLayoutManager);
        }

        adapterThisWeek = new NotificationAdapter(context, thisWeekNotifs);
        adapterPrevious = new NotificationAdapter(context, previousNotifs);

        previousListView.setAdapter(adapterPrevious);
        thisWeekListView.setAdapter(adapterThisWeek);

    }

    public static String uuidGet() {
        return UUID.randomUUID().toString();
    }

    public static void GlideSetProfilePic(Context context, String avatar, CircleImageView profilePic) {
        Glide.with(context)
                .load(Integer.parseInt(avatar))
                .placeholder(ContextCompat.getDrawable(context, Integer.parseInt(avatar)))
                .into(profilePic);
    }

    public static boolean circleFitsWithinFilterContraints(List<String> filters, Circle circle) {
        boolean circleFits = false;
        if (filters != null && filters.contains(circle.getCategory()))
            circleFits = true;

        return circleFits;
    }

/*
    public static void initializeBroadcastListener(Context context, Broadcast b, User user) {
        boolean listeningToBroadcast = user.getListeningBroadcasts() != null && user.getListeningBroadcasts().contains(b.getId());

        List<String> listenTemp = new ArrayList<>();

        if (user.getListeningBroadcasts() != null)
            listenTemp = new ArrayList<>(user.getListeningBroadcasts());

        if (!listeningToBroadcast) {
            listenTemp.add(b.getId());
            user.setListeningBroadcasts(listenTemp);
            FirebaseWriteHelper.updateUser(user, context);
        }
    }
*/

    public static void initializeNewCommentsAlertTimestamp(Context context, Broadcast b, User user) {
        HashMap<String, Long> commentTimeStampTemp;
        if (user.getNewTimeStampsComments() == null) {
            //first time viewing any comments
            commentTimeStampTemp = new HashMap<>();
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser((Activity) context, user);
            FirebaseWriteHelper.updateUserNewTimeStampComments(user.getUserId(), b.getId(), b.getLatestCommentTimestamp());
        } else if (user.getNewTimeStampsComments() != null && !user.getNewTimeStampsComments().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            commentTimeStampTemp = new HashMap<>(user.getNewTimeStampsComments());
            commentTimeStampTemp.put(b.getId(), (long) 0);
            user.setNewTimeStampsComments(commentTimeStampTemp);

            SessionStorage.saveUser((Activity) context, user);
            FirebaseWriteHelper.updateUserNewTimeStampComments(user.getUserId(), b.getId(), b.getLatestCommentTimestamp());
        }
    }


    public static void initializeNewReadComments(Context context, Broadcast b, User user) {
        HashMap<String, Integer> userNoReadComments;
        if (user.getNoOfReadDiscussions() == null) {
            //first time viewing any comments
            userNoReadComments = new HashMap<>();
            userNoReadComments.put(b.getId(), b.getNumberOfComments());
            user.setNoOfReadDiscussions(userNoReadComments);

            SessionStorage.saveUser((Activity) context, user);
            FirebaseWriteHelper.updateUserNewReadComments(user.getUserId(), b.getId(), b.getNumberOfComments());
        } else if (user.getNoOfReadDiscussions() != null && !user.getNoOfReadDiscussions().containsKey(b.getId())) {
            //if timestampcomments exists but does not contain value for that particular broadcast
            userNoReadComments = new HashMap<>(user.getNoOfReadDiscussions());
            userNoReadComments.put(b.getId(), 0);
            user.setNoOfReadDiscussions(userNoReadComments);

            SessionStorage.saveUser((Activity) context, user);
            FirebaseWriteHelper.updateUserNewReadComments(user.getUserId(), b.getId(), b.getNumberOfComments());
        }
    }

    public static void updateUserFields(Context mContext, Broadcast broadcast, String navFrom, User user) {
        HashMap<String, Integer> tempNoOfDiscussion;
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
                FirebaseWriteHelper.updateUser(user, mContext);
                break;

            case "view":
                tempNoOfDiscussion.put(broadcast.getId(), broadcast.getNumberOfComments());
                user.setNoOfReadDiscussions(tempNoOfDiscussion);
                FirebaseWriteHelper.updateUser(user, mContext);
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
        FirebaseWriteHelper.updateUser(user, mContext);
    }

    public static Uri getImageUri() {
        Uri m_imgUri = null;
        File m_file;
        try {
            SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String m_curentDateandTime = m_sdf.format(new Date());
            String m_imagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + m_curentDateandTime + ".jpg";
            m_file = new File(m_imagePath);
            m_imgUri = Uri.fromFile(m_file);
        } catch (Exception p_e) {
        }
        return m_imgUri;
    }

    public static void setProfilePicMethod(Context context, CircleImageView profilePic, String avatar, ImageView avatarBg, ImageButton avatarButton, ImageView[] avatarBgList, ImageButton[] avatarList) {
        GlideSetProfilePic(context, avatar, profilePic);
        avatarButton.setPressed(true);
        int visibility = avatarBg.getVisibility();
        if (visibility == View.VISIBLE) {
            GlideSetProfilePic(context, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);
            avatarBg.setVisibility(View.GONE);
            avatarButton.setPressed(false);
        } else {
            for (int i = 0; i < 8; i++) {
                if (avatarList[i] != avatarButton) {
                    avatarBgList[i].setVisibility(View.GONE);
                    avatarList[i].setPressed(false);
                } else
                    avatarBgList[i].setVisibility(View.VISIBLE);
            }
        }
    }

    public static LinearLayout generateLayoutPollOptionBackground(Context context, RadioButton button, int percentage) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
        linearLayoutParams.setMargins(0, 10, 0, 10);
        linearLayoutParams.weight = 100;
        layout.setLayoutParams(linearLayoutParams);
        layout.setBackground(new PercentDrawable(100, "#EFF6FF"));

        TextView tv = new TextView(context);
        tv.setText(percentage + "%");
        tv.setTextAppearance(context, R.style.poll_percentage_textview_style);
        tv.setPadding(0, 0, 30, 0);

        layout.addView(button);
        layout.addView(tv);

        return layout;
    }

    public static RadioButton generateRadioButton(Context context, String optionName, int percentage) {
        RadioButton button = new RadioButton(context);
        LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
        rbParams.weight = 90;
        rbParams.setMargins(0, 0, 0, 0);
        button.setPadding(10, 0, 0, 0);
        button.setLayoutParams(rbParams);
        button.setHighlightColor(Color.BLACK);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{Color.parseColor("#6CACFF")}, //disabled
                        new int[]{Color.parseColor("#6CACFF")} //enabled
                },
                new int[]{
                        Color.parseColor("#6CACFF") //disabled
                        , Color.parseColor("#6CACFF") //enabled
                }
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            button.setButtonTintList(colorStateList);


        button.setBackground(new PercentDrawable(percentage, "#D8E9FF"));
        button.setTextColor(Color.BLACK);
        button.setText(optionName);

        return button;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void compressImage(ContentResolver resolver, Uri filePath) {
        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(resolver, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //here you can choose quality factor in third parameter(ex. i choosen 20)
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] fileInBytes = baos.toByteArray();
    }

    public static void increaseTouchArea(View view) {
        final View parent = (View) view.getParent();  // button: the view you want to enlarge hit area
        parent.post(new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                view.getHitRect(rect);
                rect.top -= 80;    // increase top hit area
                rect.left -= 80;   // increase left hit area
                rect.bottom += 80; // increase bottom hit area
                rect.right += 80;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, view));
            }
        });
    }

    public static void increaseTouchArea(View view, int top, int left, int bottom, int right) {
        final View parent = (View) view.getParent();  // button: the view you want to enlarge hit area
        parent.post(new Runnable() {
            public void run() {
                final Rect rect = new Rect();
                view.getHitRect(rect);
                rect.top -= top;    // increase top hit area
                rect.left -= left;   // increase left hit area
                rect.bottom += bottom; // increase bottom hit area
                rect.right += right;  // increase right hit area
                parent.setTouchDelegate(new TouchDelegate(rect, view));
            }
        });
    }

    public static void setUserProfileImage(User user, Context context, CircleImageView profileImageView) {
        if (user.getProfileImageLink().length() > 10) {
            Glide.with(context)
                    .load(user.getProfileImageLink())
                    .into(profileImageView);
        } else if (user.getProfileImageLink().equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(profileImageView);
        } else {
            int propic = Integer.parseInt(user.getProfileImageLink());
            Glide.with(context)
                    .load(propic)
                    .into(profileImageView);
        }

    }

    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(40);
        }
    }

    public static String getCurrentTimeStamp() {
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewPager.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
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

    }

}
}