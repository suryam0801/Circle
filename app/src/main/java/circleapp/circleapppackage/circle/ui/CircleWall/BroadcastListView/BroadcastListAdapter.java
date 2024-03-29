package circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.ViewModels.CircleWall.BroadcastListViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageImageDisplay;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageView.FullPageBroadcastCardView;
import circleapp.circleapppackage.circle.ui.CircleWall.PollResults.CreatorPollAnswersView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.webkit.MimeTypeMap.getFileExtensionFromUrl;

public class BroadcastListAdapter extends RecyclerView.Adapter<BroadcastListAdapter.ViewHolder> {
    private List<Broadcast> broadcastList;
    private Context context;
    private Circle circle;
    private User user;
    private Dialog deleteBroadcastConfirmation;
    private GlobalVariables globalVariables = new GlobalVariables();
    private MyCirclesViewModel particularCircleViewModel;
    private LiveData<DataSnapshot> currentCircleLiveData;
    private boolean broadcastMuted;
    private BroadcastListViewModel broadcastListViewModel = new BroadcastListViewModel();

    //contructor to set latestCircleList and context for Adapter
    public BroadcastListAdapter(Context context, List<Broadcast> broadcastList, Circle circle, User user) {
        this.context = context;
        this.broadcastList = broadcastList;
        this.circle = circle;
        this.user = user;
    }

    @Override
    public BroadcastListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.broadcast_display_view, viewGroup, false);
        return new BroadcastListAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final BroadcastListAdapter.ViewHolder viewHolder, int i) {

        //init values
        Broadcast broadcast = broadcastList.get(i);
        getLiveCircleData();
        //UI actions
        updateIconOfPost(viewHolder, broadcast);
        setNewCommentsTextView(viewHolder, broadcast);
        setUiElements(viewHolder, i, broadcast);
        setButtonListeners(viewHolder, i, broadcast);
        muteButtonToggleActions(viewHolder, broadcast);

    }
    private void getLiveCircleData(){
        particularCircleViewModel = ViewModelProviders.of((FragmentActivity) context).get(MyCirclesViewModel.class);
        currentCircleLiveData = particularCircleViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        currentCircleLiveData.observe((LifecycleOwner) context, dataSnapshot -> {
            Circle tempCircle = dataSnapshot.getValue(Circle.class);
            if(tempCircle!=null){
                globalVariables.saveCurrentCircle(circle);
                circle = tempCircle;
            }
        });
    }

    private void updateIconOfPost(ViewHolder viewHolder, Broadcast broadcast){
        HelperMethodsUI.setPostIcon(broadcast,viewHolder.profPicDisplay, context);
    }

    private void setNewCommentsTextView(ViewHolder viewHolder, Broadcast broadcast){
        //calculating and setting time elapsed
        long currentTime = System.currentTimeMillis();
        long createdTime = broadcast.getTimeStamp();
        String timeElapsed = HelperMethodsUI.getTimeElapsed(currentTime, createdTime);
        viewHolder.timeElapsedDisplay.setText(timeElapsed);

        //new comments setter
        String commentsDisplayText;
        if(circle.getNoOfCommentsPerBroadcast()==null)
            commentsDisplayText = 0 + " messages";
        else if(circle.getNoOfCommentsPerBroadcast().get(broadcast.getId())==null)
            commentsDisplayText = 0 + " messages";
        else
            commentsDisplayText = circle.getNoOfCommentsPerBroadcast().get(broadcast.getId()) + " messages";
        viewHolder.viewComments.setText(commentsDisplayText);
    }

    private void setUiElements(ViewHolder viewHolder, int i, Broadcast broadcast){
        viewHolder.broadcastTitle.setText(broadcast.getTitle());
        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        //setting the main view
        if (broadcast.getMessage() != null) {
            viewHolder.broadcastMessageDisplay.setVisibility(View.VISIBLE);
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());
        }

        if (broadcast.isImageExists() == true)
            ifImageExistsAction(viewHolder, broadcast, i);

        if (broadcast.isPollExists() == true)
            ifPollExistsAction(viewHolder, broadcast, user);
        if(broadcast.isFileExists() == true)
            ifFileExistsAction(viewHolder, i, broadcast);

        if(broadcast.getMessage()!=null){
            if(broadcast.getMessage().length()>240)
                viewHolder.readMoreTextView.setVisibility(View.VISIBLE);
        }

    }

    private void ifFileExistsAction(ViewHolder viewHolder, int position, Broadcast broadcast){
        viewHolder.docText.setVisibility(View.VISIBLE);
        viewHolder.imageDisplayHolder.setVisibility(View.VISIBLE);
        viewHolder.imageDisplay.setVisibility(View.VISIBLE);
        viewHolder.imageLoadProgressBar.setVisibility(View.GONE);

        //setting imageview
        int profilePic = Integer.parseInt(String.valueOf(R.drawable.file_download));
        Glide.with((Activity) context)
                .load(ContextCompat.getDrawable(context, profilePic))
                .into(viewHolder.imageDisplay);

        //navigate to full screen photo display when clicked
        viewHolder.imageDisplay.setOnClickListener(view -> {
            if(broadcast.isPollExists())
                downloadFile(Uri.parse(broadcast.getAttachmentURI()),circle.getName()+"_"+broadcast.getPoll().getQuestion());
            else
                downloadFile(Uri.parse(broadcast.getAttachmentURI()),circle.getName()+"_"+broadcast.getTitle());
        });
    }

    private void downloadFile(Uri uri, String filename){
        Toast.makeText(context, "Your file is downloading",Toast.LENGTH_SHORT).show();
        DownloadManager.Request r = new DownloadManager.Request(uri);

// This put the download in the same Download dir the browser uses
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

// When downloading music and videos they will be listed in the player
// (Seems to be available since Honeycomb only)
        r.allowScanningByMediaScanner();

// Notify user when download is completed
// (Seems to be available since Honeycomb only)
        r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

// Start download
        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(r);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setButtonListeners(ViewHolder viewHolder, int i, Broadcast broadcast){
        //view discussion onclick
        viewHolder.viewComments.setOnClickListener(view -> intentToDiscussionActivity(i));
        viewHolder.newCommentsTopNotifContainer.setOnClickListener(view -> intentToDiscussionActivity(i));
        viewHolder.container.setOnClickListener(view -> intentToDiscussionActivity(i));

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
            globalVariables.saveCurrentBroadcast(broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });

        viewHolder.viewPollResultsImage.setOnClickListener(view -> {
            globalVariables.saveCurrentBroadcast(broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });

        viewHolder.broadcastMessageDisplay.setOnLongClickListener(v->{
            String [] options = {"Copy"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which==0)
                    {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("label", viewHolder.broadcastMessageDisplay.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Copied to Clipboard",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
            return true;
        });

        viewHolder.readMoreTextView.setOnClickListener(v->{
            viewHolder.readLessTextView.setVisibility(View.VISIBLE);
            viewHolder.readMoreTextView.setVisibility(View.GONE);
            viewHolder.broadcastMessageDisplay.setMaxLines(50);
        });

        viewHolder.readLessTextView.setOnClickListener(v->{
            viewHolder.readMoreTextView.setVisibility(View.VISIBLE);
            viewHolder.readLessTextView.setVisibility(View.GONE);
            viewHolder.broadcastMessageDisplay.setMaxLines(5);
        });

        viewHolder.container.setOnLongClickListener(v -> {
            if (broadcast.getCreatorID().equals(user.getUserId())) {
                deleteBroadcastConfirmation = new Dialog(context);
                showDeleteBroadcastDialog(broadcast);
            } else{
                deleteBroadcastConfirmation = new Dialog(context);
                HelperMethodsUI.showReportAbusePopup(deleteBroadcastConfirmation, context, circle.getId(), broadcast.getId(), "", broadcast.getCreatorID(), user.getUserId());
            }
            return true;
        });
    }

    private void muteButtonToggleActions(ViewHolder viewHolder, Broadcast broadcast){

        broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());
        if (broadcastMuted) {
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
        } else {
            int noOfUserUnread=0;
            if(circle.getNoOfCommentsPerBroadcast()==null){
                noOfUserUnread = 0;
            }
            else if(!circle.getNoOfCommentsPerBroadcast().containsKey(broadcast.getId())){
                noOfUserUnread = 0;
            }
            else if(user.getNoOfReadDiscussions()!=null&&circle.getNoOfCommentsPerBroadcast().containsKey(broadcast.getId())){
                if(user.getNoOfReadDiscussions().get(broadcast.getId())==null)
                    noOfUserUnread = circle.getNoOfCommentsPerBroadcast().get(broadcast.getId());
                else
                    noOfUserUnread = circle.getNoOfCommentsPerBroadcast().get(broadcast.getId()) - user.getNoOfReadDiscussions().get(broadcast.getId());
            }
            if (noOfUserUnread > 0) {
                viewHolder.newCommentsTopNotifContainer.setVisibility(View.VISIBLE);
                viewHolder.newCommentsTopTv.setText(noOfUserUnread + "");
            }
        }

        viewHolder.broadcastListenerToggle.setOnClickListener(view -> {
            updateUserOnMuteToggle(broadcast, viewHolder);
        });
    }

    public void updateUserOnMuteToggle(Broadcast broadcast, ViewHolder viewHolder) {
        List<String> userMutedArray;
        if (user.getMutedBroadcasts() != null)
            userMutedArray = new ArrayList<>(user.getMutedBroadcasts());
        else
            userMutedArray = new ArrayList<>();

        boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());

        if (broadcastMuted) {
            //Unmute broadcast, remove user from not listening list
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_listening_icon));
            userMutedArray.remove(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            broadcastListViewModel.updateListenerListOfBroadcast(0, user, circle.getId(), broadcast.getId());
        } else {
            //Mute broadcast, add user to not listening list
            viewHolder.broadcastListenerToggle.setBackground(context.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
            userMutedArray.add(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            broadcastListViewModel.updateListenerListOfBroadcast(1, user, circle.getId(), broadcast.getId());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void intentToDiscussionActivity(int position) {
        globalVariables.saveCurrentBroadcastList(broadcastList);
        Intent intent = new Intent(context, FullPageBroadcastCardView.class);
        intent.putExtra("broadcastPosition", position);
        context.startActivity(intent);
        ((Activity) context).finishAfterTransition();
    }

    public void ifImageExistsAction(ViewHolder viewHolder, Broadcast broadcast, int position) {
        viewHolder.imageDisplayHolder.setVisibility(View.VISIBLE);
        viewHolder.imageDisplay.setVisibility(View.VISIBLE);

        //setting imageview
        Glide.with((Activity) context)
                .load(broadcast.getAttachmentURI())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.imageLoadProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.imageLoadProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(viewHolder.imageDisplay);

        //navigate to full screen photo display when clicked
        viewHolder.imageDisplay.setOnClickListener(view -> {
            goToFullPageImageView(position,broadcast);
        });

    }

    public void ifPollExistsAction(ViewHolder viewHolder, Broadcast broadcast, User user) {
        final Poll poll = broadcast.getPoll();
        viewHolder.pollDisplay.setVisibility(View.VISIBLE);
        viewHolder.viewPollAnswers.setVisibility(View.VISIBLE);
        viewHolder.viewPollResultsImage.setVisibility(View.VISIBLE);
        viewHolder.broadcastTitle.setText(poll.getQuestion());
        HashMap<String, Integer> pollOptions = poll.getOptions();

        //Option Percentage Calculation
        int totalValue = 0;
        for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {
            totalValue += entry.getValue();
        }

        //clear option display each time to avoid repeating options
        if (viewHolder.pollOptionsDisplayGroup.getChildCount() > 0)
            viewHolder.pollOptionsDisplayGroup.removeAllViews();

        if (poll.getUserResponse() != null && poll.getUserResponse().containsKey(user.getUserId()))
            viewHolder.setCurrentUserPollOption(poll.getUserResponse().get(user.getUserId()));

        for (Map.Entry<String, Integer> entry : pollOptions.entrySet()) {

            int percentage = 0;
            if (totalValue != 0)
                percentage = (int) (((double) entry.getValue() / totalValue) * 100);

            RadioButton button = HelperMethodsUI.generateRadioButton(context, entry.getKey(), percentage);
            LinearLayout layout = HelperMethodsUI.generateLayoutPollOptionBackground(context, button, percentage);

            if (viewHolder.currentUserPollOption != null && viewHolder.currentUserPollOption.equals(button.getText()))
                button.setChecked(true);

            button.setOnClickListener(view -> {
                HelperMethodsUI.vibrate(context);
                Bundle params1 = new Bundle();
                params1.putString("PollInteracted", "Radio button");

                String option = button.getText().toString();
                broadcastListViewModel.updatePollAnswer(option, viewHolder, broadcast, poll, circle, user);
            });
            viewHolder.pollOptionsDisplayGroup.addView(layout);
            button.setPressed(true);
        }
    }

    private void goToFullPageImageView(int position, Broadcast broadcast){
        Intent intent = new Intent(context, FullPageImageDisplay.class);
        intent.putExtra("uri", broadcast.getAttachmentURI());
        intent.putExtra("indexOfBroadcast", position);
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    public void showDeleteBroadcastDialog(Broadcast broadcast) {
        deleteBroadcastConfirmation.setContentView(R.layout.delete_broadcast_popup);
        final Button closeDialogButton = deleteBroadcastConfirmation.findViewById(R.id.delete_broadcast_confirm_btn);
        final Button cancel = deleteBroadcastConfirmation.findViewById(R.id.delete_broadcast_cancel_btn);

        closeDialogButton.setOnClickListener(view -> {
            broadcastListViewModel.deleteBroadcast(circle.getId(), broadcast, circle.getNoOfBroadcasts(), user);
            deleteBroadcastConfirmation.dismiss();
            Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show();
        });

        cancel.setOnClickListener(view -> deleteBroadcastConfirmation.dismiss());

        deleteBroadcastConfirmation.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteBroadcastConfirmation.show();
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    //initializes the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView broadcastNameDisplay, broadcastMessageDisplay, timeElapsedDisplay, viewComments, broadcastTitle, newCommentsTopTv, readMoreTextView, readLessTextView, docText;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, newCommentsTopNotifContainer;
        private RelativeLayout container;
        private ScrollView pollDisplay;
        private String currentUserPollOption = null;
        private Button viewPollAnswers;
        private PhotoView imageDisplay;
        private RelativeLayout imageDisplayHolder;
        private ProgressBar imageLoadProgressBar;
        private ImageButton broadcastListenerToggle;
        private CircleImageView viewPollResultsImage;

        public ViewHolder(View view) {
            super(view);
            broadcastNameDisplay = view.findViewById(R.id.broadcastWall_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.broadcastWall_Message);
            timeElapsedDisplay = view.findViewById(R.id.broadcastWall_object_postedTime);
            pollOptionsDisplayGroup = view.findViewById(R.id.poll_options_radio_group);
            profPicDisplay = view.findViewById(R.id.broadcasttWall_profilePicture);
            pollDisplay = view.findViewById(R.id.broadcastWall_poll_display_view);
            viewComments = view.findViewById(R.id.broadcastWall_object_viewComments);
            viewPollAnswers = view.findViewById(R.id.view_poll_answers);
            viewPollResultsImage = view.findViewById(R.id.view_poll_results_image);
            broadcastTitle = view.findViewById(R.id.broadcastWall_Title);
            container = view.findViewById(R.id.broadcast_display_container);
            imageDisplay = view.findViewById(R.id.uploaded_image_display_broadcast);
            imageDisplayHolder = view.findViewById(R.id.image_display_holder);
            imageLoadProgressBar = view.findViewById(R.id.image_progress);
            newCommentsTopNotifContainer = view.findViewById(R.id.broadcast_adapter_comments_alert_display);
            newCommentsTopTv = view.findViewById(R.id.broadcast_adapter_no_of_comments_display);
            broadcastListenerToggle = view.findViewById(R.id.broadcast_listener_on_off_toggle);
            readMoreTextView = view.findViewById(R.id.read_more_message);
            readLessTextView = view.findViewById(R.id.read_less_message);
            docText = view.findViewById(R.id.uploaded_image_text_broadcast);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }

}