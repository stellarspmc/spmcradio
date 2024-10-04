package fun.spmc.radio.music;

public enum MusicType {
    DEFAULT("https://www.youtube.com/playlist?list=PL7AMJQQdFhwYlRIt-SmDsIVZ_xpJYWvgC"),
    NICK("https://www.youtube.com/playlist?list=PL7AMJQQdFhwYLhwgHJPlnI53m9j297DSD"),
    JAKE("https://www.youtube.com/playlist?list=PL7AMJQQdFhwbOjZFzretFFciOfcOMdP9O"),
    PUBLIC("https://www.youtube.com/playlist?list=PL7AMJQQdFhwas09hzDRFI9oHqjPpUM4CN"),
    SHARK(" https://www.youtube.com/playlist?list=PLd-uSADIv6DDKdFy6-3PyRc2CBgUnIn1r");

    final String url;
    MusicType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
