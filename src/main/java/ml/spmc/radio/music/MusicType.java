package ml.spmc.radio.music;

public enum MusicType {
    DEFAULT("https://youtube.com/playlist?list=PLy_S3qOMUL1epiuCU4kBTOpLo1xOFJSLx"), // 2022
    PHONK("https://youtube.com/playlist?list=PL7AMJQQdFhwbeb4dYlJeg3bPjKCb1IQAq"), // 2023
    NEWMIX("https://www.youtube.com/playlist?list=PL7AMJQQdFhwYlRIt-SmDsIVZ_xpJYWvgC"); // 2024

    final String url;
    MusicType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
