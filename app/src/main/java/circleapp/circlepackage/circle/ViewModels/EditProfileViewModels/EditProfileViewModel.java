package circleapp.circlepackage.circle.ViewModels.EditProfileViewModels;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Helpers.HelperMethods;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.ui.EditProfile.EditProfile;

public class EditProfileViewModel extends ViewModel {
    private MutableLiveData<Boolean> isProfilepicUploaded;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MutableLiveData<Boolean> userObjectUploadProgress(Boolean isUserRegistered, Uri downloadUri, User user, Activity activity) {
        if (!isUserRegistered) {
            isProfilepicUploaded = new MutableLiveData<>();
        }
        else {
            uploadUserProfilePic(downloadUri,user,activity);
        }
        return isProfilepicUploaded;
    }

    private void uploadUserProfilePic(Uri downloadLink, User user, Activity activity) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(downloadLink)
                .build();
        FirebaseWriteHelper.getUser().updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                user.setProfileImageLink(downloadLink.toString());
                FirebaseWriteHelper.updateUser(user, activity);
//                Glide.with(activity).load(downloadLink.toString()).into(profileImageView);
//                HelperMethods.GlideSetProfilePic(activity, String.valueOf(R.drawable.ic_account_circle_black_24dp), profilePic);

            }
        });

    }

}
