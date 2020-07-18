package circleapp.circlepackage.circle.data.LocalObjectModels;

public class LoginUserObject {
    private int position;
    private String ward, district,countryDialCode,countryName, completePhoneNumber;

    public LoginUserObject() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
                "position=" + position  +
                ", countryName ='" + countryName + '\'' +
                ", countryDialCode='" + countryDialCode + '\'' +
                ", district='" + district + '\'' +
                ", ward=" + ward  +
                ", completePhoneNumber='" + completePhoneNumber + '\'' +
                '}';
    }
}
