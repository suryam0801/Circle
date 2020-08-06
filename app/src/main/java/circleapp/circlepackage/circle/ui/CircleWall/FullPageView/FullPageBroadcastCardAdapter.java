package circleapp.circlepackage.circle.ui.CircleWall.FullPageView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import circleapp.circlepackage.circle.Helpers.HelperMethodsUI;
import circleapp.circlepackage.circle.Model.ObjectModels.Broadcast;
import circleapp.circlepackage.circle.Model.ObjectModels.Circle;
import circleapp.circlepackage.circle.Model.ObjectModels.Comment;
import circleapp.circlepackage.circle.Model.ObjectModels.Poll;
import circleapp.circlepackage.circle.Model.ObjectModels.User;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.ViewModels.CircleWall.FullpageAdapterViewModel;
import circleapp.circlepackage.circle.ViewModels.FBDatabaseReads.CommentsViewModel;
import circleapp.circlepackage.circle.ui.CircleWall.FullPageImageDisplay;
import circleapp.circlepackage.circle.ui.CircleWall.PollResults.CreatorPollAnswersView;
import de.hdodenhof.circleimageview.CircleImageView;

public class FullPageBroadcastCardAdapter extends RecyclerView.Adapter<FullPageBroadcastCardAdapter.ViewHolder> {
    private Context mContext;
    private List<Broadcast> broadcastList;
    private Circle circle;
    private int initialIndex;
    private InputMethodManager imm;
    private GlobalVariables globalVariables = new GlobalVariables();
    private FullpageAdapterViewModel fullpageAdapterViewModel = new FullpageAdapterViewModel();

    public FullPageBroadcastCardAdapter(Context mContext, List<Broadcast> broadcastList, Circle circle, int initialIndex) {
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
        Broadcast currentBroadcast = broadcastList.get(position);
        User user = globalVariables.getCurrentUser();
        //Init muted button
        final boolean broadcastMuted = user.getMutedBroadcasts() != null && user.getMutedBroadcasts().contains(currentBroadcast.getId());
        if (broadcastMuted) {
            holder.notificationToggle.setBackground(mContext.getResources().getDrawable(R.drawable.ic_outline_broadcast_not_listening_icon));
        }
        setButtonListeners(holder, currentBroadcast, user, position);
        setCreatorProfilePic(holder, mContext, currentBroadcast);
        setBroadcastInfo(mContext, holder, currentBroadcast, user);
        setComments(holder, position, currentBroadcast, user);
        fullpageAdapterViewModel.updateUserAfterReadingComments(circle, currentBroadcast, user, "view");

    }

    private void setComments(ViewHolder holder, int position, Broadcast currentBroadcast, User user){
        CommentAdapter commentAdapter;
        List<Comment> commentsList = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, true);
        holder.commentListView.setLayoutManager(layoutManager);

        commentAdapter = new CommentAdapter(mContext, commentsList, currentBroadcast);
        holder.commentListView.setAdapter(commentAdapter);

        CommentsViewModel viewModel = ViewModelProviders.of((FragmentActivity) mContext).get(CommentsViewModel.class);
        LiveData<String[]> liveData = viewModel.getDataSnapsCommentsLiveData(circle.getId(), currentBroadcast.getId());

        liveData.observe((LifecycleOwner) mContext, returnArray -> {
            Comment tempComment = new Gson().fromJson(returnArray[0], Comment.class);
            commentsList.add(0, tempComment); //to store timestamp values descendingly
            commentAdapter.notifyItemInserted(0);
            holder.commentListView.smoothScrollToPosition(0);

            if (position == initialIndex) {
                fullpageAdapterViewModel.updateUserAfterReadingComments(circle, currentBroadcast, user, "view");
            }
        });
    }

    private void setButtonListeners(ViewHolder holder, Broadcast currentBroadcast, User user, int position){

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

        holder.addCommentButton.setOnClickListener(view -> {
            String commentMessage = holder.addCommentEditText.getText().toString().trim();
            if (!commentMessage.equals("")) {
                fullpageAdapterViewModel.makeCommentEntry(mContext, commentMessage, currentBroadcast, user, circle);
            }
            holder.addCommentEditText.setText("");
        });

        holder.notificationToggle.setOnClickListener(view -> {
            updateMutedStatus(currentBroadcast, holder);
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

        if (broadcast.isPollExists() == true) {
            actionsIfPollExists(viewHolder, broadcast, context, user);
        }
    }

    private void setBroadcastUI(ViewHolder viewHolder, Broadcast broadcast, Context context){

        viewHolder.broadcastTitle.setText(broadcast.getTitle());

        //set the details of each circle to its respective card.
        viewHolder.broadcastNameDisplay.setText(broadcast.getCreatorName());

        if (broadcast.getMessage() == null)
            viewHolder.broadcastMessageDisplay.setVisibility(View.GONE);
        else
            viewHolder.broadcastMessageDisplay.setText(broadcast.getMessage());

        viewHolder.viewPollAnswers.setOnClickListener(view -> {
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

    private void actionsIfPollExists(ViewHolder viewHolder, Broadcast broadcast, Context context, User user) {
        final Poll poll;
        poll = broadcast.getPoll();
        viewHolder.pollOptionsDisplayGroup.setVisibility(View.VISIBLE);
        viewHolder.viewPollAnswers.setVisibility(View.VISIBLE);
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
        private TextView broadcastNameDisplay, broadcastMessageDisplay, broadcastTitle;
        private CircleImageView profPicDisplay;
        private LinearLayout pollOptionsDisplayGroup, newNotifsContainer;
        private ImageButton notificationToggle;
        private String currentUserPollOption = null;
        private Button viewPollAnswers, addCommentButton;
        private EditText addCommentEditText;
        private PhotoView imageView;
        private Button viewPostButton, hidePostButton;
        private ImageView viewPostImage, hidePostImage;

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
            broadcastTitle = view.findViewById(R.id.full_page_broadcast_Title);
            imageView = view.findViewById(R.id.uploaded_image_display_broadcast_full_page);
            addCommentButton = view.findViewById(R.id.full_page_broadcast_comment_send_button);
            addCommentEditText = view.findViewById(R.id.full_page_broadcast_comment_type_editText);
            notificationToggle = view.findViewById(R.id.full_page_broadcast_listener_on_off_toggle);
            viewPostButton = view.findViewById(R.id.view_post_button);
            viewPostImage = view.findViewById(R.id.view_image);
            hidePostButton = view.findViewById(R.id.hide_post_button);
            hidePostImage = view.findViewById(R.id.hide_image);
        }

        public String getCurrentUserPollOption() {
            return currentUserPollOption;
        }

        public void setCurrentUserPollOption(String currentUserPollOption) {
            this.currentUserPollOption = currentUserPollOption;
        }
    }
}
