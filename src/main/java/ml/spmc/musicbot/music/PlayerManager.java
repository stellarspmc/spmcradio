package ml.spmc.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import static ml.spmc.musicbot.MusicBot.bot;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final MusicManager guildmanager;
    private final AudioPlayerManager manager;

    public PlayerManager() {
        this.manager = new DefaultAudioPlayerManager();
        this.guildmanager = new MusicManager(this.manager);

        AudioSourceManagers.registerRemoteSources(this.manager);
        AudioSourceManagers.registerLocalSource(this.manager);
    }

    public MusicManager getGuildManager() {
        bot.getGuildById("1099209033996587129").getAudioManager().setSendingHandler(guildmanager.getSendHandler());
        return guildmanager;
    }

}
