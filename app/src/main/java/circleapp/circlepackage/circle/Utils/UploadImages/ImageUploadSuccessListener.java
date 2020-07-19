package circleapp.circlepackage.circle.Utils.UploadImages;

import android.net.Uri;

public interface ImageUploadSuccessListener {
    void isImageUploadSuccess(Uri downloadUri, Uri localFilePath);
}
