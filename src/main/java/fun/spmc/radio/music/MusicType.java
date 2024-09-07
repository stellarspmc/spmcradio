package fun.spmc.radio.music;

public enum MusicType {
    DEFAULT("https://www.youtube.com/playlist?list=PL7AMJQQdFhwYlRIt-SmDsIVZ_xpJYWvgC"),
    NICKWONG("https://www.youtube.com/playlist?list=PL7AMJQQdFhwYLhwgHJPlnI53m9j297DSD"),
    JAKEWONG("https://www.youtube.com/playlist?list=PL7AMJQQdFhwbOjZFzretFFciOfcOMdP9O"),
    PUBLIC("https://www.youtube.com/playlist?list=PL7AMJQQdFhwas09hzDRFI9oHqjPpUM4CN");

    final String url;
    MusicType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
