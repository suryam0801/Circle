package circleapp.circlepackage.circle.Utils.UploadImages;

import android.net.Uri;

public interface ImageUploadSuccessListener {
    static void isImageUploadSuccess(Uri downloadUri, Uri localFilePath) {

    }

    static void isImageUploadFailure(Exception exception) {

    }
}
