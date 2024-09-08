package fun.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.jetbrains.annotations.NotNull;

public class MusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    public MusicManager(@NotNull AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public MusicSendHandler getSendHandler() {
        return new MusicSendHandler(player);
    }
}