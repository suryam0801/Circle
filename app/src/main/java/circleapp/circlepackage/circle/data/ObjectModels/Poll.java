package circleapp.circlepackage.circle.data.ObjectModels;

import java.util.HashMap;

public class Poll {
    String question;
    HashMap<String, String> userResponse;
    HashMap<String, Integer> options;

    public Poll(String question, HashMap<String, Integer> options, HashMap<String, String> userResponse) {
        this.question = question;
        this.options = options;
        this.userResponse = userResponse;
    }

    public Poll() {
    }


    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public HashMap<String, Integer> getOptions() {
        return options;
    }

    public void setOptions(HashMap<String, Integer> options) {
        this.options = options;
    }

    public HashMap<String, String> getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(HashMap<String, String> userResponse) {
        this.userResponse = userResponse;
    }

    @Override
    public String toString() {
        return "Poll{" +
                ", question='" + question + '\'' +
                ", options=" + options +
                ", userResponse=" + userResponse +
                '}';
    }
}
