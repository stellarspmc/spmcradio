package ml.spmc.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.spmc.musicbot.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ml.spmc.musicbot.MusicBot.bot;

public class MusicPlayer {

    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();

    public static MusicManager getMusicManager() {
        return musicManager;
    }

    private static final MusicManager musicManager = new MusicManager(manager);
    private static final Guild guild = bot.getGuildById(Config.GUILD_ID);
    private static final AudioPlayer player = musicManager.player;
    private static final ArrayList<String> queue = new ArrayList<>();
    public static final ArrayList<AudioTrack> trackQueue = new ArrayList<>();

    public static void playMusic() {
        VoiceChannel channel = bot.getVoiceChannelById(Config.MUSIC_CHANNEL_ID);
        assert guild != null;
        final AudioManager manager2 = guild.getAudioManager();
        manager2.setSendingHandler(musicManager.getSendHandler());
        if (!manager2.isConnected()) {
            manager2.openAudioConnection(channel);
            AudioSourceManagers.registerRemoteSources(manager);
            AudioSourceManagers.registerLocalSource(manager);
            manager2.setSelfDeafened(true);
            player.setVolume(5);
        }
        if (player.isPaused()) player.setPaused(false);
        if (player.getVolume() == 0) player.setVolume(50);
        play(MusicType.SMP.getUrl());
    }

    public static void loopQueue() {
        if (TrackScheduler.shuffled) Collections.shuffle(queue);
        for (String queue: queue) {
            trackQueue.clear();
            loopedPlay(queue);
        }
    }

    public static void stopAndPlay(String url) {
        queue.clear();
        trackQueue.clear();
        musicManager.scheduler.clearQueue();
        player.stopTrack();

        TrackScheduler.shuffled = false;
        play(url);
    }

    public static void play(String url) {
        manager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackQueue.add(track);
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playList) {
                List<AudioTrack> tracks = playList.getTracks();
                if (url.contains("ytsearch")) {
                    trackQueue.add(playList.getTracks().get(0));
                    musicManager.scheduler.queue(playList.getTracks().get(0));
                } else {
                    for (AudioTrack track: tracks) {
                        trackQueue.add(track);
                        musicManager.scheduler.queue(track);
                    }
                }
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });
    }

    private static void loopedPlay(String url) {
        manager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playList) {
                List<AudioTrack> tracks = playList.getTracks();
                if (url.contains("ytsearch")) musicManager.scheduler.queue(playList.getTracks().get(0));
                else {
                    for (AudioTrack track: tracks) {
                        musicManager.scheduler.queue(track);
                    }
                }
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
