package dk.incipio.photogallery;


public class GalleryItem {
    private String mCaption;
    private String mId;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    private String mUrl;

    public String toString() {
        return mCaption;
    }
}
