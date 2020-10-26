package circleapp.circleapppackage.circle.Utils.UploadImages;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import circleapp.circleapppackage.circle.DataLayer.StorageReferenceRepository;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class ImageUpload extends ViewModel {

    private GlobalVariables globalVariables = new GlobalVariables();

    private MutableLiveData<String[]> progressPercentageAndLink;
    public MutableLiveData<String[]> uploadImageWithProgress(Uri filePath, boolean isFile) {
        if (filePath == null) {
            progressPercentageAndLink = new MutableLiveData<>();
        }
        else {
            imageUpload(filePath, isFile);
        }
        return progressPercentageAndLink;
    }
    public void imageUpload(Uri filePath, boolean isFile){
        if (filePath != null) {
            //Creating an  custom dialog to show the uploading status

            //generating random id to store the profliepic
            String id = UUID.randomUUID().toString();
            StorageReferenceRepository storageReferenceRepository = new StorageReferenceRepository();
            StorageReference profileRef = storageReferenceRepository.getStorageReference("ProfilePics/" + id);
            if (isFile) {
                Uri file = Uri.fromFile(new File(filePath.toString()));
                String[] returnValue = {filePath.toString(), "" + Math.round(0.0)};
                progressPercentageAndLink.setValue(returnValue);
                profileRef = storageReferenceRepository.getStorageReference("Files/" + id);
                UploadTask uploadTask = profileRef.putFile(file);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        String[] returnValue = {"fail", "" + -1};
                        progressPercentageAndLink.setValue(returnValue);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        if (globalVariables.getAuthenticationToken().getCurrentUser() != null) {
                            String[] returnValue = {taskSnapshot.getUploadSessionUri().toString(), "" + 100};
                            progressPercentageAndLink.setValue(returnValue);
                        }
                    }
                });



            } else {
                //storing  the pic
                StorageReference finalProfileRef = profileRef;
                profileRef.putFile(filePath).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        String[] returnValue = {filePath.toString(), "" + Math.round(progress)};
                        progressPercentageAndLink.setValue(returnValue);

                        //displaying percentage in progress dialog
                    }
                })
                        .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw Objects.requireNonNull(task.getException());
                                }
                                // Continue with the task to get the download URL
                                return finalProfileRef.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        //and displaying a success toast
//                        Toast.makeText(getApplicationContext(), "Profile Pic Uploaded " + uri.toString(), Toast.LENGTH_LONG).show();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();
                        if (globalVariables.getAuthenticationToken().getCurrentUser() != null) {
                            globalVariables.getAuthenticationToken().getCurrentUser().updateProfile(profileUpdates);
                            String[] returnValue = {uri.toString(), "" + 100};
                            progressPercentageAndLink.setValue(returnValue);
                        } else {
                            String[] returnValue = {"", -1 + ""};
                            progressPercentageAndLink.setValue(returnValue);
                        }

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                            }
                        });
            }
        }
    }
}
