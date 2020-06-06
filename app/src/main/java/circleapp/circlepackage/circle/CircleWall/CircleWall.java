package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.Explore.ExploreTabbedActivity;
import circleapp.circlepackage.circle.Notification.SendNotification;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.PersonelDisplay.PersonelDisplay;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class CircleWall extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference broadcastsDB, circlesPersonelDB, circlesDB;
    private FirebaseAuth currentUser;

    private String TAG = CircleWall.class.getSimpleName();
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 100;
    private StorageReference storageReference;
    private Uri filePath = null;
    private String downloadUri = null;
    private LinearLayout emptyDisplay;

    private Circle circle;
    private FloatingActionButton createNewBroadcast;

    private List<String> pollAnswerOptionsList = new ArrayList<>();
    private boolean pollExists = false;

    private ImageButton menuButton, back, shareCircle;
    private User user;

    //create broadcast popup ui elements
    private EditText setMessageET, setPollQuestionET, setPollOptionET;
    private LinearLayout uploadFileView, pollCreateView, additionalSelector, pollOptionsDisplay;
    private ImageView uploadCloudImageView;
    private TextView tvUploadFileOption, tvCreatePollOption, tvMiddleOrPlaceHolder, tvUploadPlaceholderText, circleBannerName;
    private Button btnAddPollOption, btnUploadBroadcast;
    private Dialog createBroadcastPopup;
    private FirebaseAnalytics firebaseAnalytics;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost;

    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);

        database = FirebaseDatabase.getInstance();
        broadcastsDB = database.getReference("Broadcasts");
        circlesPersonelDB = database.getReference("CirclePersonel");
        circlesDB = database.getReference("Circles");
        broadcastsDB.keepSynced(true);
        currentUser=FirebaseAuth.getInstance();
        user = SessionStorage.getUser(CircleWall.this);

        circle = SessionStorage.getCircle(CircleWall.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        circleBannerName = findViewById(R.id.circleBannerName);
        menuButton = findViewById(R.id.share_with_friend_button);
        back = findViewById(R.id.bck_Circlewall);
        shareCircle = findViewById(R.id.shareCircle);
        emptyDisplay = findViewById(R.id.circle_wall_empty_display);
        emptyDisplay.setVisibility(View.VISIBLE);

        circleBannerName.setText(circle.getName());

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        firebaseAnalytics.setCurrentScreen(CircleWall.this, "Inside circle wall scrolling", null);

        menuButton.setOnClickListener(view -> {
            showPopup(view);
        });

        shareCircle.setOnClickListener(view -> {
            showShareCirclePopup();
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            finish();
        });

        loadCircleBroadcasts();

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.circle_wall_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.viewMembers:
                        startActivity(new Intent(CircleWall.this, PersonelDisplay.class));
                        return true;
                    case R.id.exitCircle:
                        exitCircle();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private void exitCircle() {
        circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
        circlesPersonelDB.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
    }

    private void showShareCirclePopup() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Circle: Your friendly neighborhood app");
            String shareMessage= "\nLet me recommend you this application\n\n";
            //https://play.google.com/store/apps/details?id=
            Log.d(TAG, circle.getId());
            shareMessage = "www.circleneighborhoodapp.com/" + circle.getId();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception error) {

        }
    }

    private void loadCircleBroadcasts() {

        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.broadcastViewRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle);
        recyclerView.setAdapter(adapter);

        broadcastsDB.child(circle.getId()).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                broadcastList.add(0,broadcast); //to store timestamp values descendingly
                adapter.notifyDataSetChanged();
                emptyDisplay.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = 0;
                List<Broadcast> tempBroadcastList = new ArrayList<>(broadcastList); //avoids concurrent modification error
                for(Broadcast b : tempBroadcastList){
                    if(b.getId().equals(broadcast.getId())){
                        broadcastList.remove(position);
                        broadcastList.add(position,broadcast);
                        adapter.notifyDataSetChanged();
                    }
                    ++position;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = 0;
                for(Broadcast b : broadcastList){
                    if(b.getId().equals(broadcast.getId())) {
                        broadcastList.remove(position);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showCreateBroadcastDialog() {
        createBroadcastPopup = new Dialog(CircleWall.this);
        createBroadcastPopup.setContentView(R.layout.broadcast_create_popup_layout); //set dialog view

        setMessageET = createBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        setPollQuestionET = createBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollOptionET = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        pollOptionsDisplay = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_display);
        uploadFileView = createBroadcastPopup.findViewById(R.id.attachmentUploadView);
        pollCreateView = createBroadcastPopup.findViewById(R.id.poll_create_layout);
        additionalSelector = createBroadcastPopup.findViewById(R.id.additional_selector_view);
        uploadCloudImageView = createBroadcastPopup.findViewById(R.id.create_broadcast_file_upload_cloud_image_btn);
        tvUploadFileOption = createBroadcastPopup.findViewById(R.id.upload_file_btn);
        tvCreatePollOption = createBroadcastPopup.findViewById(R.id.poll_create_btn);
        tvMiddleOrPlaceHolder = createBroadcastPopup.findViewById(R.id.upload_or_poll_or_textview);
        tvUploadPlaceholderText = createBroadcastPopup.findViewById(R.id.create_broadcast_file_upload_text);
        btnAddPollOption = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_add_btn);
        btnUploadBroadcast = createBroadcastPopup.findViewById(R.id.upload_broadcast_btn);

        tvUploadFileOption.setOnClickListener(view -> {
            tvUploadFileOption.setVisibility(View.GONE);
            tvMiddleOrPlaceHolder.setVisibility(View.GONE);
            uploadFileView.setVisibility(View.VISIBLE);
            if (tvCreatePollOption.getVisibility() == View.GONE)
                additionalSelector.setVisibility(View.GONE);
        });

        tvCreatePollOption.setOnClickListener(view -> {
            tvCreatePollOption.setVisibility(View.GONE);
            tvMiddleOrPlaceHolder.setVisibility(View.GONE);
            pollCreateView.setVisibility(View.VISIBLE);
            if (tvUploadFileOption.getVisibility() == View.GONE)
                additionalSelector.setVisibility(View.GONE);
        });

        uploadFileView.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(CircleWall.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CircleWall.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                selectFile();
            }
        });

        btnAddPollOption.setOnClickListener(view -> {
            String option = setPollOptionET.getText().toString();
            if (!option.isEmpty() && !setPollQuestionET.getText().toString().isEmpty()) {
                LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
                lparams.setMargins(0, 10, 20, 0);
                final TextView tv = new TextView(CircleWall.this);
                tv.setLayoutParams(lparams);
                tv.setText(option);
                tv.setTextColor(Color.WHITE);
                tv.setGravity(Gravity.CENTER_VERTICAL);
                tv.setBackground(getResources().getDrawable(R.drawable.poll_creation_item_option_background));
                tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear_white_24dp, 0);
                tv.setPaddingRelative(40, 10, 40, 10);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pollOptionsDisplay.removeView(tv);
                        pollAnswerOptionsList.remove(tv.getText());
                        Log.d("CIRCLE WALL, ", pollAnswerOptionsList.toString());
                    }
                });
                pollAnswerOptionsList.add(option);
                Log.d("CIRCLE WALL, ", pollAnswerOptionsList.toString());
                pollOptionsDisplay.addView(tv);
                setPollOptionET.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "Fill out all poll fields", Toast.LENGTH_SHORT).show();
            }
        });

        btnUploadBroadcast.setOnClickListener(view -> {
            if (!pollAnswerOptionsList.isEmpty())
                pollExists = true;

            if (!setMessageET.getText().toString().isEmpty())
                createBroadcast();

            else
                Toast.makeText(getApplicationContext(), "Set your broadcast message", Toast.LENGTH_SHORT).show();
        });

        createBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createBroadcastPopup.show();
    }

    private void createBroadcast() {
        createBroadcastPopup.dismiss();
        String currentCircleId = circle.getId();

        String message = setMessageET.getText().toString();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String pollQuestion = setPollQuestionET.getText().toString();

        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();

        SendNotification.sendBCinfo(broadcastId,circle.getName(),circle.getId(),currentUserName,circle.getMembersList());

        HashMap<String, Integer> options = new HashMap<>();
        if(!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (downloadUri == null && pollExists == false) {

            Broadcast broadcast = new Broadcast(broadcastId, message, null,
                    currentUserName, currentUserId, false, System.currentTimeMillis(), null, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);

        } else if (downloadUri != null && pollExists == false) {

            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri,
                    currentUserName, currentUserId,  false, System.currentTimeMillis(), null, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);

        } else if (downloadUri == null && pollExists == true) {

            Poll poll = new Poll(pollQuestion, options, null);
            Broadcast broadcast = new Broadcast(broadcastId, message, null,
                    currentUserName, currentUserId,  true, System.currentTimeMillis(), poll, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);

        } else if (downloadUri != null && pollExists == true) {

            Poll poll = new Poll(pollQuestion, options, null);
            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri,
                    currentUserName, currentUserId,  true, System.currentTimeMillis(), poll, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);
        }
    }

    //start the intent for picking an image
    public void selectFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    //request permission from the user to access their internal storage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CircleWall.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                selectFile();
            } else {
                Toast.makeText(CircleWall.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    //when file is picked, upload file to firebase storage and retrieve the download URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            String uniqueID = UUID.randomUUID().toString();
            final StorageReference riversRef = storageReference.child("ProjectWall/" + circle.getId() + "/" + uniqueID);

            final ProgressDialog progressDialog = new ProgressDialog(CircleWall.this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(filePath));

            riversRef.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            })
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return riversRef.getDownloadUrl();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    downloadUri = uri.toString();
                    Log.d(TAG, downloadUri);
                    progressDialog.dismiss();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying error message
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

            if (extension != null) {
                uploadCloudImageView.setBackgroundResource(0);
                switch (extension) {
                    case "pdf":
                        uploadCloudImageView.setBackground(getResources().getDrawable(R.drawable.pdf_image));
                        break;
                    case "ppt":
                        uploadCloudImageView.setBackground(getResources().getDrawable(R.drawable.ppt_image));
                        break;
                    case "doc":
                        uploadCloudImageView.setBackground(getResources().getDrawable(R.drawable.doc_image));
                        break;
                    case "jpg":
                    case "jpeg":
                    case "png":
                    case "webp":
                        uploadCloudImageView.setImageURI(filePath);
                }
            }

            tvUploadPlaceholderText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CircleWall.this,ExploreTabbedActivity.class);
        startActivity(intent);
    }
}