package circleapp.circlepackage.circle.ObjectModels;

import java.util.List;

public class Poll {
    String id, question;
    List<String> options, userResponse;

    public Poll(String id, String question, List<String> options, List<String> userResponse) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.userResponse = userResponse;
    }

    public Poll() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<String> getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(List<String> userResponse) {
        this.userResponse = userResponse;
    }

    @Override
    public String toString() {
        return "Poll{" +
                "id='" + id + '\'' +
                ", question='" + question + '\'' +
                ", options=" + options +
                ", userResponse=" + userResponse +
                '}';
    }
}
