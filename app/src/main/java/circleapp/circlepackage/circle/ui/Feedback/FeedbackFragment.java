package circleapp.circlepackage.circle.ui.Feedback;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import circleapp.circlepackage.circle.FirebaseHelpers.FirebaseWriteHelper;
import circleapp.circlepackage.circle.Utils.GlobalVariables;
import circleapp.circlepackage.circle.data.ObjectModels.User;
import circleapp.circlepackage.circle.R;

public class FeedbackFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private EditText feedbackEditText;
    private Button feedbackSend;
    private GlobalVariables globalVariables = new GlobalVariables();

    public FeedbackFragment() {
    }

    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        feedbackEditText = view.findViewById(R.id.feedback_type_editText);
        feedbackSend = view.findViewById(R.id.feedback_send_button);

        feedbackEditText.clearFocus();

        feedbackSend.setOnClickListener(v -> {
            if (!feedbackEditText.getText().toString().trim().equals(""))
                makeFeedbackEntry(view);
            feedbackEditText.setText("");
        });
        return view;
    }

    private void makeFeedbackEntry(View view) {
        long currentCommentTimeStamp = System.currentTimeMillis();

        User user = globalVariables.getCurrentUser();

        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", currentCommentTimeStamp);
        map.put("feedback", feedbackEditText.getText().toString().trim());
        map.put("userId", user.getUserId());
        map.put("userName", user.getName().trim());

        FirebaseWriteHelper.makeFeedbackEntry(map);

        Toast.makeText(view.getContext(), "Thanks for your feedback :)", Toast.LENGTH_SHORT).show();
    }
}
