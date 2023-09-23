package ml.spmc.musicbot.music;

public enum MusicType {
    NCS("https://youtube.com/playlist?list=PL7AMJQQdFhwa9W7PKH5u25BovEfeM_i75"),
    SMP("https://youtube.com/playlist?list=PLy_S3qOMUL1epiuCU4kBTOpLo1xOFJSLx"),
    MINECRAFT("https://www.youtube.com/playlist?list=PLefKpFQ8Pvy5aCLAGHD8Zmzsdljos-t2l"),
    PHONK("https://youtube.com/playlist?list=PL7AMJQQdFhwbeb4dYlJeg3bPjKCb1IQAq");

    String url;
    MusicType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
