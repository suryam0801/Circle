package circleapp.circleapppackage.circle.ViewModels.CircleWall;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import circleapp.circleapppackage.circle.Utils.GlobalVariables;

public class ImageUrlViewModel extends ViewModel {
    private MutableLiveData<Boolean> isImageUploaded = new MutableLiveData<>();

    public MutableLiveData<Boolean> listenForLocationUpdates(Boolean imageUploadedOrNot) {
        if (!imageUploadedOrNot) {
            checkIfDownloadLinkExists();
        }
        return isImageUploaded;
    }
    public void checkIfDownloadLinkExists(){
        GlobalVariables globalVariables = new GlobalVariables();
        if(globalVariables.getCommentDownloadLink()!=null){
            isImageUploaded.postValue(true);
        }
    }
}
