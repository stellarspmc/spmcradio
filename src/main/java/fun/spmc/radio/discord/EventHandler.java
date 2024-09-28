package fun.spmc.radio.discord;

import com.sedmelluq.discord.lavaplayer.track.*;

import fun.spmc.radio.Utilities;
import fun.spmc.radio.music.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.*;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.net.URL;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class EventHandler extends ListenerAdapter {

    private static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent e) {
        e.getGuild().updateCommands().addCommands(
                Commands.slash("play", "Play a song you want to listen! It can be from YouTube or SoundCloud!")
                        .addOption(OptionType.STRING, "song", "The song you want to play.", true, true),
                Commands.slash("queue", "Queue a song you want to listen! It can be from YouTube or SoundCloud!")
                        .addOption(OptionType.STRING, "song", "The song you want to queue.", true, true),
                Commands.slash("nowplaying", "Check what song is playing!"),
                Commands.slash("queuelist", "Get the queue list of songs!"),
                Commands.slash("volume", "Only for admins.")
                        .addOption(OptionType.INTEGER, "volume", "Volume", true, false),
                Commands.slash("shuffle", "Shuffles the queue!"),
                Commands.slash("skip", "Skip the song playing.")
        ).queue();
    }
    
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "play" -> {
                String url = Objects.requireNonNull(e.getOption("song")).getAsString();
                for (MusicType type: MusicType.values()) {
                    if (url.equalsIgnoreCase(type.name())) {
                        MusicPlayer.stopAndLoadSong(type.getUrl(), e);
                        return;
                    }
                }

                url = isValidURL(url) ? url : "ytmsearch:" + url;
                MusicPlayer.stopAndLoadSong(url, e);
            }
            case "queue" -> {
                String url = Objects.requireNonNull(e.getOption("song")).getAsString();
                for (MusicType type: MusicType.values()) {
                    if (url.equals(type.name().toLowerCase()) || url.equals(type.name().toUpperCase())) {
                        MusicPlayer.loadSong(type.getUrl(), e);
                        return;
                    }
                }

                url = isValidURL(url) ? url : "ytmsearch:" + url;
                MusicPlayer.loadSong(url, e);
            }
            case "nowplaying" -> e.replyEmbeds(getNowPlayingEmbed()).queue();
            case "queuelist" -> e.replyEmbeds(getQueueListEmbed()).queue();
            case "volume" -> {
                int volume = Objects.requireNonNull(e.getOption("volume")).getAsInt();
                if (volume > 100 || volume < 0) volume = 50;
                TrackScheduler.setVolume(volume);
                e.replyEmbeds(getVolumeEmbed(volume)).queue();
            }
            case "shuffle" -> {
                TrackScheduler.shuffle();
                e.replyEmbeds(getShuffleEmbed()).queue();
            }
            case "skip" -> {
                TrackScheduler.skipTrack();
                e.replyEmbeds(getSkipEmbed()).queue();
            }
        }
    }

    private static @NotNull MessageEmbed getNowPlayingEmbed() {
        AudioTrack playingTrack = TrackScheduler.getPlayingTrack();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.addField("Now Playing", MarkdownUtil.maskedLink(playingTrack.getInfo().title, playingTrack.getInfo().uri), true);
        embedBuilder.addField("Author", MarkdownUtil.monospace(playingTrack.getInfo().author), true);
        embedBuilder.addField("Duration", MarkdownUtil.monospace(Utilities.getDuration(Duration.ofMillis(playingTrack.getPosition())) + " - " + Utilities.getDuration(Duration.ofMillis(playingTrack.getDuration()))), true);
        return Utilities.appendEmbed(embedBuilder);
    }

    private static @NotNull MessageEmbed getVolumeEmbed(int volume) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("Changed volume to " + MarkdownUtil.monospace(String.valueOf(volume)) +"%.");
        embedBuilder.setColor(new Color(2600572));
        return embedBuilder.build();
    }

    private static @NotNull MessageEmbed getShuffleEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("Shuffled queue.");
        embedBuilder.setColor(new Color(2600572));
        return embedBuilder.build();
    }

    private static @NotNull MessageEmbed getSkipEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription("Skipped track.");
        embedBuilder.setColor(new Color(2600572));
        return embedBuilder.build();
    }

    private static @NotNull MessageEmbed getQueueListEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Queue List");

        ArrayList<AudioTrack> array = TrackScheduler.arrayQueue;
        StringBuilder oldString = new StringBuilder();
        StringBuilder string = new StringBuilder();

        for (AudioTrack track: array) {
            oldString.append(Objects.equals(track.getIdentifier(), TrackScheduler.getPlayingTrack().getIdentifier()) ? "→ " : "");
            oldString.append(array.indexOf(track) + 1).append(". ").append(track.getInfo().title).append(" - ").append(track.getInfo().author);
            oldString.append(" (").append(Utilities.getDuration(Duration.ofMillis(track.getDuration()))).append(")\n");
            if (oldString.toString().length() + string.toString().length() > 4096) break;
            string.append(oldString);
            oldString.setLength(0);
        }

        embedBuilder.setDescription(string.toString());
        embedBuilder.addField("Total Track Count", MarkdownUtil.monospace(String.valueOf(array.size())), true);
        embedBuilder.addField("Total Time", MarkdownUtil.monospace(Utilities.getDuration(Duration.ofMillis(array.stream().mapToLong(AudioTrack::getDuration).sum()))), true);
        embedBuilder.addField("Now Playing", MarkdownUtil.maskedLink(TrackScheduler.getPlayingTrack().getInfo().title, TrackScheduler.getPlayingTrack().getInfo().uri), true);
        return Utilities.appendEmbed(embedBuilder);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if ((event.getName().equals("play") || event.getName().equals("queue")) && event.getFocusedOption().getName().equals("song")) {
            ArrayList<String> string = new ArrayList<>();
            for (MusicType type : MusicType.values()) {
                string.add(type.name().toLowerCase());
            }
            try {
                String[] type = string.toArray(new String[]{});

                Collection<Command.Choice> options = Stream.of(type)
                        .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                        .map(word -> new Command.Choice(word, word))
                        .collect(Collectors.toList());
                event.replyChoices(options).queue();
            } catch (ClassCastException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}