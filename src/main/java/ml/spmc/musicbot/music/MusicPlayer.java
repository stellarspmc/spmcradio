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

import static ml.spmc.musicbot.MusicBot.bot;

public class MusicPlayer {

    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();
    private static final MusicManager musicManager = new MusicManager(manager);
    private static final Guild guild = bot.getGuildById(Config.GUILD_ID);
    private static final AudioPlayer player = musicManager.player;
    private static final ArrayList<String> queue = new ArrayList<>();

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
        Collections.shuffle(queue);
        for (String queue: queue) {
            play(queue);
        }
    }

    public static void stopAndPlay(String url) {
        queue.clear();
        musicManager.scheduler.clearQueue();
        player.stopTrack();
        play(url);
    }

    public static void play(String url) {
        manager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                queue.add(track.getInfo().uri);
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playList) {
                for (AudioTrack track: playList.getTracks()) {
                    queue.add(track.getInfo().uri);
                    musicManager.scheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException exception) {}
        });
    }
}
