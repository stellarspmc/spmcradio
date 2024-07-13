package ml.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.spmc.radio.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.*;

import static ml.spmc.radio.SPMCRadio.bot;

public class MusicPlayer {

    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();

    private static final MusicManager musicManager = new MusicManager(manager);
    private static final Guild guild = bot.getGuildById(Config.GUILD_ID);
    private static final AudioPlayer player = musicManager.player;

    public static void playMusic() {
        VoiceChannel channel = bot.getVoiceChannelById(Config.MUSIC_CHANNEL_ID);
        assert guild != null;
        dev.lavalink.youtube.YoutubeAudioSourceManager ytSourceManager = new dev.lavalink.youtube.YoutubeAudioSourceManager();
        manager.registerSourceManager(ytSourceManager);

        final AudioManager manager2 = guild.getAudioManager();
        manager2.setSendingHandler(musicManager.getSendHandler());
        if (!manager2.isConnected()) {
            manager2.openAudioConnection(channel);
            AudioSourceManagers.registerRemoteSources(manager, YoutubeAudioSourceManager.class);
            AudioSourceManagers.registerLocalSource(manager);
            manager2.setSelfDeafened(true);
            player.setVolume(100);
        }
        if (player.isPaused()) player.setPaused(false);
        if (player.getVolume() == 0) player.setVolume(50);
        play(MusicType.DEFAULT.getUrl());
        TrackScheduler.shuffle();
    }

    public static void loopQueue() {
        ArrayList<AudioTrack> queue = new ArrayList<>(TrackScheduler.arrayQueue);
        if (TrackScheduler.shuffled) Collections.shuffle(queue);
        musicManager.scheduler.clearQueue();
        for (AudioTrack track: queue) {
            play(track.getInfo().uri);
        }
    }

    public static void stopAndPlay(String url) {
        musicManager.scheduler.clearQueue();
        player.stopTrack();

        TrackScheduler.shuffled = false;
        play(url);
    }

    public static void play(String url) {
        manager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queueTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playList) {
                musicManager.scheduler.queuePlaylist(playList);
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }
}
