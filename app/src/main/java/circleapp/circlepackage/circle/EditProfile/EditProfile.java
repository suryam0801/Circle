package circleapp.circlepackage.circle.EditProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import circleapp.circlepackage.circle.CircleWall.CircleWall;
import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.Explore.Explore;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.ObjectModels.Circle;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles, editIntTagBtn;
    private Button editProfPic, finalizeChanges, logout;
    private ImageButton back;
    private ChipGroup interestTagsEditDisplay;
    private Uri filePath;
    private StorageReference storageReference;
    private Uri downloadUri;
    private Dialog interestTagDialog;
    private List<String> selectedInterestTags;
    private List<String> suggestInList = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    String TAG = EditProfile.class.getSimpleName();
    User user;
    int[] myImageList = new int[]{R.drawable.profile_image, R.drawable.profile_image_black_dude, R.drawable.profile_image_black_woman,
            R.drawable.profile_image_italian_dude, R.drawable.profile_image_lady_glasses};
    boolean emptyDismiss = true;


    private FirebaseDatabase database;
    private DatabaseReference tags, userDB;
    private FirebaseAuth currentUser;

    //locationTags and location-interestTags retrieved from database. interest tags will be display according to selected location tags
    private List<String> dbInterestTags = new ArrayList<>(); //interestTags will be added by parsing through HashMap LocIntTags
    private HashMap<String, Object> locIntTags = new HashMap<>();


    //UI elements for location tag selector popup and interest tag selector popup
    private AutoCompleteTextView interestTagEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        this.setFinishOnTouchOutside(false);


        user = SessionStorage.getUser(EditProfile.this);

        userName = findViewById(R.id.viewProfile_name);
        userNumber = findViewById(R.id.viewProfile_email);
        createdCircles = findViewById(R.id.viewProfileCreatedCirclesCount);
        workingCircles = findViewById(R.id.viewProfileActiveCirclesCount);
        editProfPic = findViewById(R.id.profile_view_profilePicSetterImage);
        profileImageView = findViewById(R.id.profile_view_profile_image);
        finalizeChanges = findViewById(R.id.profile_finalize_changes);
        logout = findViewById(R.id.profile_logout);
        back = findViewById(R.id.bck_view_edit_profile);
        interestTagsEditDisplay = findViewById(R.id.viewProfile_interestTags);
        editIntTagBtn = findViewById(R.id.interestTagEditButton);
        currentUser = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users");

        tags.child("locationInterestTags").child(user.getDistrict()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                locIntTags = (HashMap<String, Object>) snapshot.getValue();

                //load tags
                for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
                    List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
                    for (String interest : tempInterests) {
                        if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                            if(entry.getKey().trim().equals(user.getWard().trim()))
                                dbInterestTags.add(0, interest);
                            else
                                dbInterestTags.add(interest);
                        }
                    }
                }
                selectedInterestTags = new ArrayList<>(user.getInterestTags().keySet());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        userName.setText(user.getFirstName() + " " + user.getLastName());
        userNumber.setText(user.getContact());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");

        selectedInterestTags = new ArrayList<>(user.getInterestTags().keySet());

        //populate chip group with currently selectedTags
        for (String interest : selectedInterestTags)
            setInterestTag(interest, interestTagsEditDisplay);
        Random r = new Random();
        int count = r.nextInt((4 - 0) + 1);
        Glide.with(EditProfile.this)
                .load(user.getProfileImageLink())
                .placeholder(ContextCompat.getDrawable(EditProfile.this, myImageList[count]))
                .into(profileImageView);

        editIntTagBtn.setOnClickListener(view -> {
            displayInterestTagPopup();
        });

        editProfPic.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditProfile.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EditProfile.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                selectFile();
            }

        });

        finalizeChanges.setOnClickListener(view -> {
            if(downloadUri!=null)
                user.setProfileImageLink(downloadUri.toString());

            HashMap<String, Boolean> tempIntTags = new HashMap<>();
            for(String interest : selectedInterestTags)
                tempIntTags.put(interest, true);
            user.setInterestTags(tempIntTags);

            userDB.child(user.getUserId()).setValue(user);
            //storing user as a json in file locally
            String string = new Gson().toJson(user);
            storeUserFile(string, getApplicationContext());
            SessionStorage.saveUser(EditProfile.this, user);
            
            startActivity(new Intent(EditProfile.this, Explore.class));
            finish();
        });

        logout.setOnClickListener(view -> {
            currentUser.signOut();
            currentUser = null;
            startActivity(new Intent(EditProfile.this, PhoneLogin.class));
            finish();
        });

        back.setOnClickListener(view -> {
            startActivity(new Intent(EditProfile.this, Explore.class));
            finish();
        });

    }

    private void storeUserFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("user.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private void displayInterestTagPopup() {
        interestTagDialog = new Dialog(EditProfile.this);
        interestTagDialog.setContentView(R.layout.activity_create_circle_interesttag_dialog); //set dialog view

        //initialize elements in popup dialog
        final Button finalizeInterestTag = interestTagDialog.findViewById(R.id.circle_finalize_interest_tags);
        final ChipGroup interestChipGroupPopup = interestTagDialog.findViewById(R.id.circle_interest_tag_chip_group);
        interestTagEntry = interestTagDialog.findViewById(R.id.circle_interest_tags_entry);
        final Button interestTagAdd = interestTagDialog.findViewById(R.id.circle_interest_tag_add_button);

        for (String interest : dbInterestTags) {
            setInterestTag(interest, interestChipGroupPopup);
            suggestInList.add("#"+interest);
        }
        Log.d(TAG,"Suggestion"+suggestInList.toString());
        String[] arr = suggestInList.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line, arr);
        interestTagEntry.setThreshold(1);
        interestTagEntry.setAdapter(adapter);

        interestTagDialog.setOnDismissListener(dialogInterface -> {
            if(emptyDismiss == false) {
                interestTagsEditDisplay.removeAllViews();
                for (String interest : selectedInterestTags)
                    setInterestTag(interest, interestTagsEditDisplay);
            }
        });

        finalizeInterestTag.setOnClickListener(view -> {
            emptyDismiss = false;
            interestTagDialog.dismiss();
            finalizeChanges.setVisibility(View.VISIBLE);
        });

        interestTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "TOUCHEDDDD!!!!!");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(interestTagEntry, InputMethodManager.SHOW_IMPLICIT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            interestTagEntry.setShowSoftInputOnFocus(true);
                        }
                        interestTagEntry.setText("#");
                        interestTagEntry.setSelection(interestTagEntry.getText().length());
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        interestTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String interestTag = interestTagEntry.getText().toString().replace("#", "");
                if (!interestTag.isEmpty()) {
                    selectedInterestTags.add(interestTag);
                    if (!dbInterestTags.contains(interestTag)) {
                        dbInterestTags.add(interestTag);
                        setInterestTag(interestTag, interestChipGroupPopup);
                    } else {
                        Log.d(TAG, "DB INTEREST TAGS: " + dbInterestTags.toString());
                        Log.d(TAG, "DB SELECTED TAGS: " + selectedInterestTags.toString());
                        interestChipGroupPopup.removeViewAt(dbInterestTags.indexOf(interestTag)+1);
                        setInterestTag(interestTag, interestChipGroupPopup);
                    }
                }

                interestTagEntry.setText("#");
                interestTagEntry.setSelection(interestTagEntry.getText().length());
            }
        });


        interestTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        interestTagDialog.show();
    }

    public void setInterestTag(final String name, ChipGroup chipGroupLocation) {
        final Chip chip = new Chip(this);
        int paddingDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics()
        );
        chip.setRippleColor(ColorStateList.valueOf(Color.WHITE));
        chip.setPadding(
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3,
                        getResources().getDisplayMetrics()
                ),
                paddingDp, paddingDp, paddingDp);
        if (!name.contains("#"))
            chip.setText("#" + name);
        else
            chip.setText(name);

        if (selectedInterestTags.contains(name)) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
            chip.setTextColor(Color.WHITE);
        } else {
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
            chip.setTextColor(Color.BLACK);
        }

        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (chip.getChipBackgroundColor().getDefaultColor() == -9655041) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.chip_unselected_gray)));
                    chip.setTextColor(Color.BLACK);
                    selectedInterestTags.remove(chip.getText().toString().replace("#", ""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedInterestTags.add(chip.getText().toString().replace("#", ""));
                }
                Log.d(TAG, "INTEREST TAG LIST: " + selectedInterestTags.toString());
            }

        });

        if(selectedInterestTags.contains(name))
            chipGroupLocation.addView(chip, 0);
        else
            chipGroupLocation.addView(chip);

    }

    public void selectFile() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_IMAGE_REQUEST);
    }

    //Check whether the permission is granted or not for uploading the profile pic
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(EditProfile.this,
                        "Storage Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
                selectFile();
            } else {
                Toast.makeText(EditProfile.this,
                        "Storage Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    //code for upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            //check the path for the image
            //if the image path is notnull the uploading process will start
            if (filePath != null) {

                //Creating an  custom dialog to show the uploading status
                final ProgressDialog progressDialog = new ProgressDialog(EditProfile.this);
                progressDialog.setTitle("Uploading");
                progressDialog.show();

                //generating random id to store the profliepic
                String id = UUID.randomUUID().toString();
                final StorageReference profileRef = storageReference.child("ProfilePics/" + id);

                //storing  the pic
                profileRef.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
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
                                return profileRef.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        finalizeChanges.setVisibility(View.VISIBLE);
                        //and displaying a success toast
                        downloadUri = uri;
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        currentUser.getCurrentUser().updateProfile(profileUpdates);
                        Glide.with(EditProfile.this).load(downloadUri.toString()).into(profileImageView);

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
            }
        }
    }
}
