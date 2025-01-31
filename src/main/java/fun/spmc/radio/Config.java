package fun.spmc.radio;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;

public class Config {
    public static String BOT_TOKEN;
    public static String MUSIC_CHANNEL_ID;
    public static String GUILD_ID;
    public static String REFRESH_TOKEN;

    // ran when start
    public static void init() {
        Configurations configs = new Configurations();
        File configFile = new File("config/config.properties");
        try {
            if (!configFile.exists()) configFile.createNewFile();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(configFile);
            Configuration config = builder.getConfiguration();

            BOT_TOKEN = config.getString("token");
            MUSIC_CHANNEL_ID = config.getString("music.channel.id");
            GUILD_ID = config.getString("guild.id");
            REFRESH_TOKEN = config.getString("refresh.token");
            if (BOT_TOKEN == null || MUSIC_CHANNEL_ID == null || GUILD_ID == null) {
                config.addProperty("token", "");
                config.addProperty("music.channel.id", "");
                config.addProperty("guild.id", "");
                config.addProperty("refresh.token", "");
                builder.save();
                throw new RuntimeException("The bot token and music channel id are not set.");
            }
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
