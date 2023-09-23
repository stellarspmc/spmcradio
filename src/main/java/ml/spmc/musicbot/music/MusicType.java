package ml.spmc.musicbot.music;

public enum MusicType {
    SMP("https://youtube.com/playlist?list=PLy_S3qOMUL1epiuCU4kBTOpLo1xOFJSLx"),
    NCS("https://youtube.com/playlist?list=PL7AMJQQdFhwa9W7PKH5u25BovEfeM_i75"),
    MINECRAFT("https://www.youtube.com/playlist?list=PLefKpFQ8Pvy5aCLAGHD8Zmzsdljos-t2l"),
    PHONK("https://youtube.com/playlist?list=PL7AMJQQdFhwbeb4dYlJeg3bPjKCb1IQAq"),
    BF4("https://www.youtube.com/playlist?list=PLrLdOpn32nJfc8pqKQyHSaO_nzva2F5eI"),
    PVZ("https://www.youtube.com/playlist?list=PL8C3D47E6FA9CDDC4"),
    LOFI("https://www.youtube.com/playlist?list=PLy_S3qOMUL1fMOProobYMiSflQaoSUD4x"),
    THEFATRAT("https://www.youtube.com/playlist?list=PL37UZ2QfPUvyeqqNi4m_byAjAbSHBIosW");

    final String url;
    MusicType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
