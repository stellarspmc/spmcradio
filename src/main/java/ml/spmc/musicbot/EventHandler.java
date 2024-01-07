package ml.spmc.musicbot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ml.spmc.musicbot.music.MusicPlayer;
import ml.spmc.musicbot.music.MusicType;
import ml.spmc.musicbot.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void onGuildReady(@Nullable GuildReadyEvent e) {
        assert e != null;
        e.getGuild().updateCommands().addCommands(
                Commands.slash("play", "Queue a song you want to listen! It can be from YouTube or SoundCloud (Playlist works too)!")
                        .addOption(OptionType.STRING, "song", "The song you want to search or the bot's collection of music.", true, true)
                        .addOption(OptionType.BOOLEAN, "extend", "Are you extending the queue?", true, false),
                Commands.slash("nowplaying", "Check what song is playing!"),
                Commands.slash("skip", "Skip the song playing."),
                Commands.slash("queuelist", "Get the queue list of songs!")
                ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "play" -> {
                String url = Objects.requireNonNull(e.getOption("song")).getAsString();
                boolean extend = Objects.requireNonNull(e.getOption("extend")).getAsBoolean();
                for (MusicType type: MusicType.values()) {
                    if (url.equals(type.name().toLowerCase()) || url.equals(type.name().toUpperCase())) {
                        if (!extend) MusicPlayer.stopAndPlay(type.getUrl());
                        else MusicPlayer.play(type.getUrl());
                        e.reply("Now playing bot's tracks.").queue();
                    }
                }

                url = isValidURL(url) ? url : "ytsearch:" + url;
                if (extend) MusicPlayer.play(url);
                else MusicPlayer.stopAndPlay(url);

                e.reply("Now playing external tracks.").queue();
            }
            case "nowplaying" -> e.replyEmbeds(getNowPlayingEmbed()).queue();
            case "skip" -> {
                TrackScheduler.skipTrack();
                e.reply("Skipped track.").queue();
            }
            case "queuelist" -> e.replyEmbeds(getQueueListEmbed()).queue();
        }
    }

    @NotNull
    private static MessageEmbed getNowPlayingEmbed() {
        AudioTrack playingTrack = TrackScheduler.getPlayingTrack();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(playingTrack.getInfo().title + " - " + playingTrack.getInfo().author, playingTrack.getInfo().uri);
        embedBuilder.setDescription(getDuration(Duration.ofMillis(playingTrack.getPosition())) + " - " + getDuration(Duration.ofMillis(playingTrack.getDuration())));
        embedBuilder.setColor(new Color(2600572));
        embedBuilder.setAuthor("Provided by TCFPlayz", "https://dc.spmc.tk", "https://cdn.discordapp.com/avatars/340022376924446720/dff2fd1a8161150ce10b7138c66ca58c.webp?size=1024");
        return embedBuilder.build();
    }

    @NotNull
    private static MessageEmbed getQueueListEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(2600572));
        embedBuilder.setAuthor("Provided by TCFPlayz", "https://dc.spmc.tk", "https://cdn.discordapp.com/avatars/340022376924446720/dff2fd1a8161150ce10b7138c66ca58c.webp?size=1024");
        embedBuilder.setTitle("Queue List");

        ArrayList<AudioTrack> array = new ArrayList<>(Arrays.asList(TrackScheduler.getQueue()));
        array.add(0, TrackScheduler.getPlayingTrack());

        StringBuilder string = new StringBuilder();
        int count = 0;

        for (AudioTrack track: array) {
            count += 1;
            string
                    .append(count).append(". ").append(track.getInfo().title)
                    .append(" - ").append(track.getInfo().author)
                    .append(" (").append(getDuration(Duration.ofMillis(track.getPosition()))).append(" - ").append(getDuration(Duration.ofMillis(track.getDuration())))
                    .append(")\n");
        }

        embedBuilder.setDescription(string.toString());
        return embedBuilder.build();
    }

    @NotNull
    private static String getDuration(Duration d) {
        String m = String.valueOf(d.toMinutesPart());
        String s = String.valueOf(d.toSecondsPart());
        String h = String.valueOf(d.toHoursPart());

        if (Integer.parseInt(m) < 10) m = "0" + m;
        if (Integer.parseInt(s) < 10) s = "0" + s;
        if (Integer.parseInt(h) < 10) h = "0" + h;
        if (Integer.parseInt(h) == 0) return m + ":" + s;
        else return h + ":" + m + ":" + s;
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("play") && event.getFocusedOption().getName().equals("song")) {
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