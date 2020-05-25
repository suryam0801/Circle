package circleapp.circlepackage.circle.ObjectModels;

public class Subscriber {
    String id, name, photoURI, token_id;

    public Subscriber(String id, String name, String photoURI, String token_id) {
        this.id = id;
        this.name = name;
        this.photoURI = photoURI;
        this.token_id = token_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoURI() {
        return photoURI;
    }

    public void setPhotoURI(String photoURI) {
        this.photoURI = photoURI;
    }

    public String getToken_id() {
        return token_id;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    @Override
    public String toString() {
        return "Subscriber{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", photoURI='" + photoURI + '\'' +
                ", token_id='" + token_id + '\'' +
                '}';
    }
}
