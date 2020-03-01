package throwlink.appsters.sukalp.nativeplayer;

public class Video {

    String title;
    String url;
    String thumb;


    public Video()
    {

    }
    public Video(String title, String url,String thumb) {
        this.title = title;
        this.url = url;
        this.thumb= thumb;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
