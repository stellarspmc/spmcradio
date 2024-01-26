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
        play(MusicType.SMP.getUrl(), true, false);
    }

    public static void loopQueue() {
        for (String queue: queue) {
            play(queue, true, true);
        }
    }

    public static void stopAndPlay(String url, boolean shuffle) {
        queue.clear();
        trackQueue.clear();
        musicManager.scheduler.clearQueue();
        player.stopTrack();
        play(url, shuffle, false);
    }

    public static void play(String url, boolean shuffle, boolean repeat) {
        manager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (!repeat) {
                    queue.add(track.getInfo().uri);
                    trackQueue.add(track);
                }
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playList) {
                List<AudioTrack> tracks = playList.getTracks();
                if (shuffle) Collections.shuffle(tracks);
                if (url.contains("ytsearch")) {
                    if (!repeat) {
                        queue.add(playList.getTracks().get(0).getInfo().uri);
                        trackQueue.add(playList.getTracks().get(0));
                    }
                    musicManager.scheduler.queue(playList.getTracks().get(0));
                } else {
                    for (AudioTrack track: tracks) {
                        if (!repeat) {
                            queue.add(track.getInfo().uri);
                            trackQueue.add(track);
                        }
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
