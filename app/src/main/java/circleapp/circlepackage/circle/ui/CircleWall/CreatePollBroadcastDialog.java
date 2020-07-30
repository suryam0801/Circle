package circleapp.circlepackage.circle.ui.CircleWall;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import circleapp.circlepackage.circle.DataLayer.BroadcastsRepository;
import circleapp.circlepackage.circle.Helpers.SendNotification;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.User;

import static android.Manifest.permission.CAMERA;

public class CreatePollBroadcastDialog {
    public boolean pollExists = false;
    public boolean imageExists = false;

    private static final int PICK_IMAGE_ID = 234;
    private Dialog createPollBroadcastPopup;
    private EditText setPollQuestionET, setPollOptionET;
    private Button btnAddPollOption,btnUploadPollBroadcast, cancelPollButton;
    private TextView pollAddPhotoText;
    private List<String> pollAnswerOptionsList = new ArrayList<>();
    private CircleWall circleWall;
    public RelativeLayout pollUploadButtonView;
    private Circle circle;
    private User user;
    private Activity activity;
    private Uri downloadLink;


    private GlobalVariables globalVariables = new GlobalVariables();
    private LinearLayout pollOptionsDisplay, pollImageUploadInitiation;
    public ImageView pollAddPhoto;
    public void showCreatePollBroadcastDialog(Activity activity) {
        this.activity = activity;
        InitUI();
        cancelPollButton.setOnClickListener(view -> createPollBroadcastPopup.dismiss());

        pollImageUploadInitiation.setOnClickListener(view -> {
            pollImageUploadInitiation.setVisibility(View.GONE);
            pollUploadButtonView.setVisibility(View.VISIBLE);
        });

        pollUploadButtonView.setOnClickListener(v -> {
            permissionCheck();
        });

        btnAddPollOption.setOnClickListener(view -> {
            addPollOption();

        });

        btnUploadPollBroadcast.setOnClickListener(view -> {
            uploadPoll();

        });
        createPollBroadcastPopup.show();
    }

    private void uploadPoll() {
        if (pollAnswerOptionsList.isEmpty() || setPollQuestionET.getText().toString().isEmpty())
            Toast.makeText(activity, "Fill out all fields", Toast.LENGTH_SHORT).show();
        else {
            downloadLink = globalVariables.getTempdownloadLink();
            if (downloadLink != null)
                imageExists = true;
            createPollBroadcast();
        }
    }

    private void addPollOption() {
        String option = setPollOptionET.getText().toString();
        if ((option.contains(".") || option.contains("$") || option.contains("#") || option.contains("[") || option.contains("]") || option.isEmpty())) {
            //checking for invalid characters
            Toast.makeText(activity, "Option cannot use special characters or be empty", Toast.LENGTH_SHORT).show();
        } else {
            if (!option.isEmpty() && !setPollQuestionET.getText().toString().isEmpty()) {

                final TextView tv = generatePollOptionTV(option);

                tv.setOnClickListener(view1 -> {
                    pollOptionsDisplay.removeView(tv);
                    pollAnswerOptionsList.remove(tv.getText());
                });

                pollAnswerOptionsList.add(option);
                pollOptionsDisplay.addView(tv);
                setPollOptionET.setText("");
            } else {
                Toast.makeText(activity, "Fill out all fields", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void permissionCheck() {

        Permissions.check(activity/*context*/, CAMERA, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                ImagePicker imagePicker= new ImagePicker(activity.getApplication());
                Intent chooseImageIntent = imagePicker.getPickImageIntent();
                activity.startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
            }
        });
    }

    private void InitUI() {
        createPollBroadcastPopup = new Dialog(activity);
        createPollBroadcastPopup.setContentView(R.layout.poll_broadcast_create_popup); //set dialog view
        createPollBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createPollBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        circleWall = new CircleWall();
        user = globalVariables.getCurrentUser();
        circle = globalVariables.getCurrentCircle();
        setPollQuestionET = createPollBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollQuestionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setPollOptionET = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        setPollOptionET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        pollOptionsDisplay = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_display);
        btnAddPollOption = createPollBroadcastPopup.findViewById(R.id.poll_create_answer_option_add_btn);
        pollAddPhoto = createPollBroadcastPopup.findViewById(R.id.poll_display_photo_add_broadcast);
        pollUploadButtonView = createPollBroadcastPopup.findViewById(R.id.poll_add_photo_view);
        pollAddPhotoText = createPollBroadcastPopup.findViewById(R.id.poll_upload_photo);
        pollImageUploadInitiation = createPollBroadcastPopup.findViewById(R.id.poll_image_upload_initiate_layout);
        pollExists = true;

        btnUploadPollBroadcast = createPollBroadcastPopup.findViewById(R.id.upload_poll_broadcast_btn);
        cancelPollButton = createPollBroadcastPopup.findViewById(R.id.create_poll_broadcast_cancel_btn);
    }

    public TextView generatePollOptionTV(String option) {
        LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
        lparams.setMargins(0, 10, 20, 0);

        final TextView tv = new TextView(activity);
        tv.setLayoutParams(lparams);
        tv.setText(option);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setBackground(activity.getResources().getDrawable(R.drawable.poll_creation_item_option_background));
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_white_24dp, 0);
        tv.setPaddingRelative(40, 10, 40, 10);

        return tv;
    }
    private void createPollBroadcast() {
        BroadcastsRepository broadcastsRepository = new BroadcastsRepository();
        String currentCircleId = circle.getId();
        String broadcastId = broadcastsRepository.getBroadcastId(currentCircleId);
        String pollQuestion = setPollQuestionET.getText().toString();
        Broadcast pollBroadcast = new Broadcast();
        String currentUserName = user.getName();
        String currentUserId = user.getUserId();

        //creating poll options hashmap
        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (pollExists) {

            Poll poll = new Poll(pollQuestion, options, null);
            if (imageExists) {
                pollBroadcast = new Broadcast(broadcastId, null, null, downloadLink.toString(), currentUserName, circle.getMembersList(), currentUserId, true, true,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
            } else
                pollBroadcast = new Broadcast(broadcastId, null, null, null, currentUserName, circle.getMembersList(), currentUserId, true, false,
                        System.currentTimeMillis(), poll, user.getProfileImageLink(), 0, 0,true);
        }
        //updating number of broadcasts in circle
        circle = globalVariables.getCurrentCircle();
        int newCount = circle.getNoOfBroadcasts() + 1;
        circle.setNoOfBroadcasts(newCount);
        globalVariables.saveCurrentCircle(circle);
        SendNotification.sendBCinfo(activity, pollBroadcast, user.getUserId(), broadcastId, circle.getName(), currentCircleId, currentUserName, circle.getMembersList(), circle.getBackgroundImageLink(), pollQuestion);
        circleWall.updateUserCount(circle);

        //updating broadcast in broadcast db
        broadcastsRepository.writeBroadcast(circle.getId(), pollBroadcast, newCount);
        pollExists = false;
        imageExists = false;
        pollAnswerOptionsList.clear();
        createPollBroadcastPopup.dismiss();
    }
}
