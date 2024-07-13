package fun.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static fun.spmc.radio.EventHandler.getDuration;


public class TrackScheduler extends AudioEventAdapter {

    private static MessageEmbed createEmbed(String[] detail) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.addField(detail[0].equalsIgnoreCase("a") ? "Playlist " : "Track", detail[1], true);
        embedBuilder.addField("Author", detail[2], true);
        embedBuilder.addField("Duration", getDuration(Duration.ofMillis(Long.parseLong(detail[3]))), true);

        embedBuilder.setColor(new Color(2600572));
        embedBuilder.setAuthor("TCFPlayz", "https://mc.spmc.fun", "https://cdn.discordapp.com/avatars/340022376924446720/dff2fd1a8161150ce10b7138c66ca58c.webp?size=1024");
        embedBuilder.setFooter("SPMCRadio 2.5.3");
        embedBuilder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        return embedBuilder.build();
    }

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
