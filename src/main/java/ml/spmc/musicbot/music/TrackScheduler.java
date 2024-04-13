package ml.spmc.musicbot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.Arrays;
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
        if (!arrayQueue.isEmpty()) queue.offer(track);
        player.startTrack(track, true);
        arrayQueue.add(track);
    }

    public static void shuffle() {
        AudioTrack playing = player.getPlayingTrack();
        ArrayList<AudioTrack> array = arrayQueue;

        array.remove(playing);
        queue.removeAll(array);
        System.out.println(Arrays.toString(queue.toArray()));
        Collections.shuffle(array);
        for (AudioTrack track: array) {
            queue.offer(track);
        }

        array.add(playing);

        shuffled = !shuffled;
        arrayQueue = array;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;

        AudioTrack track2 = queue.poll();
        assert track2 != null;
        track2.setPosition(0);
        boolean bool = player.startTrack(track2, false);
        if (track2.getPosition() == track2.getDuration() || !bool) MusicPlayer.loopQueue();
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
