package ml.spmc.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {

    private static AudioPlayer player;
    private static BlockingQueue<AudioTrack> queue;
    AudioTrack lastTrack;
    public static boolean shuffled = false;

    public TrackScheduler(AudioPlayer player) {
        TrackScheduler.player = player;
        queue = new LinkedBlockingQueue<>();
    }

    public static void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) queue.offer(track);
    }

    public static void shuffle() {
        List<AudioTrack> array = new java.util.ArrayList<>(List.of(queue.toArray(new AudioTrack[]{})));

        clearQueue();
        Collections.shuffle(array);
        for (AudioTrack track: array) {
            queue.offer(track);
        }

        shuffled = !shuffled;
    }

    public void nextTrack() {
        AudioTrack track = queue.poll();
        if (track == null) MusicPlayer.loopQueue();
        else player.startTrack(track, false);
    }

    public static void skipTrack() {
        player.stopTrack();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        if (queue.isEmpty() || lastTrack == queue.toArray()[queue.size() - 1]) MusicPlayer.loopQueue();
        else nextTrack();
    }

    public static void clearQueue() {
        queue.clear();
    }

    public static AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public static void setVolume(int volume) {
        player.setVolume(volume);
    }
}
