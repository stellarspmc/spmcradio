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
        arrayQueue = new ArrayList<>();
    }

    public void queue(AudioTrack track) {
        if (!arrayQueue.isEmpty()) queue.offer(track);
        player.startTrack(track, true);
        arrayQueue.add(track);
    }

    public static void shuffle() {
        ArrayList<AudioTrack> array = arrayQueue;

        queue.clear();
        Collections.shuffle(array);
        for (AudioTrack track: array) {
            queue.offer(track);
        }

        shuffled = !shuffled;
        arrayQueue = array;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.lastTrack = track;
        AudioTrack track2 = queue.poll();
        if (track2 != null) {
            if (track2.getPosition() == track2.getDuration()) track2.setPosition(0);
            boolean bool = player.startTrack(track2, false);
            if (!bool) MusicPlayer.loopQueue();
        }

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
