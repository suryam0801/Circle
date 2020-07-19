package circleapp.circlepackage.circle.Utils.UploadImages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;
import java.util.UUID;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;

public class ImageUpload {
    public void imageUpload(Activity activity, Uri filePath){
        if (filePath != null) {
            //Creating an  custom dialog to show the uploading status
            final ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            //generating random id to store the profliepic
            String id = UUID.randomUUID().toString();
            final StorageReference profileRef = FirebaseWriteHelper.getStorageReference("ProfilePics/" + id);

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
                                throw Objects.requireNonNull(task.getException());
                            }

                            // Continue with the task to get the download URL
                            return profileRef.getDownloadUrl();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    progressDialog.dismiss();
                    ImageUploadSuccessListener.isImageUploadSuccess(uri, filePath);
                    //and displaying a success toast
//                        Toast.makeText(getApplicationContext(), "Profile Pic Uploaded " + uri.toString(), Toast.LENGTH_LONG).show();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build();
                    FirebaseWriteHelper.getUser().updateProfile(profileUpdates);
                    //Glide.with(activity).load(filePath).into(profilePic);

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            ImageUploadSuccessListener.isImageUploadFailure(exception);
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}
