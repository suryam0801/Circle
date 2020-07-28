package circleapp.circlepackage.circle.ViewModels;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;

import circleapp.circlepackage.circle.DataRepository.CirclesRepository;
import circleapp.circlepackage.circle.DataRepository.ParticularCirclesRepository;
import circleapp.circlepackage.circle.DataRepository.UserRepository;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.Circle;
import circleapp.circlepackage.circle.data.ObjectModels.User;

public class ParticularCircleViewModel extends ViewModel {
    private GlobalVariables globalVariables = new GlobalVariables();
    public MutableLiveData<Circle> particularCircle = new MutableLiveData<>();
    public MutableLiveData<Circle> getParticularCircle(Context context, String circleId) {
        getSingleCircle(context, circleId);
        return particularCircle;
    }
    private void getSingleCircle(Context context, String circleId){
        if(globalVariables.getAuthenticationToken().getCurrentUser() != null){
            ParticularCirclesRepository particularCirclesRepository = new ParticularCirclesRepository();
            LiveData<DataSnapshot> liveData = particularCirclesRepository.getDataSnapsParticularCircleLiveData(circleId);
            liveData.observe((LifecycleOwner) context, dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    Circle circle = dataSnapshot.getValue(Circle.class);
                    particularCircle.postValue(circle);
                    globalVariables.saveCurrentCircle(circle);
                }
            });
        }
    }

}
