package ml.spmc.musicbot;

import ml.spmc.musicbot.music.MusicPlayer;
import ml.spmc.musicbot.music.MusicType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventHandler extends ListenerAdapter {

    @Override
    public void onGuildReady(@Nullable GuildReadyEvent e) {
        assert e != null;
        e.getGuild().updateCommands().addCommands(
                Commands.slash("musicselection", "Select the type of music you want to listen!")
                        .addOption(OptionType.STRING, "type", "Type of music.", true, true)
        ).queue();
    }

    @Override
    public void onReady(@NotNull ReadyEvent e) {
        MusicBot.bot.updateCommands().addCommands(
                Commands.slash("appeal", "Appeal?")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        switch (e.getName()) {
            case "musicselection" -> {
                String string = Objects.requireNonNull(e.getOption("type")).getAsString();
                for (MusicType type: MusicType.values()) {
                    if (string.equals(type.name().toLowerCase()) || string.equals(type.name().toUpperCase())) {
                        MusicPlayer.type = type;
                        MusicPlayer.stopAndPlayNewList(type.getUrl());
                        e.reply("Changing to " + string + " type.").queue();
                    }
                }
            }
            case "appeal" -> {
                TextInput menu = TextInput.create("from", "From", TextInputStyle.SHORT)
                        .setPlaceholder("Where were you warned / kicked / banned. (Minecraft/Discord...)")
                        .build();
                TextInput subject = TextInput.create("subject", "Title", TextInputStyle.SHORT)
                        .setPlaceholder("The reason you're warned / etc.")
                        .setMinLength(10)
                        .setMaxLength(100)
                        .build();
                TextInput body = TextInput.create("body", "Reason", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Reason for unmute / kick / ban?")
                        .setMinLength(30)
                        .setMaxLength(1000)
                        .build();
                Modal modal = Modal.create("appeal", "Appeal")
                        .addActionRow(ActionRow.of(menu).getComponents())
                        .addActionRow(ActionRow.of(subject).getComponents())
                        .addActionRow(ActionRow.of(body).getComponents())
                        .build();
                e.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("musicselection") && event.getFocusedOption().getName().equals("type")) {
            String[] type = new String[]{"phonk", "ncs", "smp", "minecraft"};

            Collection<Command.Choice> options = Stream.of(type)
                    .filter(word -> word.startsWith(event.getFocusedOption().getValue()))
                    .map(word -> new Command.Choice(word, word))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent e) {
        if ("appeal".equals(e.getModalId())) {
            String subject = Objects.requireNonNull(e.getValue("subject")).getAsString();
            String body = Objects.requireNonNull(e.getValue("body")).getAsString();
            String from = Objects.requireNonNull(e.getValue("from")).getAsString();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor(e.getUser().getName(), e.getUser().getEffectiveAvatarUrl());
            eb.setTitle(subject);
            eb.setColor(new Color(155, 160, 81));
            eb.addField("Warned From", from, false);
            eb.addField("Reason of Appeal", body, false);
            eb.setFooter("Provided by NCCBot.", "https://github.com/tcfplayz/images/blob/main/ncc.png?raw=true");
            eb.setTimestamp(Instant.now());

            TextChannel channel2 = MusicBot.bot.getTextChannelById(965803265235226634L);
            if (channel2 == null) return;
            channel2.sendMessageEmbeds(eb.build()).queue();

            e.reply("Thanks for your appeal request!").setEphemeral(true).queue();
        }
    }
}