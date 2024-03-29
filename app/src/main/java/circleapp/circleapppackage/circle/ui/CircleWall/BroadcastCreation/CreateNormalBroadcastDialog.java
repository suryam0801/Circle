package circleapp.circleapppackage.circle.ui.CircleWall.BroadcastCreation;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import java.util.HashMap;

import circleapp.circleapppackage.circle.DataLayer.UserRepository;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.CircleWall.CircleWallViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.CircleWall;

public class CreateNormalBroadcastDialog {
    public boolean pollExists = false;
    public boolean imageExists = false;
    private Dialog createNormalBroadcastPopup;
    private EditText setTitleET, setMessageET;
    private TextView broadcastHeader;
    private Button btnUploadNormalBroadcast, cancelNormalButton;
    Activity activity;
    CircleWall circleWall;
    GlobalVariables globalVariables;
    private CircleWallViewModel circleWallViewModel;
    User user;
    Circle circle;
    public void showCreateNormalBroadcastDialog(Activity activity) {
        this.activity = activity;
        InitUI();
        cancelNormalButton.setOnClickListener(view -> createNormalBroadcastPopup.dismiss());
        btnUploadNormalBroadcast.setOnClickListener(view -> {
            if (setTitleET.getText().toString().isEmpty())
                Toast.makeText(activity, "The Post cant be empty", Toast.LENGTH_SHORT).show();
            else
            {
                String description,title;
                title = setTitleET.getText().toString();
                if(setMessageET.getText()==null)
                    description=null;
                else
                    description=setMessageET.getText().toString();

                circleWallViewModel = ViewModelProviders.of((FragmentActivity) activity).get(CircleWallViewModel.class);
                circleWallViewModel.createBroadcast(title,description,circle,user,activity, globalVariables.getCircleWallPersonel()).observe((LifecycleOwner) activity, state->{
                    if (state){
                        livedataobserver(user,circle);
                    }
                    else {
                        createNormalBroadcastPopup.dismiss();
                        Toast.makeText(activity,"Error while Creating broadcast",Toast.LENGTH_SHORT).show();
                    }

                });}
        });
        createNormalBroadcastPopup.show();
    }

    private void InitUI() {
        createNormalBroadcastPopup = new Dialog(activity);
        createNormalBroadcastPopup.setContentView(R.layout.normal_broadcast_create_popup); //set dialog view
        createNormalBroadcastPopup.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        createNormalBroadcastPopup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        circleWall = new CircleWall();
        globalVariables = new GlobalVariables();
        user = globalVariables.getCurrentUser();
        circle = globalVariables.getCurrentCircle();
        broadcastHeader = createNormalBroadcastPopup.findViewById(R.id.broadcast_header);
        btnUploadNormalBroadcast = createNormalBroadcastPopup.findViewById(R.id.upload_normal_broadcast_btn);
        cancelNormalButton = createNormalBroadcastPopup.findViewById(R.id.create_normal_broadcast_cancel_btn);
        setTitleET = createNormalBroadcastPopup.findViewById(R.id.broadcastTitleEditText);
        setTitleET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        setMessageET = createNormalBroadcastPopup.findViewById(R.id.broadcastDescriptionEditText);
        setMessageET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

    }

    private void livedataobserver(User user, Circle circle) {
        this.pollExists = false;
        this.imageExists = false;
        if (user.getNotificationsAlert() != null) {
            HashMap<String, Integer> newNotifs = new HashMap<>(user.getNotificationsAlert());
            newNotifs.put(circle.getId(), circle.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
//            SessionStorage.saveUser(activity, user);
            globalVariables.saveCurrentUser(user);
            UserRepository userRepository = new UserRepository();
            userRepository.updateUserCount(user.getUserId(), circle.getId(), circle.getNoOfBroadcasts());
        } else {
            HashMap<String, Integer> newNotifs = new HashMap<>();
            newNotifs.put(circle.getId(), circle.getNoOfBroadcasts());
            user.setNotificationsAlert(newNotifs);
//            SessionStorage.saveUser(activity, user);
            globalVariables.saveCurrentUser(user);
        }
        createNormalBroadcastPopup.dismiss();
    }
}
