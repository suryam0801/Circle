package circleapp.circleapppackage.circle.ui.CircleWall.FullPageView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circleapppackage.circle.Helpers.HelperMethodsBL;
import circleapp.circleapppackage.circle.Helpers.HelperMethodsUI;
import circleapp.circleapppackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circleapppackage.circle.Model.ObjectModels.Circle;
import circleapp.circleapppackage.circle.Model.ObjectModels.Comment;
import circleapp.circleapppackage.circle.Model.ObjectModels.Poll;
import circleapp.circleapppackage.circle.Model.ObjectModels.Subscriber;
import circleapp.circleapppackage.circle.Model.ObjectModels.User;
import circleapp.circleapppackage.circle.R;
import circleapp.circleapppackage.circle.Utils.GlobalVariables;
import circleapp.circleapppackage.circle.Utils.UploadImages.ImagePicker;
import circleapp.circleapppackage.circle.ViewModels.CircleWall.FullpageAdapterViewModel;
import circleapp.circleapppackage.circle.ViewModels.CircleWall.ImageUrlViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.CirclePersonnelViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.CommentsViewModel;
import circleapp.circleapppackage.circle.ViewModels.FBDatabaseReads.MyCirclesViewModel;
import circleapp.circleapppackage.circle.ui.CircleWall.BroadcastListView.BroadcastListAdapter;
import circleapp.circleapppackage.circle.ui.CircleWall.FullPageImageDisplay;
import circleapp.circleapppackage.circle.ui.CircleWall.PollResults.CreatorPollAnswersView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.content.Context.DOWNLOAD_SERVICE;

public class FullPageBroadcastCardAdapter extends RecyclerView.Adapter<FullPageBroadcastCardAdapter.ViewHolder>{
    private Activity mContext;
    private List<Broadcast> broadcastList;
    private Circle circle;
    private User user;
    private Broadcast currentBroadcast;
    private List<Subscriber> listOfMembers;
    private int initialIndex;
    private InputMethodManager imm;
    private GlobalVariables globalVariables = new GlobalVariables();
    private FullpageAdapterViewModel fullpageAdapterViewModel = new FullpageAdapterViewModel();
    private RecyclerView.LayoutManager layoutManager;
    private int itemPos = 0;
    private static final int TOTAL_ITEMS_TO_LOAD = 50;
    private int mCurrentPage = 1;
    private String mLastKey = "";
    private String mPrevKey = "";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LiveData<String []> loadMoreLiveData, initLiveData;
    private boolean isDownloadLinkActive = false;
    private ImageUrlViewModel  imageUrlViewModel = new ImageUrlViewModel();

    public FullPageBroadcastCardAdapter(Activity mContext, List<Broadcast> broadcastList, Circle circle, int initialIndex) {
        this.mContext = mContext;
        this.broadcastList = broadcastList;
        this.circle = circle;
        this.initialIndex = initialIndex;
        imm = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);
    }

    @NonNull
    @Override
    public FullPageBroadcastCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.broadcast_full_page_card_item, parent, false);
        return new FullPageBroadcastCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FullPageBroadcastCardAdapter.ViewHolder holder, int position) {
        ((Activity) mContext).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        //Init UI Elements

        currentBroadcast = broadcastList.get(position);
        user = globalVariables.getCurrentUser();
        setCircleObserver();
        setCircleMembersObserver();
        setImageActiveListener(holder);
        //Init muted button
        final boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(currentBroadcast.getId());
        if (broadcastMuted) {
            holder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
        }
        setButtonListeners(holder, currentBroadcast, position);
        setCreatorProfilePic(holder, mContext, currentBroadcast);
        setBroadcastInfo(mContext, holder, currentBroadcast, globalVariables.getCurrentUser());
        setComments(holder, currentBroadcast, position);
        fullpageAdapterViewModel.updateUserAfterReadingComments(circle, currentBroadcast, user, "view");
    }

    private void setImageActiveListener(ViewHolder holder){
        imageUrlViewModel = ViewModelProviders.of((FragmentActivity) mContext).get(ImageUrlViewModel.class);
        imageUrlViewModel.listenForLocationUpdates(isDownloadLinkActive).observe((LifecycleOwner) mContext, imageUpdates -> {
            if(imageUpdates&&globalVariables.getCommentDownloadLink()!=null)
            {
                String commentMessage = globalVariables.getCommentDownloadLink().toString();
                if (!commentMessage.equals("")) {
                    fullpageAdapterViewModel.makeCommentEntry(mContext, commentMessage, currentBroadcast, globalVariables.getCurrentUser(), circle, listOfMembers);
                }
                holder.addCommentEditText.setText("");
                globalVariables.setCommentDownloadLink(null);
            }
        });
    }

    private void setCircleObserver(){
        circle = globalVariables.getCurrentCircle();
        MyCirclesViewModel tempViewModel = ViewModelProviders.of((FragmentActivity) mContext).get(MyCirclesViewModel.class);
        LiveData<DataSnapshot> tempLiveData = tempViewModel.getDataSnapsParticularCircleLiveData(circle.getId());
        tempLiveData.observe((LifecycleOwner) mContext, dataSnapshot -> {
            Circle circleTemp = dataSnapshot.getValue(Circle.class);
            if (circleTemp != null&&circleTemp.getNoOfCommentsPerBroadcast()!=null) {
                if(circleTemp.getNoOfCommentsPerBroadcast().get(currentBroadcast.getId())!=null)
                    updateUserReadValues(circleTemp.getNoOfCommentsPerBroadcast().get(currentBroadcast.getId()),circleTemp);
            }
        });
    }

    private void setCircleMembersObserver(){
        listOfMembers = new ArrayList<>();
        CirclePersonnelViewModel circlePersonnelViewModel = ViewModelProviders.of((FragmentActivity) mContext).get(CirclePersonnelViewModel.class);
        LiveData<String[]> liveData = circlePersonnelViewModel.getDataSnapsCirclePersonelLiveData(circle.getId(), "members");
        liveData.observe((LifecycleOwner) mContext, returnArray -> {
            Subscriber member = new Gson().fromJson(returnArray[0], Subscriber.class);
            if(member!=null){
                listOfMembers.add(member);
            }
        });
    }

    private void updateUserReadValues(int commentCount, Circle circleTemp) {
        if(user.getNoOfReadDiscussions()!=null){
            if(user.getNoOfReadDiscussions().get(currentBroadcast.getId())!=null){
                if(user.getNoOfReadDiscussions().get(currentBroadcast.getId())<commentCount)
                    globalVariables.saveCurrentCircle(circleTemp);
                    HelperMethodsBL.updateUserOnCommentRead(user,commentCount,currentBroadcast.getId());
            }
        }
    }

    private void setComments(ViewHolder holder,Broadcast currentBroadcast, int position){
        CommentAdapter commentAdapter;
        List<Comment> commentsList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        holder.commentListView.setLayoutManager(layoutManager);

        commentAdapter = new CommentAdapter(commentsList, currentBroadcast, position);
        holder.commentListView.setAdapter(commentAdapter);
        mSwipeRefreshLayout = holder.swipeRefreshLayout;
        //Load initial messages
        loadInitialComments(currentBroadcast, holder, commentAdapter, commentsList);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreComments(currentBroadcast, commentAdapter, commentsList);
                if(!mSwipeRefreshLayout.isRefreshing())
                    mSwipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void loadInitialComments(Broadcast currentBroadcast, ViewHolder holder, CommentAdapter commentAdapter, List<Comment> commentsList){

        CommentsViewModel commentsViewModel = new CommentsViewModel();
        initLiveData = commentsViewModel.getDataSnapsInitialLoadCommentsLiveData(circle.getId(), currentBroadcast.getId(), mCurrentPage, TOTAL_ITEMS_TO_LOAD);
        initLiveData.observe((LifecycleOwner) mContext, returnArray->{
            Comment comment = new Gson().fromJson(returnArray[0], Comment.class);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    addInitComments(comment,commentAdapter,commentsList,holder);
                    break;
                case "removed":
                    removeInitComment(comment,commentAdapter,commentsList);
                    break;
            }
        });
    }

    private void addInitComments(Comment comment, CommentAdapter commentAdapter, List<Comment> commentsList, ViewHolder holder){
        itemPos++;
        if (itemPos == 1) {
            String messageKey = comment.getId();
            mLastKey = messageKey;
            mPrevKey = messageKey;
        }
        commentsList.add(comment);
        commentAdapter.notifyDataSetChanged();
        assert comment != null;
        holder.commentListView.scrollToPosition(commentsList.size() - 1);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void removeInitComment(Comment comment, CommentAdapter commentAdapter, List<Comment> commentsList){
        int index = HelperMethodsUI.returnIndexOfComment(commentsList, comment);
        commentsList.remove(index);
        commentAdapter.notifyItemRemoved(index);
    }

    private void loadMoreComments(Broadcast currentBroadcast, CommentAdapter commentAdapter, List<Comment> commentsList) {

        CommentsViewModel commentsViewModel = new CommentsViewModel();
        loadMoreLiveData = commentsViewModel.getDataSnapsLoadMoreCommentsLiveData(circle.getId(), currentBroadcast.getId(), mLastKey, 100);
        loadMoreLiveData.observe((LifecycleOwner) mContext, returnArray->{
            Comment comment = new Gson().fromJson(returnArray[0], Comment.class);
            mSwipeRefreshLayout.setRefreshing(false);
            String modifierType = returnArray[1];
            switch (modifierType) {
                case "added":
                    addMoreComments(comment,commentAdapter,commentsList);
                    break;
            }
        });
    }

    private void addMoreComments(Comment comment, CommentAdapter commentAdapter, List<Comment> commentsList){
        String commentKey = comment.getId();
        assert comment != null;
        if(!mPrevKey.equals(commentKey)){

            commentsList.add(itemPos++,comment);

        } else {
            mPrevKey = mLastKey;
        }

        if(itemPos == 1) {
            mLastKey = commentKey;
        }
        commentAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void setButtonListeners(ViewHolder holder, Broadcast currentBroadcast, int position){

        if(!currentBroadcast.isFileExists()&&!currentBroadcast.isImageExists()&&!currentBroadcast.isPollExists()&&currentBroadcast.getMessage()==null)
            holder.viewPostButton.setVisibility(View.GONE);

        holder.viewPostButton.setOnClickListener(view->{
            //visibility of button
            holder.viewPostImage.setVisibility(View.GONE);
            holder.viewPostButton.setVisibility(View.GONE);
            holder.hidePostButton.setVisibility(View.VISIBLE);
            holder.hidePostImage.setVisibility(View.VISIBLE);
            holder.postContentLayout.setVisibility(View.VISIBLE);
            globalVariables.saveCurrentBroadcast(broadcastList.get(position));

        });
        holder.hidePostButton.setOnClickListener(view->{
            //visibility of button
            holder.hidePostButton.setVisibility(View.GONE);
            holder.hidePostImage.setVisibility(View.GONE);
            holder.viewPostImage.setVisibility(View.VISIBLE);
            holder.viewPostButton.setVisibility(View.VISIBLE);
            holder.postContentLayout.setVisibility(View.GONE);
        });

        //Write new comment

        holder.addCommentButton.setOnClickListener(view -> {
            String commentMessage = holder.addCommentEditText.getText().toString().trim();
            if (!commentMessage.equals("")) {
                fullpageAdapterViewModel.makeCommentEntry(mContext, commentMessage, currentBroadcast, globalVariables.getCurrentUser(), circle, listOfMembers);
            }
            holder.addCommentEditText.setText("");
        });

        holder.notificationToggle.setOnClickListener(view -> {
            updateMutedStatus(currentBroadcast, holder);
        });

        holder.imageUpload.setOnClickListener(v->{
            Permissions.check(mContext,new String[]{CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    ImagePicker imagePicker = new ImagePicker(mContext.getApplication());
                    Intent chooseImageIntent = imagePicker.getPickImageIntent();
                    mContext.startActivityForResult(chooseImageIntent, 234);
                }
                @Override
                public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                    // permission denied, block the feature.
                }
            });
        });
    }

    public void updateMutedStatus(Broadcast broadcast, ViewHolder viewHolder) {
        User user = globalVariables.getCurrentUser();
        List<String> userMutedArray;
        if (user.getMutedBroadcasts() != null)
            userMutedArray = new ArrayList<>(user.getMutedBroadcasts());
        else
            userMutedArray = new ArrayList<>();

        boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(broadcast.getId());

        if (broadcastMuted) {
            viewHolder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_listening_icon));
            userMutedArray.remove(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            fullpageAdapterViewModel.updateMutedList(user, circle.getId(), broadcast.getId(), 0);
        } else {
            viewHolder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
            userMutedArray.add(broadcast.getId());
            user.setMutedBroadcasts(userMutedArray);
            fullpageAdapterViewModel.updateMutedList(user, circle.getId(), broadcast.getId(), 1);
        }
    }

    public void setBroadcastInfo(Context context, ViewHolder viewHolder, Broadcast broadcast, User user) {

        setBroadcastUI(viewHolder, broadcast, context);
        setImageIfExists(broadcast,viewHolder,context);

        if (broadcast.isPollExists()) {
            actionsIfPollExists(viewHolder, broadcast, context, user);
        }
        if(broadcast.isFileExists())
            ifFileExistsAction(viewHolder, context, broadcast);
    }

    private void setBroadcastUI(ViewHolder viewHolder, Broadcast broadcast, Context context){

        viewHolder.broadcastTitle.setText(broadcast.getTitle());

        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        if (broadcast.getMessage() == null)
            viewHolder.broadcastMessageDisplay.setVisibility(View.GONE);
        else
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());

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

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
            globalVariables.saveCurrentBroadcast(broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });
        viewHolder.viewPollAnswersImageBtn.setOnClickListener(view -> {
            globalVariables.saveCurrentBroadcast(broadcast);
            context.startActivity(new Intent(context, CreatorPollAnswersView.class));
        });
    }

    private void setCreatorProfilePic(ViewHolder viewHolder, Context context, Broadcast broadcast){
        HelperMethodsUI.setPostIcon(broadcast,viewHolder.profPicDisplay, context);
    }

    private void setImageIfExists(Broadcast broadcast, ViewHolder viewHolder, Context context){
        if (broadcast.getAttachmentURI() != null) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
            //setting imageview
            Glide.with((Activity) context)
                    .load(broadcast.getAttachmentURI())
                    .into(viewHolder.imageView);

            //navigate to full screen photo display when clicked
            viewHolder.imageView.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullPageImageDisplay.class);
                intent.putExtra("uri", broadcast.getAttachmentURI());
                context.startActivity(intent);
                ((Activity) context).finish();
            });
        }
    }

    private void ifFileExistsAction(ViewHolder viewHolder, Context context, Broadcast broadcast){
        if (broadcast.getAttachmentURI() != null) {
            viewHolder.imageView.setVisibility(View.VISIBLE);
        }

        //setting imageview
        viewHolder.imageHelpText.setVisibility(View.VISIBLE);
        int profilePic = Integer.parseInt(String.valueOf(R.drawable.file_download));
        Glide.with((Activity) context)
                .load(ContextCompat.getDrawable(context, profilePic))
                .override(300,300)
                .into(viewHolder.imageView);

        //navigate to full screen photo display when clicked
        viewHolder.imageView.setOnClickListener(view -> {
            //TODO Open file
            if(broadcast.isPollExists())
                downloadFile(Uri.parse(broadcast.getAttachmentURI()),circle.getName()+"_"+broadcast.getPoll().getQuestion(), context);
            else
                downloadFile(Uri.parse(broadcast.getAttachmentURI()),circle.getName()+"_"+broadcast.getTitle(), context);
        });
    }

    private void downloadFile(Uri uri, String filename, Context context){
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


    private void actionsIfPollExists(ViewHolder viewHolder, Broadcast broadcast, Context context, User user) {
        final Poll poll;
        poll = broadcast.getPoll();
        viewHolder.pollOptionsDisplayGroup.setVisibility(View.VISIBLE);
        viewHolder.viewPollAnswers.setVisibility(View.VISIBLE);
        viewHolder.viewPollAnswersImageBtn.setVisibility(View.VISIBLE);
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
                fullpageAdapterViewModel.updatePollValues(viewHolder,broadcast,user,poll,option,circle);
                setBroadcastInfo(context, viewHolder, broadcast, user);
            });
            viewHolder.pollOptionsDisplayGroup.addView(layout);
            button.setPressed(true);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return broadcastList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerView commentListView;
        private RelativeLayout broadcst_container;
        private LinearLayout postContentLayout;
        private TextView broadcastNameDisplay, broadcastMessageDisplay, broadcastTitle, imageHelpText;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, newNotifsContainer;
        private CircleImageView viewPollAnswersImageBtn;
        private ImageButton notificationToggle, imageUpload;
        private String currentUserPollOption = null;
        private Button viewPollAnswers, addCommentButton;
        private EditText addCommentEditText;
        private PhotoView imageView;
        private Button viewPostButton, hidePostButton;
        private ImageView viewPostImage, hidePostImage;
        private SwipeRefreshLayout swipeRefreshLayout;

        public ViewHolder(View view) {
            super(view);
            commentListView = view.findViewById(R.id.full_page_broadcast_comments_display);
            broadcst_container = view.findViewById(R.id.full_page_broadcast_container);
            postContentLayout = view.findViewById(R.id.post_content_layout);
            broadcastNameDisplay = view.findViewById(R.id.full_page_broadcast_ownerName);
            broadcastMessageDisplay = view.findViewById(R.id.full_page_broadcast_Message);
            profPicDisplay = view.findViewById(R.id.full_page_broadcast_profilePicture);
            pollOptionsDisplayGroup = view.findViewById(R.id.full_page_broadcast_poll_options_radio_group);
            viewPollAnswers = view.findViewById(R.id.full_page_broadcast_view_poll_answers);
            viewPollAnswersImageBtn = view.findViewById(R.id.full_page_broadcast_view_poll_results_image);
            broadcastTitle = view.findViewById(R.id.full_page_broadcast_Title);
            imageView = view.findViewById(R.id.uploaded_image_display_broadcast_full_page);
            addCommentButton = view.findViewById(R.id.full_page_broadcast_comment_send_button);
            addCommentEditText = view.findViewById(R.id.full_page_broadcast_comment_type_editText);
            notificationToggle = view.findViewById(R.id.full_page_broadcast_listener_on_off_toggle);
            viewPostButton = view.findViewById(R.id.view_post_button);
            viewPostImage = view.findViewById(R.id.view_image);
            hidePostButton = view.findViewById(R.id.hide_post_button);
            hidePostImage = view.findViewById(R.id.hide_image);
            swipeRefreshLayout = view.findViewById(R.id.message_swipe_layout);
            imageUpload = view.findViewById(R.id.comment_image_upload);
            imageHelpText = view.findViewById(R.id.uploaded_image_text_broadcast_full_page);

        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
