package fun.spmc.radio;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class Utilities {
    public static @NotNull String getDuration(@NotNull Duration d) {
        int m = d.toMinutesPart();
        int s = d.toSecondsPart();
        int h = d.toHoursPart();

        if (h == 0) return String.format("%02d:%02d", m, s);
        else return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static @NotNull MessageEmbed appendEmbed(@NotNull EmbedBuilder builder) {
        builder.setImage("https://media1.tenor.com/images/b3b66ace65470cba241193b62366dfee/tenor.gif");
        builder.setColor(new Color(2600572));
        builder.setFooter("SPMCRadio 2.5.6");
        builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));
        return builder.build();
    }
}
