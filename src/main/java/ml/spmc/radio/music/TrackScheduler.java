package ml.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
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

    public void passOnData(AudioTrack track) {
        MusicPlayer.details[0] = "Track";
        MusicPlayer.details[1] = track.getInfo().title;
        MusicPlayer.details[2] = track.getInfo().author;
        MusicPlayer.details[3] = String.valueOf(track.getDuration());
    }

    public void passOnList(AudioPlaylist list, String url) {
        MusicPlayer.details[0] = "a";
        MusicPlayer.details[1] = list.getName();
        MusicPlayer.details[2] = list.isSearchResult() ? "Search Result" : "Unknown";
        MusicPlayer.details[3] = url.contains("ytsearch") ? String.valueOf(list.getTracks().get(0).getDuration()) : String.valueOf(list.getTracks().stream().mapToLong(AudioTrack::getDuration).sum());
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
        Collections.shuffle(array);
        for (AudioTrack track: array) {
            track.setPosition(0);
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
