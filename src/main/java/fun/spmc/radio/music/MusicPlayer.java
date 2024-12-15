package fun.spmc.radio.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import fun.spmc.radio.Utilities;
import fun.spmc.radio.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.time.Duration;
import java.util.*;

import static fun.spmc.radio.SPMCRadio.bot;

public class MusicPlayer {
    private static final Logger log = LoggerFactory.getLogger(MusicPlayer.class);
    private static @NotNull MessageEmbed createEmbed(AudioItem item, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (item instanceof AudioTrack track) {
            bot.getPresence().setPresence(OnlineStatus.IDLE, Activity.listening(track.getInfo().title));
            embedBuilder.addField("Queuing Track", MarkdownUtil.maskedLink(MarkdownUtil.monospace(track.getInfo().title), track.getInfo().uri), true);
            embedBuilder.addField("Requested By", "<@" + user.getId() + ">", true);
            embedBuilder.addField("Duration", MarkdownUtil.monospace(Utilities.getDuration(Duration.ofMillis(track.getDuration()))), true);
        } else if (item instanceof AudioPlaylist playlist) {
            if (playlist.isSearchResult()) {
                bot.getPresence().setPresence(OnlineStatus.IDLE, Activity.listening(playlist.getSelectedTrack().getInfo().title));
                embedBuilder.addField("Queuing Track", MarkdownUtil.maskedLink(playlist.getSelectedTrack().getInfo().title, playlist.getSelectedTrack().getInfo().uri), true);
                embedBuilder.addField("Requested By", "<@" + user.getId() + ">", true);
                embedBuilder.addField("Duration", MarkdownUtil.monospace(Utilities.getDuration(Duration.ofMillis(playlist.getSelectedTrack().getDuration()))), true);
            } else {
                bot.getPresence().setPresence(OnlineStatus.ONLINE, Activity.listening(playlist.getName()));
                embedBuilder.addField("Queuing Playlist", MarkdownUtil.monospace(playlist.getName()), true);
                embedBuilder.addField("Requested By", "<@" + user.getId() + ">", true);
                embedBuilder.addField("Duration", MarkdownUtil.monospace(Utilities.getDuration(Duration.ofMillis(playlist.getTracks().stream().mapToLong(AudioTrack::getDuration).sum()))), true);
            }
        }

        return Utilities.appendEmbed(embedBuilder);
    }

    private static final AudioPlayerManager manager = new DefaultAudioPlayerManager();

    private static final MusicManager musicManager = new MusicManager(manager);
    private static final Guild guild = bot.getGuildById(Config.GUILD_ID);
    private static final AudioPlayer player = musicManager.player;

    public static void playMusic() {
        YoutubeAudioSourceManager source = new YoutubeAudioSourceManager(true, new TvHtml5EmbeddedWithThumbnail(), new MusicWithThumbnail());
        Web.setPoTokenAndVisitorData(Config.PO_TOKEN, Config.VISITOR_DATA);
        manager.registerSourceManager(source);

        VoiceChannel channel = bot.getVoiceChannelById(Config.MUSIC_CHANNEL_ID);
        assert guild != null;
        final AudioManager manager2 = guild.getAudioManager();
        manager2.setSendingHandler(musicManager.getSendHandler());
        if (!manager2.isConnected()) {
            manager2.openAudioConnection(channel);
            AudioSourceManagers.registerRemoteSources(manager);
            AudioSourceManagers.registerLocalSource(manager);
            manager2.setSelfDeafened(true);
            player.setVolume(100);
        }
        if (player.isPaused()) player.setPaused(false);
        if (player.getVolume() == 0) player.setVolume(50);
        loadSong(MusicType.DEFAULT.getUrl(), null);
    }

    public static void loopQueue() {
        ArrayList<AudioTrack> queue = new ArrayList<>(TrackScheduler.arrayQueue);
        if (TrackScheduler.shuffled) Collections.shuffle(queue);
        musicManager.scheduler.clearQueue();
        for (AudioTrack track: queue) {
            loadSong(track.getInfo().uri, null);
        }
    }

    public static void stopAndLoadSong(String url, SlashCommandInteractionEvent event) {
        musicManager.scheduler.clearQueue();
        player.stopTrack();

        TrackScheduler.shuffled = false;
        loadSong(url, event);
    }

    public static void loadSong(String url, SlashCommandInteractionEvent event) {
        manager.loadItemOrdered(musicManager, url, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
                if (event != null) event.replyEmbeds(createEmbed(track, event.getUser())).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.isSearchResult()) {
                    musicManager.scheduler.queue(playlist.getTracks().get(0));
                    if (event != null) event.replyEmbeds(createEmbed(playlist.getTracks().get(0), event.getUser())).queue();
                }
                else {
                    playlist.getTracks().forEach(musicManager.scheduler::queue);
                    if (event != null) event.replyEmbeds(createEmbed(playlist, event.getUser())).queue();
                }
            }

            @Override
            public void noMatches() {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setDescription("No matches");
                embedBuilder.setColor(new Color(2600572));
                if (event != null) event.replyEmbeds(embedBuilder.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Error, please report this to the admins");
                embedBuilder.setDescription(exception.getMessage());
                if (event != null) event.replyEmbeds(Utilities.appendEmbed(embedBuilder)).queue();
                log.error(exception.getMessage());
            }
        });
    }
}
