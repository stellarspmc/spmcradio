package fun.spmc.radio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fun.spmc.radio.music.MusicPlayer;
import fun.spmc.radio.music.MusicType;
import fun.spmc.radio.music.TrackScheduler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
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
import java.time.Instant;
import java.util.ArrayList;
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
                Commands.slash("play", "Play a song you want to listen! It can be from YouTube or SoundCloud!")
                        .addOption(OptionType.STRING, "song", "The song you want to play.", true, true),
                Commands.slash("queue", "Queue a song you want to listen! It can be from YouTube or SoundCloud!")
                        .addOption(OptionType.STRING, "song", "The song you want to queue.", true, true),
                Commands.slash("nowplaying", "Check what song is playing!"),
                Commands.slash("queuelist", "Get the queue list of songs!"),
                Commands.slash("volume", "Only for owner cuz scared of abusing")
                        .addOption(OptionType.INTEGER, "volume", "Volume", true, false),
                Commands.slash("shuffle", "Shuffles the queue!")
        ).queue();
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "play" -> {
                String url = Objects.requireNonNull(e.getOption("song")).getAsString();
                for (MusicType type: MusicType.values()) {
                    if (url.equals(type.name().toLowerCase()) || url.equals(type.name().toUpperCase())) {
                        MusicPlayer.stopAndPlay(type.getUrl());
                        e.reply("Now playing tracks.").queue();
                        return;
                    }
                }

                url = isValidURL(url) ? url : "ytsearch:" + url;
                MusicPlayer.stopAndPlay(url);
                e.reply("Now playing tracks.").queue();
            }
            case "queue" -> {
                String url = Objects.requireNonNull(e.getOption("song")).getAsString();
                for (MusicType type: MusicType.values()) {
                    if (url.equals(type.name().toLowerCase()) || url.equals(type.name().toUpperCase())) {
                        MusicPlayer.play(type.getUrl());
                        e.reply("Now queuing tracks.").queue();
                        return;
                    }
                }

                url = isValidURL(url) ? url : "ytsearch:" + url;
                MusicPlayer.play(url);
                e.reply("Now queuing tracks.").queue();
            }
            case "nowplaying" -> e.replyEmbeds(getNowPlayingEmbed()).queue();
            case "queuelist" -> e.replyEmbeds(getQueueListEmbed()).queue();
            case "volume" -> {
                int volume = Objects.requireNonNull(e.getOption("volume")).getAsInt();
                if (volume > 100 || volume < 0) e.deferReply().queue();
                if (!Objects.requireNonNull(e.getMember()).hasPermission(Permission.ADMINISTRATOR)) e.deferReply().queue();
                TrackScheduler.setVolume(volume);
                e.reply("Changed volume to " + volume + "%.").queue();
            }
            case "shuffle" -> {
                TrackScheduler.shuffle();
                e.reply("Shuffled queue!").queue();
            }
        }
    }

    @NotNull
    private static MessageEmbed getNowPlayingEmbed() {
        AudioTrack playingTrack = TrackScheduler.getPlayingTrack();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.addField("Track", playingTrack.getInfo().title, true);
        embedBuilder.addField("Author", playingTrack.getInfo().author, true);
        embedBuilder.addField("Duration", getDuration(Duration.ofMillis(playingTrack.getPosition())) + " - " + getDuration(Duration.ofMillis(playingTrack.getDuration())), true);

        embedBuilder.setColor(new Color(2600572));
        embedBuilder.setAuthor("TCFPlayz", "https://mc.spmc.fun", "https://cdn.discordapp.com/avatars/340022376924446720/dff2fd1a8161150ce10b7138c66ca58c.webp?size=1024");
        embedBuilder.setFooter("SPMCRadio 2.5.3");
        embedBuilder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        return embedBuilder.build();
    }

    @NotNull
    private static MessageEmbed getQueueListEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(new Color(2600572));
        embedBuilder.setAuthor("TCFPlayz", "https://mc.spmc.fun", "https://cdn.discordapp.com/avatars/340022376924446720/dff2fd1a8161150ce10b7138c66ca58c.webp?size=1024");
        embedBuilder.setFooter("SPMCRadio 2.5.3");
        embedBuilder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        embedBuilder.setTitle("Queue");

        AudioTrack[] array = TrackScheduler.arrayQueue.toArray(new AudioTrack[0]);

        StringBuilder oldString = new StringBuilder();
        StringBuilder string = new StringBuilder();
        int count = 0;
        long dur = 0;
        long pos = 0;

        for (AudioTrack track: array) {
            count += 1;
            if (track.equals(TrackScheduler.getPlayingTrack())) oldString
                        .append("→ ").append(count).append(". ").append(track.getInfo().title)
                        .append(" - ").append(track.getInfo().author)
                        .append(" (").append(getDuration(Duration.ofMillis(track.getPosition()))).append(" - ").append(getDuration(Duration.ofMillis(track.getDuration())))
                        .append(")\n");
            else oldString
                    .append(count).append(". ").append(track.getInfo().title)
                    .append(" - ").append(track.getInfo().author)
                    .append(" (").append(getDuration(Duration.ofMillis(track.getPosition()))).append(" - ").append(getDuration(Duration.ofMillis(track.getDuration())))
                    .append(")\n");
            if (oldString.toString().length() + string.toString().length() > 4096) break;
            string.append(oldString);
            oldString.setLength(0);
        }

        for (AudioTrack track: array) {
            dur += track.getDuration();
            pos += track.getPosition();
        }

        embedBuilder.setDescription(string.toString());
        embedBuilder.addField("Listened Duration", getDuration(Duration.ofMillis(pos)), true);
        embedBuilder.addField("Total Duration", getDuration(Duration.ofMillis(dur)), true);
        return embedBuilder.build();
    }

    @NotNull
    public static String getDuration(Duration d) {
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