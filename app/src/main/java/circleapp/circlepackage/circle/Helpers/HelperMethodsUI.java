package circleapp.circlepackage.circle.Helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.ViewPager;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Notification;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ui.ExploreTabbedActivity;
import circleapp.circlepackage.circle.ui.Notifications.NotificationAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

public class HelperMethodsUI {
    private static GlobalVariables globalVariables = new GlobalVariables();
//UI
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
//UI
    public static int returnIndexOfBroadcast(List<Broadcast> broadcastList, Broadcast broadcast) {
        int position = 0;
        for (Broadcast b : broadcastList) {
            if (broadcast.getId().equals(b.getId()))
                return position;

            position++;
        }
        return position;
    }
//UI
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
//UI
    public static boolean listContainsBroadcast(List<Broadcast> broadcastList, Broadcast broadcast) {
        boolean containsBroadcast = false;
        for (Broadcast b : broadcastList) {
            if (b.getId().equals(broadcast.getId()))
                containsBroadcast = true;
        }
        return containsBroadcast;
    }
//UI
    public static int numberOfApplicants(Circle c, User user) {
        int numOfApplicants = 0;
        if (c.getApplicantsList() != null && user.getUserId().equals(c.getCreatorID())) {
            numOfApplicants = c.getApplicantsList().size();
        }
        return numOfApplicants;
    }
//UI
    public static boolean ifUserApplied(Circle c, String userId) {
        boolean isApplicant = false;
        if (c.getApplicantsList() != null && c.getApplicantsList().keySet().contains(userId))
            isApplicant = true;

        return isApplicant;
    }
//UI
    public static int newNotifications(Circle c, User user) {
        int newNotifs = 0;
        if (user.getNotificationsAlert() != null && user.getNotificationsAlert().containsKey(c.getId())) {
            int userRead = user.getNotificationsAlert().get(c.getId());

            if (c.getNoOfBroadcasts() > userRead)
                newNotifs = c.getNoOfBroadcasts() - userRead;

        }
        return newNotifs;
    }

//UI
    public static String convertIntoDateFormat(String dateDormat, long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return DateFormat.format("", cal).toString();
    }
//UI
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
//UI
    public static GradientDrawable gradientRectangleDrawableSetter(int radius) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(radius);
        return gradientDrawable;
    }
//UI Context
    public static void showShareCirclePopup(Circle c, Activity activity) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Redefining Communication and Connection");
            String shareMessage = "\nCome join my circle: " + c.getName() + "\n\n";
            shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + c.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            activity.startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }
//UI
    public static void copyLinkToClipBoard(Circle circle, Activity activity) {
        String shareMessage = "\nCome join my circle: " + circle.getName() + "\n\n";
        shareMessage = shareMessage + "https://worfo.app.link/8JMEs34W96/" + "?" + circle.getId();
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Clip Copied", shareMessage);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(activity.getApplicationContext(), "Share Link Copied", Toast.LENGTH_SHORT).show();
    }
//UI
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
                HelperMethodsBL.writeReportAbuse(context, circleID, broadcastID, commentID, creatorID, userID, reportType);
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
//UI
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
//UI
    public static String uuidGet() {
        return UUID.randomUUID().toString();
    }
//UI
    public static void GlideSetProfilePic(Context context, String avatar, CircleImageView profilePic) {
        int drawableValue;
        if(avatar.equals(String.valueOf(R.drawable.ic_account_circle_black_24dp)))
            drawableValue = Integer.parseInt(avatar);
        else {
            int index = Integer.parseInt(String.valueOf(avatar.charAt(avatar.length()-1)));
            index = index-1;
            TypedArray avatarResourcePos = context.getResources().obtainTypedArray(R.array.AvatarValues);
            drawableValue = avatarResourcePos.getResourceId(index, 0);
        }
        Glide.with(context)
                .load(drawableValue)
                .placeholder(R.drawable.ic_account_circle_black_24dp)
                .into(profilePic);
    }
//UI
    public static boolean circleFitsWithinFilterContraints(List<String> filters, Circle circle) {
        boolean circleFits = false;
        if (filters != null && filters.contains(circle.getCategory()))
            circleFits = true;

        return circleFits;
    }

//UI
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
//UI
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
//UI
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
//UI
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
//UI
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
//UI
    public static void setUserProfileImage(String url, Context context, CircleImageView profileImageView) {
        if (url.length() > 10) {
            Glide.with(context)
                    .load(globalVariables.getAuthenticationToken().getCurrentUser().getPhotoUrl())
                    .into(profileImageView);
        } else if (url.equals("default")) {
            int profilePic = Integer.parseInt(String.valueOf(R.drawable.default_profile_pic));
            Glide.with(context)
                    .load(ContextCompat.getDrawable(context, profilePic))
                    .into(profileImageView);
        } else {
            int index = Integer.parseInt(String.valueOf(url.charAt(url.length()-1)));
            index = index-1;
            Log.d("index", index+"");
            TypedArray avatarResourcePos = context.getResources().obtainTypedArray(R.array.AvatarValues);
            int profilePic = avatarResourcePos.getResourceId(index, 0);
            Glide.with(context)
                    .load(profilePic)
                    .into(profileImageView);
        }

    }
//UI
    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(40);
        }
    }
//UI
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
//UI
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
//UI
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

//UI
    public static void createDefaultCircleIcon(Circle circle, Context context, CircleImageView backgroundPic){
        char firstLetter = circle.getName().charAt(0);
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color = generator.getColor(circle.getName());
        TextDrawable drawable = TextDrawable.builder()
                .buildRound(firstLetter +"",color);

        if (!circle.getBackgroundImageLink().equals("default"))
            Glide.with(context).load(circle.getBackgroundImageLink()).into(backgroundPic);
        else {
            backgroundPic.setBackground(drawable);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showExitDialog(Context context, Circle circle, User user) {
        Dialog confirmationDialog = new Dialog(context);
        confirmationDialog.setContentView(R.layout.exit_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.remove_user_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.remove_user_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            HelperMethodsBL.exitCircle((Activity) context, circle, user);
            confirmationDialog.dismiss();
            context.startActivity(new Intent(context, ExploreTabbedActivity.class));
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void showDeleteDialog(Context context, Circle circle, User user) {
        Dialog confirmationDialog = new Dialog(context);
        confirmationDialog.setContentView(R.layout.delete_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.delete_circle_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.delete_circle_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            HelperMethodsBL.deleteCircle((Activity) context, circle, user);
            context.startActivity(new Intent(context, ExploreTabbedActivity.class));
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> confirmationDialog.dismiss());

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }
}