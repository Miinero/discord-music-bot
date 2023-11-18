package com.xao.discordbot.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.xao.discordbot.DiscordBot;
import com.xao.discordbot.musicplayer.MusicBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.xao.discordbot.utilities.Utils.*;
import static com.xao.discordbot.utilities.Utils.getRandomImageUrl;

public class PlaySlashCMD extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (!command.equals("play")) return;

        OptionMapping query = event.getOption("query");
        Guild guild = event.getGuild();

        if (query == null || guild == null) return;

        AudioManager audioManager = guild.getAudioManager();

        if (!audioManager.isConnected()) {
            GuildVoiceState voiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
            if (voiceState != null && voiceState.getChannel() != null)
                audioManager.openAudioConnection(voiceState.getChannel());
            else {
                event.reply("Você deve estar em um canal de voz para o bot se conectar").queue();
                return;
            }
        }

        String queryAsString = query.getAsString();

        String spotifyAudioName = null;

        if(isSpotifyURL(queryAsString)){
            List<String> spotifyAudio = getSpotifyAudio(queryAsString);

            if(spotifyAudio.isEmpty()){
                event.reply("Não foi possivel obter informações sobre esse audio, tente novamente!").queue();
                return;
            }

            queryAsString = spotifyAudio.get(1);
            spotifyAudioName = spotifyAudio.get(0);
        } else if (!isYouTubeURL(queryAsString)) {
            queryAsString = getFirstVideoUrl(queryAsString);
        }

        MusicBot bot = DiscordBot.botMap.get(event.getGuild().getId());

        bot.play(queryAsString);

        List<String> list = new ArrayList<>();

        int cache = 0;
        for (AudioTrack song : bot.getQueue()) {
            if (cache == 10) {
                list.add("...");
                break;
            }

            list.add("- _" + song.getInfo().title + " - " + getMusicTime(song.getDuration()) + "_");

            cache++;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("**Baguncinha**");
        builder.setDescription("Musica adicionada a fila porra.\n**Nome:** _" + (spotifyAudioName != null ? spotifyAudioName : bot.getQueue().get(bot.getQueue().size() - 1).getInfo().title) +
                " - " + getMusicTime(bot.getQueue().get(bot.getQueue().size() - 1).getDuration()) + "_\n\n**Fila (" + bot.getNumberOfTracks() + "):**\n" + String.join("\n", list));
        builder.setColor(Color.decode(getRandomColor()));

        if (guild.getId().equals("643498262363504664")) builder.setThumbnail(getRandomImageUrl());

        MessageEmbed embed = builder.build();

        event.replyEmbeds(embed).queue();
    }
}
