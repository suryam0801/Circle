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
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.ReportAbuse;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

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

    public static void addDistrict(String district) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference locationsDB;
        locationsDB = database.getReference("Locations");
        locationsDB.child(district).setValue(true);
    }
    public static void broadcastListenerList(int transaction, String userId,String circleId, String broadcastId){
        //transaction=0 for adding, 1 for removing
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB;
        broadcastsDB = database.getReference("Broadcasts");
        if(transaction==0){
            broadcastsDB.child(circleId).child(broadcastId).child("listenersList").child(userId).setValue(true);
        }
        else {
            broadcastsDB.child(circleId).child(broadcastId).child("listenersList").child(userId).removeValue();
        }
    }

    public static String createPhotoBroadcast(String title, String photoUri, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB;
        broadcastsDB = database.getReference("Broadcasts");
        String id = uuidGet();
        Broadcast broadcast = new Broadcast(id, title, null, photoUri, creatorName,null, "AdminId", false, true, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments);
        broadcastsDB.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createPollBroadcast(String text, String creatorName, int offsetTimeStamp, HashMap<String, Integer> pollOptions, String downloadUri, int noOfComments, String circleId) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB;
        broadcastsDB = database.getReference("Broadcasts");
        String id = uuidGet();
        Broadcast broadcast;
        Poll poll = new Poll(text, pollOptions, null);
        if (downloadUri != null)
            broadcast = new Broadcast(id, null, null, downloadUri, creatorName,null, "AdminId", true, true, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments);
        else
            broadcast = new Broadcast(id, null, null, null, creatorName,null, "AdminId", true, false, System.currentTimeMillis() + offsetTimeStamp, poll, "default", 0, noOfComments);
        broadcastsDB.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createMessageBroadcast(String title, String message, String creatorName, int offsetTimeStamp, int noOfComments, String circleId) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB;
        broadcastsDB = database.getReference("Broadcasts");
        String id = uuidGet();
        Broadcast broadcast = new Broadcast(id, title, message, null, creatorName,null, "AdminId", false, false, System.currentTimeMillis() + offsetTimeStamp, null, "default", 0, noOfComments);
        broadcastsDB.child(circleId).child(id).setValue(broadcast);
        return id;
    }

    public static String createCircle(String name, String description, String acceptanceType, String creatorName, String district, int noOfBroadcasts, int noOfDiscussions, String category) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference circlesDB;
        circlesDB = database.getReference("Circles");
        HashMap<String, Boolean> circleIntTags = new HashMap<>();
        circleIntTags.put("sample", true);
        String id = uuidGet();
        Circle circle = new Circle(id, name, description, acceptanceType, "Everybody", "CreatorAdmin", creatorName, category, "default", null, null, district, null, System.currentTimeMillis(), noOfBroadcasts, noOfDiscussions);
        circlesDB.child(id).setValue(circle);
        return id;
    }

    public static void createComment(String name, String text, int offsetTimeStamp, String circleId, String broadcastId) {
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference commentsDB;
        commentsDB = database.getReference("BroadcastComments");
        String id = uuidGet();
        Comment comment = new Comment(name, text, id, null, System.currentTimeMillis() + offsetTimeStamp);
        commentsDB.child(circleId).child(broadcastId).child(id).setValue(comment);
    }

    public static void createReportAbuse(Context context, String circleID, String broadcastID, String commentID, String creatorID, String userID, String reportType) {
        FirebaseDatabase database;
        FirebaseAuth currentuser;
        currentuser = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        DatabaseReference reportAbuseDB;
        reportAbuseDB = database.getReference("ReportAbuse");
        String id = uuidGet();
        ReportAbuse reportAbuse = new ReportAbuse(id, circleID, broadcastID, commentID, creatorID, userID, reportType);
        if (currentuser.getCurrentUser().getUid() == creatorID) {
            Toast.makeText(context, "Stop Reporting your own Content", Toast.LENGTH_SHORT).show();
        } else {

            reportAbuseDB.child(id).setValue(reportAbuse);
        }
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
                createReportAbuse(context, circleID, broadcastID, commentID, creatorID, userID, reportType);
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

    public static void OrderNotification(Context context, TextView prevnotify, Notification notification, List<Notification> previousNotifs, List<Notification> thisWeekNotifs, NotificationAdapter adapterPrevious, NotificationAdapter adapterThisWeek, ListView previousListView, ListView thisWeekListView)
    {
        String currentTimeStamp = getCurrentTimeStamp();

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
        }

        adapterThisWeek = new NotificationAdapter(context, thisWeekNotifs);
        adapterPrevious = new NotificationAdapter(context, previousNotifs);

        previousListView.setAdapter(adapterPrevious);
        thisWeekListView.setAdapter(adapterThisWeek);

    }

    public static void deleteBroadcast(String circleId, String broadcastId, int noOfBroadcasts){
        FirebaseDatabase database;
        database = FirebaseDatabase.getInstance();
        DatabaseReference broadcastsDB, commentsDB, circleDB;
        commentsDB = database.getReference("BroadcastComments");
        broadcastsDB = database.getReference("Broadcasts");
        circleDB = database.getReference("Circles");
        broadcastsDB.child(circleId).child(broadcastId).removeValue();
        commentsDB.child(circleId).child(broadcastId).removeValue();
        circleDB.child(circleId).child("noOfBroadcasts").setValue(noOfBroadcasts-1);
    }
    public static void NotifyOnclickListener(Context context, Notification curent, int position, String broadcastId)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference circlesDB;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        circlesDB = database.getReference("Circles");
        String circleid = curent.getCircleId();
        circlesDB.child(circleid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Circle circle = dataSnapshot.getValue(Circle.class);
                if (circle != null) {

                    Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                    if (circle.getMembersList().containsKey(firebaseAuth.getCurrentUser().getUid())) {
                        SessionStorage.saveCircle((Activity) context, circle);
                        Intent intent = new Intent(context, CircleWall.class);
                        intent.putExtra("broadcastPos", position);
                        intent.putExtra("broadcastId", broadcastId);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    } else {
                        Toast.makeText(context, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "The Circle has been deleted by Creator", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewPager.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

}