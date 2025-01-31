package fun.spmc.radio.features;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SPMCWrapped {
    private static final Logger log = LoggerFactory.getLogger(SPMCWrapped.class);
    private static File wrappedFile;
    private static JSONObject file = new JSONObject();

    public static void init() {
        wrappedFile = new File("wrapped.json");
        try {
            if (!wrappedFile.exists()) wrappedFile.createNewFile();
            else {
                Object obj = new JSONParser().parse(new FileReader(wrappedFile));
                file = (JSONObject) obj;
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addUserData(User user, AudioTrack track) {
        HashMap<String, Long> userSongList = new HashMap<>(fetchUserSongList(user));
        long duration = track.getPosition();
        if (duration == 0) return;
        if (user.isBot()) return;

        if (userSongList.containsKey(track.getInfo().identifier)) userSongList.put(track.getInfo().identifier, userSongList.get(track.getInfo().identifier) + duration);
        else userSongList.put(track.getInfo().identifier, duration);
        file.put(user.getId(), userSongList);
    }

    public static long fetchPlaytime(User user) {
        HashMap<String, Long> userSongList = fetchUserSongList(user);
        AtomicLong total = new AtomicLong();
        userSongList.forEach((a, b) -> total.addAndGet(b));
        return total.get();
    }

    private static HashMap<String, Long> fetchUserSongList(User user) {
        return (HashMap<String, Long>) file.getOrDefault(user.getId(), new HashMap<String, Long>());
    }

    public static void save() {
        try {
            FileWriter myWriter = new FileWriter(wrappedFile);
            myWriter.write(file.toString());
            myWriter.close();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public static List<Map.Entry<String, Long>> topFiveSongs(User user) {
        HashMap<String, Long> userSongList = new HashMap<>(fetchUserSongList(user));
        ArrayList<Long> list = new ArrayList<>();

        for (Map.Entry<String, Long> entry : userSongList.entrySet()) {
            list.add(entry.getValue());
        }

        list.sort(Collections.reverseOrder());
        List<Map.Entry<String, Long>> sortedArray = new ArrayList<>();

        for (long str : list) {
            for (Map.Entry<String, Long> entry : userSongList.entrySet()) {
                if (entry.getValue().equals(str)) sortedArray.add(entry);
            }
        }

        if (sortedArray.size() > 5) sortedArray = sortedArray.subList(0, 5);
        return sortedArray;
    }
}
