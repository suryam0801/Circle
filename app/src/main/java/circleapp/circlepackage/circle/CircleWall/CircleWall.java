package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.MainDisplay.Explore;
import circleapp.circlepackage.circle.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.Poll;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class CircleWall extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference pollsDB, broadcastsDB;

    private String TAG = CircleWall.class.getSimpleName();
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 100;
    private StorageReference storageReference;
    private Uri filePath = null;
    private String downloadUri = null;

    private Circle circle;
    private FloatingActionButton createNewBroadcast;

    private List<String> pollAnswerOptionsList = new ArrayList<>( );
    private boolean pollExists = false;
    private String pollQuestion;

    //create broadcast popup ui elements
    private EditText setMessageET, setPollQuestionET, setPollOptionET;
    private LinearLayout uploadFileView, pollCreateView, additionalSelector, pollOptionsDisplay;
    private ImageView uploadCloudImageView;
    private TextView tvUploadFileOption, tvCreatePollOption, tvMiddleOrPlaceHolder, tvUploadPlaceholderText;
    private Button btnAddPollOption, btnUploadBroadcast;
    private Dialog createBroadcastPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);

        database = FirebaseDatabase.getInstance();
        pollsDB = database.getReference("Polls");
        broadcastsDB = database.getReference("Broadcasts");

        circle = SessionStorage.getCircle(CircleWall.this);
        storageReference = FirebaseStorage.getInstance().getReference();

        createNewBroadcast = findViewById(R.id.create_new_broadcast_btn);

        createNewBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateBroadcastDialog();
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

        tvUploadFileOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvUploadFileOption.setVisibility(View.GONE);
                tvMiddleOrPlaceHolder.setVisibility(View.GONE);
                uploadFileView.setVisibility(View.VISIBLE);
                if(tvCreatePollOption.getVisibility() == View.GONE)
                    additionalSelector.setVisibility(View.GONE);
            }
        });

        tvCreatePollOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvCreatePollOption.setVisibility(View.GONE);
                tvMiddleOrPlaceHolder.setVisibility(View.GONE);
                pollCreateView.setVisibility(View.VISIBLE);
                if(tvUploadFileOption.getVisibility() == View.GONE)
                    additionalSelector.setVisibility(View.GONE);
            }
        });

        uploadFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(CircleWall.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CircleWall.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
                } else {
                    selectFile();
                }
            }
        });

        btnAddPollOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String option = setPollOptionET.getText().toString();
                if(!option.isEmpty() && !setPollQuestionET.getText().toString().isEmpty()){
                    LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 110);
                    lparams.setMargins(0, 10, 20, 0);
                    final TextView tv = new TextView(CircleWall.this);
                    tv.setLayoutParams(lparams);
                    tv.setText(option);
                    tv.setTextColor(Color.BLACK);
                    tv.setGravity(Gravity.CENTER_VERTICAL);
                    tv.setBackground(getResources().getDrawable(R.drawable.light_blue_rounded_background));
                    tv.setCompoundDrawablesWithIntrinsicBounds(0,0 , R.drawable.ic_clear_blue_24dp, 0);
                    tv.setPaddingRelative(40,10,40,10);
                    tv.setTextColor(Color.parseColor("#6CACFF"));
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
                } else {
                    Toast.makeText(getApplicationContext(), "Fill out all poll fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUploadBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!setMessageET.getText().toString().isEmpty())
                    createBroadcast();
                else
                    Toast.makeText(getApplicationContext(), "Set your broadcast message", Toast.LENGTH_SHORT).show();
            }
        });

        createBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        createBroadcastPopup.show();
    }

    private void createBroadcast() {
        String currentCircleId = circle.getId();

        String message = setMessageET.getText().toString();
        String broadcastId = broadcastsDB.child(currentCircleId).push().getKey();
        String pollId = pollsDB.child(currentCircleId).push().getKey();

        if(downloadUri == null && pollExists == false){
            Broadcast broadcast = new Broadcast(broadcastId, message, null, null, false);
            broadcastsDB.child(currentCircleId).push().setValue(broadcast);
        } else if (downloadUri != null && pollExists == false) {
            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri, null, false);
            broadcastsDB.child(currentCircleId).push().setValue(broadcast);
        } else if (downloadUri == null && pollExists == true) {
            Broadcast broadcast = new Broadcast(broadcastId, message, null, pollId, true);
            Poll poll = new Poll(pollId, pollQuestion, pollAnswerOptionsList, null);
            broadcastsDB.child(currentCircleId).push().setValue(broadcast);
        } else if (downloadUri != null && pollExists == true){
            Broadcast broadcast = new Broadcast(broadcastId, message, downloadUri, pollId, true);
            Poll poll = new Poll(pollId, pollQuestion, pollAnswerOptionsList, null);
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
}