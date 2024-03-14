package ml.spmc.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {

    private static AudioPlayer player;
    private static BlockingQueue<AudioTrack> queue;
    public static ArrayList<AudioTrack> arrayQueue;
    AudioTrack lastTrack;
    public static boolean shuffled = false;

    public TrackScheduler(AudioPlayer player) {
        TrackScheduler.player = player;
        queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            arrayQueue.add(track);
        }
    }

    public static void shuffle() {
        queue.clear();
        Collections.shuffle(arrayQueue);
        for (AudioTrack track: arrayQueue) {
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
        nextTrack();
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
