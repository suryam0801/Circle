package circleapp.circlepackage.circle.Model.ObjectModels;

public class Contacts {
    private String phn_number;
    private String uid;
    public Contacts(){

    }
    public Contacts(String phn_number, String uid) {
        this.phn_number = phn_number;
        this.uid = uid;
    }

    public String getPhn_number() {
        return phn_number;
    }

    public void setPhn_number(String phn_number) {
        this.phn_number = phn_number;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    @Override
    public String toString() {
        return "Contacts{" +
                "Uid='" + uid + '\'' +
                ", contact='" + phn_number +
                '}';
    }
}
