package fun.spmc.radio.music;

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
        arrayQueue = new ArrayList<>();
    }

    public void queue(AudioTrack track) {
        AudioTrack clonedTrack = track.makeClone();
        if (!arrayQueue.isEmpty()) queue.offer(clonedTrack);
        startTrack(clonedTrack, true);
        arrayQueue.add(clonedTrack);
    }

    public static void shuffle() {
        AudioTrack playing = player.getPlayingTrack();
        ArrayList<AudioTrack> array = arrayQueue;

        queue.clear();
        Collections.shuffle(array);
        array.remove(playing);
        array.forEach(track -> queue.offer(track.makeClone()));
        array.add(0, playing);

        shuffled = !shuffled;
        arrayQueue = array;
    }

    public static void skipTrack() {
        player.stopTrack();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        if (endReason.mayStartNext || endReason == AudioTrackEndReason.CLEANUP || endReason == AudioTrackEndReason.STOPPED) {
            AudioTrack track2 = queue.poll();
            if (track2 != null) {
                boolean bool = startTrack(track2.makeClone(), false);
                if (!bool) MusicPlayer.loopQueue();
            } else MusicPlayer.loopQueue();
        }
    }

    public static boolean startTrack(AudioTrack track, boolean bool) {
        return player.startTrack(track, bool);
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
