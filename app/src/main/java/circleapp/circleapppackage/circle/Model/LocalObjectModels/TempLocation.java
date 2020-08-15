package circleapp.circleapppackage.circle.Model.LocalObjectModels;

public class TempLocation {
    public TempLocation() {
    }

    private String ward, district,countryDialCode,countryName;

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
    @Override
    public String toString() {
        return "TempLocation{" +
                "countryName ='" + countryName + '\'' +
                ", countryDialCode='" + countryDialCode + '\'' +
                ", district='" + district + '\'' +
                ", ward=" + ward  + '\'' +
                '}';
    }
}
