package fun.spmc.radio.features;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SongToTitleCacher {
    private static final Logger log = LoggerFactory.getLogger(SongToTitleCacher.class);
    private static File cache;
    private static JSONObject file = new JSONObject();

    public static void init() {
        cache = new File("config/song_title.json");
        try {
            if (!cache.exists()) cache.createNewFile();
            else {
                Object obj = new JSONParser().parse(new FileReader(cache));
                file = (JSONObject) obj;
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addSongData(AudioTrack track) {
        if (!containsSongData(track.getInfo().identifier)) {
            file.put(track.getInfo().identifier, track.getInfo().title);
            save();
        }
    }

    public static boolean containsSongData(String identifier) {
        return file.containsKey(identifier);
    }

    public static String fetchSongData(String identifier) {
        return (String) file.getOrDefault(identifier, "");
    }

    public static void save() {
        try {
            FileWriter myWriter = new FileWriter(cache);
            myWriter.write(file.toString());
            myWriter.close();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
