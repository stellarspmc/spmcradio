package fun.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static fun.spmc.radio.SPMCRadio.bot;


public class TrackScheduler extends AudioEventAdapter {

    private static AudioPlayer player;
    private static BlockingQueue<AudioTrack> queue;
    public static ArrayList<AudioTrack> arrayQueue;
    AudioTrack lastTrack;
    public static boolean shuffled = false;

    public TrackScheduler(AudioPlayer player) {
        TrackScheduler.player = player;
        queue = new LinkedBlockingQueue<>();
        arrayQueue = new ArrayList<>();
    }

    private void queue(AudioTrack track) {
        bot.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening(track.getInfo().title));
        if (!arrayQueue.isEmpty()) queue.offer(track);
        player.startTrack(track, true);
        arrayQueue.add(track);
    }

    public void queuePlaylist(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) queue(playlist.getSelectedTrack());
        else playlist.getTracks().forEach(this::queue);
    }

    public void queueTrack(AudioTrack track) {
        queue(track);
    }

    public static void shuffle() {
        AudioTrack playing = player.getPlayingTrack();
        ArrayList<AudioTrack> array = arrayQueue;

        array.remove(playing);
        queue.removeAll(array);
        Collections.shuffle(array);
        for (AudioTrack track: array) {
            if (!track.equals(playing)) track.setPosition(0);
            queue.offer(track);
        }

        array.add(0, playing);

        shuffled = !shuffled;
        arrayQueue = array;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        AudioTrack track2 = queue.poll();
        boolean bool = player.startTrack(track2, false);
        if (!bool) MusicPlayer.loopQueue();

        if (getPlayingTrack().getPosition() == getPlayingTrack().getDuration()) getPlayingTrack().setPosition(0);
    }

    public void clearQueue() {
        queue.clear();
        arrayQueue.clear();
    }

    public static AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public static void setVolume(int volume) {
        player.setVolume(volume);
    }
}
