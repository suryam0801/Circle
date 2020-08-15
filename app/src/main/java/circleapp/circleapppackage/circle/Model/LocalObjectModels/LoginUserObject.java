package circleapp.circleapppackage.circle.Model.LocalObjectModels;

public class LoginUserObject {
    private String completePhoneNumber,uid;

    public LoginUserObject() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCompletePhoneNumber() {
        return completePhoneNumber;
    }

    public void setCompletePhoneNumber(String completePhoneNumber) {
        this.completePhoneNumber = completePhoneNumber;
    }
    @Override
    public String toString() {
        return "LoginUserObject{" +
                "uid ='" + uid + '\'' +
                ", completePhoneNumber='" + completePhoneNumber + '\'' +
                '}';
    }
}
