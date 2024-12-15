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
    public static String PO_TOKEN;
    public static String VISITOR_DATA;

    // ran when start
    public static void checkConfigs() {
        Configurations configs = new Configurations();
        File configFile = new File("config.properties");
        try {
            if (!configFile.exists()) configFile.createNewFile();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = configs.propertiesBuilder(configFile);
            Configuration config = builder.getConfiguration();

            BOT_TOKEN = config.getString("token");
            MUSIC_CHANNEL_ID = config.getString("music.channel.id");
            GUILD_ID = config.getString("guild.id");
            PO_TOKEN = config.getString("po.token");
            VISITOR_DATA = config.getString("visitor.data");
            if (BOT_TOKEN == null || MUSIC_CHANNEL_ID == null || GUILD_ID == null) {
                config.addProperty("token", "");
                config.addProperty("music.channel.id", "");
                config.addProperty("guild.id", "");
                builder.save();
                throw new RuntimeException("The bot token and music channel id are not set.");
            }

            if (PO_TOKEN == null || VISITOR_DATA == null) {
                config.addProperty("po.token", "");
                config.addProperty("visitor.data", "");
                builder.save();
                throw new RuntimeException("Please setup Proof of Origin tokens.");
            }
        } catch (ConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
