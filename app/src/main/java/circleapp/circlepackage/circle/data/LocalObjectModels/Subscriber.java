package circleapp.circlepackage.circle.data.LocalObjectModels;

import circleapp.circlepackage.circle.data.ObjectModels.User;

public class Subscriber {
    String id, name, photoURI, token_id;
    long timestamp;

    public Subscriber(User user, long timestamp) {
        this.id = user.getUserId();
        this.name = user.getName();
        this.photoURI = user.getProfileImageLink();
        this.token_id = user.getToken_id();
        this.timestamp = timestamp;
    }

    public Subscriber(){}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
                ", timestamp=" + timestamp +
                '}';
    }
}
