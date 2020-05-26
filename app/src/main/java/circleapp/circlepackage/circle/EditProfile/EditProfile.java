package circleapp.circlepackage.circle.EditProfile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import circleapp.circlepackage.circle.CreateCircle;
import circleapp.circlepackage.circle.Login.GatherUserDetails;
import circleapp.circlepackage.circle.Login.PhoneLogin;
import circleapp.circlepackage.circle.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.SessionStorage;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView userName, userNumber, createdCircles, workingCircles, editLocTagBtn, editIntTagBtn;
    private Button editProfPic, finalizeChanges, logout;
    private ImageButton back;
    private ChipGroup locationTagsEditDisplay, interestTagsEditDisplay;
    private Uri filePath;
    private StorageReference storageReference;
    private Uri downloadUri;
    private Dialog interestTagDialog, locationTagDialog;
    private List<String> locationTagList, interestTagList, selectedLocationTags, selectedInterestTags;
    private List<String> suggestInList = new ArrayList<>(),suggestlocList = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    String TAG = EditProfile.class.getSimpleName();
    User user;

    private FirebaseDatabase database;
    private DatabaseReference tags, userDB;
    private FirebaseAuth currentUser;

    //locationTags and location-interestTags retrieved from database. interest tags will be display according to selected location tags
    private List<String> dbLocationTags = new ArrayList<>(), dbInterestTags = new ArrayList<>(); //interestTags will be added by parsing through HashMap LocIntTags
    private HashMap<String, Object> locIntTags = new HashMap<>();


    //UI elements for location tag selector popup and interest tag selector popup
    private AutoCompleteTextView locationTagEntry, interestTagEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

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
        locationTagsEditDisplay = findViewById(R.id.viewProfile_locationTags);
        interestTagsEditDisplay = findViewById(R.id.viewProfile_interestTags);
        editLocTagBtn = findViewById(R.id.locationTagEditButton);
        editIntTagBtn = findViewById(R.id.interestTagEditButton);
        currentUser = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance();
        tags = database.getReference("Tags");
        userDB = database.getReference("Users");

        tags.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashMap<String, Object> tagsDBRetrieved = (HashMap<String, Object>) snapshot.getValue();

                dbLocationTags = new ArrayList<>(((HashMap<String, Boolean>) tagsDBRetrieved.get("locationTags")).keySet());
                locIntTags = (HashMap<String, Object>) tagsDBRetrieved.get("locationInterestTags");

                //geting location tags and location-interestTags from the database
                for (HashMap.Entry<String, Object> entry : locIntTags.entrySet()) {
                    List<String> tempInterests = new ArrayList<>(((HashMap<String, Boolean>) entry.getValue()).keySet());
                    for (String interest : tempInterests) {
                        if (!dbInterestTags.contains(interest)) { //avoid duplicate interests
                            dbInterestTags.add(interest);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        userName.setText(user.getFirstName() + " " + user.getLastName());
        userNumber.setText(user.getContact());
        createdCircles.setText(user.getCreatedCircles() + "");
        workingCircles.setText(user.getActiveCircles() + "");

        selectedLocationTags = new ArrayList<>(user.getLocationTags().keySet());
        selectedInterestTags = new ArrayList<>(user.getInterestTags().keySet());

        //populate chip group with currently selectedTags
        for (String loc : selectedLocationTags)
            setLocationTags(loc, locationTagsEditDisplay);
        for (String interest : selectedInterestTags)
            setInterestTag(interest, interestTagsEditDisplay);

        Glide.with(EditProfile.this)
                .load(user.getProfileImageLink())
                .placeholder(ContextCompat.getDrawable(EditProfile.this, R.drawable.profile_image))
                .into(profileImageView);

        editIntTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                interestTagsEditDisplay.removeAllViews();
                displayInterestTagPopup();
            }
        });

        editLocTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationTagsEditDisplay.removeAllViews();
                displayLocationTagPopup();
            }
        });

        editProfPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditProfile.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfile.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
                } else {
                    selectFile();
                }

            }
        });

        finalizeChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(downloadUri!=null)
                    user.setProfileImageLink(downloadUri.toString());
                HashMap<String, Boolean> tempLocTags = new HashMap<>();
                for(String loc : selectedLocationTags)
                    tempLocTags.put(loc, true);
                user.setLocationTags(tempLocTags);

                HashMap<String, Boolean> tempIntTags = new HashMap<>();
                for(String interest : selectedInterestTags)
                    tempIntTags.put(interest, true);
                user.setInterestTags(tempIntTags);

                userDB.child(user.getUserId()).setValue(user);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser.signOut();
                startActivity(new Intent(EditProfile.this, PhoneLogin.class));
                finish();
            }
        });

    }

    private void displayLocationTagPopup() {
        locationTagDialog = new Dialog(EditProfile.this);
        locationTagDialog.setContentView(R.layout.activity_create_circle_locationtag_dialog); //set dialog view

        //initialize elements in popup dialog
        final Button finalizeLocationTag = locationTagDialog.findViewById(R.id.circle_finalize_location_tags);
        final ChipGroup locationChipGroupPopup = locationTagDialog.findViewById(R.id.circle_location_tag_chip_group);
        locationTagEntry = locationTagDialog.findViewById(R.id.circle_location_tags_entry);
        final Button locationTagAdd = locationTagDialog.findViewById(R.id.circle_location_tag_add_button);

        for (String loc : dbLocationTags) {
            setLocationTags(loc, locationChipGroupPopup);
            suggestlocList.add("#"+loc);
        }
        Log.d(TAG,"Suggestion"+suggestlocList.toString());
        String[] arr = suggestlocList.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_dropdown_item_1line, arr);
        locationTagEntry.setThreshold(1);
        locationTagEntry.setAdapter(adapter);

        finalizeLocationTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (String loc : selectedLocationTags)
                    setLocationTags(loc, locationTagsEditDisplay);
                locationTagDialog.dismiss();
                finalizeChanges.setVisibility(View.VISIBLE);
            }
        });

        locationTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(locationTagEntry, InputMethodManager.SHOW_IMPLICIT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            locationTagEntry.setShowSoftInputOnFocus(true);
                        }
                        locationTagEntry.setText("#");
                        locationTagEntry.setSelection(locationTagEntry.getText().length());
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

        locationTagAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTag = locationTagEntry.getText().toString().replace("#", "");
                if (!locationTag.isEmpty()) {
                    selectedLocationTags.add(locationTag);
                    if (!dbLocationTags.contains(locationTag))
                        dbLocationTags.add(locationTag);
                    setLocationTags(locationTag, locationChipGroupPopup);
                }
            }
        });


        locationTagDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        locationTagDialog.show();

    }

    private void setLocationTags(final String name, ChipGroup chipGroupLocation) {
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

        if (selectedLocationTags.contains(name)) {
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
                    selectedLocationTags.remove(chip.getText().toString().replace("#", ""));
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.color_blue)));
                    chip.setTextColor(Color.WHITE);
                    selectedLocationTags.add(chip.getText().toString().replace("#", ""));
                }

            }

        });

        chipGroupLocation.addView(chip);
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

        finalizeInterestTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (String interest : selectedInterestTags)
                    setInterestTag(interest, interestTagsEditDisplay);
                interestTagDialog.dismiss();
                finalizeChanges.setVisibility(View.VISIBLE);
            }
        });

        interestTagEntry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                    if (!dbInterestTags.contains(interestTag))
                        dbInterestTags.add(interestTag);

                    setInterestTag(interestTag, interestChipGroupPopup);
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
