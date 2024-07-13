package ml.spmc.radio;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    public static String BOT_TOKEN;
    public static String MUSIC_CHANNEL_ID;
    public static String GUILD_ID;


    // ran when start
    public static void checkConfigs() {
        try {
            Path path = Paths.get(new File(SPMCRadio.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getPath()).getParent().resolve("config.properties");
            if (!Files.exists(path)) {
                Files.createFile(path);

                FileWriter myWriter = new FileWriter(path.toFile());
                myWriter.write(
                        "token=null\n" +
                                "guild_id=null\n" +
                                "music_channel_id=null");
                myWriter.close();
            }

            FileInputStream propsInput = new FileInputStream(path.toFile());
            Properties prop = new Properties();
            prop.load(propsInput);

            BOT_TOKEN = prop.getProperty("token");
            MUSIC_CHANNEL_ID = prop.getProperty("music_channel_id");
            GUILD_ID = prop.getProperty("guild_id");
        } catch (IOException e) {
            System.err.println("Fill in the config!");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
