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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Explore.NotificationAdapter;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseRetrievalViewModel;
import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.Login.OtpActivity;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Comment;
import circleapp.circlepackage.circle.ObjectModels.Notification;
import circleapp.circlepackage.circle.ObjectModels.Subscriber;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;

import de.hdodenhof.circleimageview.CircleImageView;

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

    public static void OrderNotification(Context context, TextView prevnotify, Notification notification, List<Notification> previousNotifs, List<Notification> thisWeekNotifs, NotificationAdapter adapterPrevious, NotificationAdapter adapterThisWeek, ListView previousListView, ListView thisWeekListView) {
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

    public static void NotifyOnclickListener(Context context, Activity activity, Notification curent, int position, String broadcastId) {
        FirebaseRetrievalViewModel viewModel = ViewModelProviders.of((FragmentActivity) activity).get(FirebaseRetrievalViewModel.class);
        LiveData<DataSnapshot> liveData = viewModel.getDataSnapsParticularCircleLiveData(curent.getCircleId());
        liveData.observe((LifecycleOwner) activity, dataSnapshot -> {
            Circle circle = dataSnapshot.getValue(Circle.class);
            if (circle != null) {
                Log.d("Notification Fragment", "Circle list :: " + circle.toString());
                if (circle.getMembersList().containsKey(SessionStorage.getUser((Activity) activity).getUserId())) {
                    SessionStorage.saveCircle((Activity) activity, circle);
                    Intent intent = new Intent(activity, CircleWall.class);
                    intent.putExtra("broadcastPos", position);
                    intent.putExtra("broadcastId", broadcastId);
                    activity.startActivity(intent);
                    ((Activity) activity).finish();
                } else {
                    Toast.makeText(activity, "Not a member of this circle anymore", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "The Circle has been deleted by Creator", Toast.LENGTH_SHORT).show();
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
    public static void storeUserFile(String data, OtpActivity otpActivity) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(otpActivity.openFileOutput("user.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            sendUserToHome(otpActivity);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public static void sendUserToHome(OtpActivity otpActivity) {
        Intent homeIntent = new Intent(otpActivity, ExploreTabbedActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        otpActivity.startActivity(homeIntent);
        otpActivity.finish();
    }

    //Function to send the user to Registration Page
    public static void senduserToReg(OtpActivity otpActivity, String phn_number, String ward, String district) {
        Intent homeIntent = new Intent(otpActivity, GatherUserDetails.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        homeIntent.putExtra("phn", phn_number);
        homeIntent.putExtra("ward",ward);
        homeIntent.putExtra("district",district);
        //homeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        otpActivity.startActivity(homeIntent);
        Log.d("OtpActivity",ward+"::"+district);
        otpActivity.finish();
    }

}