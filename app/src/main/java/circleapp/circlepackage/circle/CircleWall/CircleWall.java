package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    private DatabaseReference broadcastsDB, circlesPersonelDB, circlesDB, usersDB;
    private FirebaseAuth currentUser;

    private String TAG = CircleWall.class.getSimpleName();
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 100;
    private StorageReference storageReference;
    private Uri filePath = null;
    private String downloadUri = null;
    private LinearLayout emptyDisplay;

    private Circle circle;

    private List<String> pollAnswerOptionsList = new ArrayList<>();
    private boolean pollExists = false;

    private ImageButton exitOrDeleteButton, back, viewPersonelButton;
    private User user;

    //create broadcast popup ui elements
    private EditText setMessageET, setPollQuestionET, setPollOptionET;
    private LinearLayout uploadFileView, pollCreateView, additionalSelector, pollOptionsDisplay;
    private ImageView uploadCloudImageView;
    private TextView circleBannerName, descPlaceHolder;
    private Button btnAddPollOption, btnUploadBroadcast;
    private Dialog createBroadcastPopup, confirmationDialog;
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton poll, newPost;
    private Button clearBroadcastPopup;
    String usersState;
    //elements for loading broadcasts, setting recycler view, and passing objects into adapter
    List<Broadcast> broadcastList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);
        confirmationDialog = new Dialog(CircleWall.this);
        user = SessionStorage.getUser(CircleWall.this);

        database = FirebaseDatabase.getInstance();
        broadcastsDB = database.getReference("Broadcasts");
        circlesPersonelDB = database.getReference("CirclePersonel");
        circlesDB = database.getReference("Circles");
        usersDB = database.getReference("Users").child(user.getUserId());
        broadcastsDB.keepSynced(true);
        currentUser = FirebaseAuth.getInstance();

        circle = SessionStorage.getCircle(CircleWall.this);
        storageReference = FirebaseStorage.getInstance().getReference();


        circleBannerName = findViewById(R.id.circleBannerName);
        exitOrDeleteButton = findViewById(R.id.share_with_friend_button);
        back = findViewById(R.id.bck_Circlewall);
        viewPersonelButton = findViewById(R.id.shareCircle);
        emptyDisplay = findViewById(R.id.circle_wall_empty_display);
        emptyDisplay.setVisibility(View.VISIBLE);
        poll = findViewById(R.id.poll_creation_FAB);
        newPost = findViewById(R.id.message_creation_FAB);
        floatingActionMenu = findViewById(R.id.menu);

        if(circle.getCreatorID().equals(user.getUserId()))
            exitOrDeleteButton.setBackground(getResources().getDrawable(R.drawable.ic_delete_forever_black_24dp));

        circleBannerName.setText(circle.getName());


        exitOrDeleteButton.setOnClickListener(view -> {
            if(circle.getCreatorID().equals(user.getUserId()))
                showDeleteDialog();
            else
                showExitDialog();
        });

        viewPersonelButton.setOnClickListener(view -> {
            Intent intent = new Intent(CircleWall.this, PersonelDisplay.class);
            intent.putExtra("userState",usersState);
            startActivity(intent);
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
            finish();
        });

        poll.setOnClickListener(view -> {
            showCreateBroadcastDialog("poll");
            floatingActionMenu.close(true);

        });
        newPost.setOnClickListener(view -> {
            showCreateBroadcastDialog("message");
            floatingActionMenu.close(true);
        });

        loadCircleBroadcasts();
   }

    private void deleteCircle(){
        circlesPersonelDB.child(circle.getId()).removeValue();
        circlesDB.child(circle.getId()).removeValue();
        //reducing created circle count
        int currentCreatedCount = user.getCreatedCircles()-1;
        user.setCreatedCircles(currentCreatedCount);
        usersDB.child("createdCircles").setValue(currentCreatedCount);
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
    }

    private void exitCircle() {
        circlesPersonelDB.child(circle.getId()).child("members").child(user.getUserId()).removeValue();
        circlesDB.child(circle.getId()).child("membersList").child(user.getUserId()).removeValue();
        //reducing active circle count
        int currentActiveCount = user.getActiveCircles()-1;
        user.setActiveCircles(currentActiveCount);
        usersDB.child("activeCircles").setValue(currentActiveCount);
        startActivity(new Intent(CircleWall.this, ExploreTabbedActivity.class));
    }

    private void loadCircleBroadcasts() {

        //initialize recylcerview
        RecyclerView recyclerView = findViewById(R.id.broadcastViewRecyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
//        ((LinearLayoutManager) layoutManager).setReverseLayout(true);
//        ((LinearLayoutManager) layoutManager).setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        //initializing the CircleDisplayAdapter and setting the adapter to recycler view
        //adapter adds all items from the circle list and displays them in individual cards in the recycler view
        final RecyclerView.Adapter adapter = new BroadcastListAdapter(CircleWall.this, broadcastList, circle);

        broadcastsDB.child(circle.getId()).orderByChild("timeStamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                recyclerView.setAdapter(adapter);
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                broadcastList.add(0, broadcast); //to store timestamp values descendingly
                adapter.notifyDataSetChanged();
                emptyDisplay.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                recyclerView.setAdapter(adapter);

                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = 0;
                List<Broadcast> tempList = new ArrayList<>(broadcastList);
                for(Broadcast b : tempList) {
                    if(b.getId().equals(broadcast.getId())) {
                        broadcastList.remove(position);
                        adapter.notifyItemRemoved(position);
                        broadcastList.add(position, broadcast);
                        adapter.notifyDataSetChanged();
                    }
                    position = position + 1;
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Broadcast broadcast = dataSnapshot.getValue(Broadcast.class);
                int position = 0;
                for (Broadcast b : broadcastList) {
                    if (b.getId().equals(broadcast.getId())) {
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

    public void showExitDialog(){

        confirmationDialog.setContentView(R.layout.exit_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.remove_user_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.remove_user_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            exitCircle();
            confirmationDialog.cancel();
        });

        cancel.setOnClickListener(view -> {
            confirmationDialog.cancel();
        });

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    public void showDeleteDialog(){

        confirmationDialog.setContentView(R.layout.delete_confirmation_popup);
        final Button closeDialogButton = confirmationDialog.findViewById(R.id.delete_circle_accept_button);
        final Button cancel = confirmationDialog.findViewById(R.id.delete_circle_cancel_button);

        closeDialogButton.setOnClickListener(view -> {
            deleteCircle();
            confirmationDialog.dismiss();
        });

        cancel.setOnClickListener(view -> {
            confirmationDialog.dismiss();
        });

        confirmationDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirmationDialog.show();
    }

    private void showCreateBroadcastDialog(String flag) {
        createBroadcastPopup = new Dialog(CircleWall.this);
        createBroadcastPopup.setContentView(R.layout.broadcast_create_popup_layout); //set dialog view

        setMessageET = createBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        descPlaceHolder = createBroadcastPopup.findViewById(R.id.description_placeholder_tv);
        setPollQuestionET = createBroadcastPopup.findViewById(R.id.poll_create_question_editText);
        setPollOptionET = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_editText);
        pollOptionsDisplay = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_display);
        //uploadFileView = createBroadcastPopup.findViewById(R.id.attachmentUploadView);
        pollCreateView = createBroadcastPopup.findViewById(R.id.poll_create_layout);
        additionalSelector = createBroadcastPopup.findViewById(R.id.additional_selector_view);
        //uploadCloudImageView = createBroadcastPopup.findViewById(R.id.create_broadcast_file_upload_cloud_image_btn);
        //tvUploadFileOption = createBroadcastPopup.findViewById(R.id.upload_file_btn);
        btnAddPollOption = createBroadcastPopup.findViewById(R.id.poll_create_answer_option_add_btn);
        btnUploadBroadcast = createBroadcastPopup.findViewById(R.id.upload_broadcast_btn);
        clearBroadcastPopup = createBroadcastPopup.findViewById(R.id.clear_broadcast_popup);
/*
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
*/

        clearBroadcastPopup.setOnClickListener(view -> createBroadcastPopup.dismiss());

        Log.d(TAG, "kwejfnwekjfnfkjwnef " + circle.getId());

        if (flag.equals("poll")) {
            pollCreateView.setVisibility(View.VISIBLE);
            setMessageET.setVisibility(View.GONE);
            descPlaceHolder.setVisibility(View.GONE);
        }

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
            createBroadcast();
        });

        createBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createBroadcastPopup.show();
    }

    private void createBroadcast() {
        createBroadcastPopup.dismiss();
        String currentCircleId = circle.getId();

        String message = null;
        if(!setMessageET.getText().toString().isEmpty())
            message = setMessageET.getText().toString();

        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String pollQuestion = setPollQuestionET.getText().toString();

        String currentUserName = currentUser.getCurrentUser().getDisplayName();
        String currentUserId = currentUser.getCurrentUser().getUid();

        SendNotification.sendBCinfo(broadcastId, circle.getName(), circle.getId(), currentUserName, circle.getMembersList());

        HashMap<String, Integer> options = new HashMap<>();
        if (!pollAnswerOptionsList.isEmpty()) {
            for (String option : pollAnswerOptionsList)
                options.put(option, 0);
        }

        if (downloadUri == null && pollExists == false) {

            Broadcast broadcast = new Broadcast(broadcastId, message, null,
                    currentUserName, currentUserId, false, System.currentTimeMillis(), null, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);
            int newCount = circle.getNoOfBroadcasts() + 1;
            circle.setNoOfBroadcasts(newCount);
            circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
            pollExists = false;
            pollAnswerOptionsList.clear();
        } else if (downloadUri != null && pollExists == false) {

            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri,
                    currentUserName, currentUserId, false, System.currentTimeMillis(), null, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);
            int newCount = circle.getNoOfBroadcasts() + 1;
            circle.setNoOfBroadcasts(newCount);
            circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
            pollExists = false;
            pollAnswerOptionsList.clear();
        } else if (downloadUri == null && pollExists == true) {

            Poll poll = new Poll(pollQuestion, options, null);
            Broadcast broadcast = new Broadcast(broadcastId, message, null,
                    currentUserName, currentUserId, true, System.currentTimeMillis(), poll, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);
            int newCount = circle.getNoOfBroadcasts() + 1;
            circle.setNoOfBroadcasts(newCount);
            circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
            pollExists = false;
            pollAnswerOptionsList.clear();
        } else if (downloadUri != null && pollExists == true) {

            Poll poll = new Poll(pollQuestion, options, null);
            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri,
                    currentUserName, currentUserId, true, System.currentTimeMillis(), poll, user.getProfileImageLink());
            broadcastsDB.child(currentCircleId).child(broadcastId).setValue(broadcast);
            int newCount = circle.getNoOfBroadcasts() + 1;
            circle.setNoOfBroadcasts(newCount);
            circlesDB.child(circle.getId()).child("noOfBroadcasts").setValue(newCount);
            pollExists = false;
            pollAnswerOptionsList.clear();
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
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CircleWall.this, ExploreTabbedActivity.class);
        startActivity(intent);
    }

}