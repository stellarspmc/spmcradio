package fun.spmc.radio;

import fun.spmc.radio.discord.EventHandler;
import fun.spmc.radio.features.SPMCWrapped;
import fun.spmc.radio.features.SongToTitleCacher;
import fun.spmc.radio.music.MusicPlayer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SPMCRadio {
    public static JDA bot;
    public static void main(String[] args) throws InterruptedException, IOException {
        Files.createDirectories(Paths.get("config"));
        Config.init();
        SPMCWrapped.init();
        SongToTitleCacher.init();
        bot = JDABuilder.createDefault(Config.BOT_TOKEN)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new EventHandler())
                .build();
         bot.awaitReady();
         MusicPlayer.playMusic();
    }
}
