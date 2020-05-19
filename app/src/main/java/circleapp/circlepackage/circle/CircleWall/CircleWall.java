package circleapp.circlepackage.circle.CircleWall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;

public class CircleWall extends AppCompatActivity {


    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 100;
    private StorageReference storageReference;
    private Uri filePath = null;
    private String downloadUri, uniqueID;

    private Circle circle;
    private FloatingActionButton createNewBroadcast;

    //create broadcast popup ui elements
    private EditText setMessageET, setPollQuestionET, setPollAnswerET;
    private LinearLayout uploadFileView, pollCreateView, additionalSelector;
    private ImageView uploadCloudImageView;
    private TextView tvUploadFileOption, tvCreatePollOption, tvMiddleOrPlaceHolder, tvUploadPlaceholderText;
    private AlertDialog alertDialog;
    private Button btnAddPollOption, btnUploadBroadcast;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private View dialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle_wall);

        circle = SessionStorage.getCircle(CircleWall.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        uniqueID = UUID.randomUUID().toString();

        createNewBroadcast = findViewById(R.id.create_new_broadcast_btn);

        createNewBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateBroadcastDialog();
            }
        });
    }

    private void showCreateBroadcastDialog() {
        builder = new AlertDialog.Builder(CircleWall.this);
        inflater = getLayoutInflater();
        dialogue = inflater.inflate(R.layout.broadcast_create_popup_layout, null);
        builder.setView(dialogue);

        setMessageET = dialogue.findViewById(R.id.broadcastDescriptionEditText);
        setPollQuestionET = dialogue.findViewById(R.id.poll_create_question_editText);
        setPollAnswerET = dialogue.findViewById(R.id.poll_create_answer_option_editText);
        uploadFileView = dialogue.findViewById(R.id.attachmentUploadView);
        pollCreateView = dialogue.findViewById(R.id.poll_create_layout);
        additionalSelector = dialogue.findViewById(R.id.additional_selector_view);
        uploadCloudImageView = dialogue.findViewById(R.id.create_broadcast_file_upload_cloud_image_btn);
        tvUploadFileOption = dialogue.findViewById(R.id.upload_file_btn);
        tvCreatePollOption = dialogue.findViewById(R.id.poll_create_btn);
        tvMiddleOrPlaceHolder = dialogue.findViewById(R.id.upload_or_poll_or_textview);
        tvUploadPlaceholderText = dialogue.findViewById(R.id.create_broadcast_file_upload_text);
        btnAddPollOption = dialogue.findViewById(R.id.poll_create_answer_option_add_btn);
        btnUploadBroadcast = dialogue.findViewById(R.id.upload_broadcast_btn);

        tvUploadFileOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvUploadFileOption.setVisibility(View.GONE);
                tvMiddleOrPlaceHolder.setVisibility(View.GONE);
                uploadFileView.setVisibility(View.VISIBLE);
            }
        });

        tvCreatePollOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvCreatePollOption.setVisibility(View.GONE);
                tvMiddleOrPlaceHolder.setVisibility(View.GONE);
                pollCreateView.setVisibility(View.VISIBLE);
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

        alertDialog = builder.create();
        alertDialog.show();
    }

    public void selectFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

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