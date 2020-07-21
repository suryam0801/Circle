package circleapp.circlepackage.circle.data.LocalObjectModels;

public class LoginUserObject {
    private String ward, district,countryDialCode,countryName, completePhoneNumber,uid;

    public LoginUserObject() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCountryDialCode() {
        return countryDialCode;
    }

    public void setCountryDialCode(String countryDialCode) {
        this.countryDialCode = countryDialCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
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
                ", countryName ='" + countryName + '\'' +
                ", countryDialCode='" + countryDialCode + '\'' +
                ", district='" + district + '\'' +
                ", ward=" + ward  +
                ", completePhoneNumber='" + completePhoneNumber + '\'' +
                '}';
    }
}
