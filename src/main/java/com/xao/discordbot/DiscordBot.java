package com.xao.discordbot;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.xao.discordbot.musicplayer.MusicBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.xao.discordbot.utilities.Utils.*;

public class DiscordBot extends ListenerAdapter {

    private static JDA build;
    private static Map<String, MusicBot> botMap;

    public static boolean noSkip = false;

    public static void main(String[] args) {

        botMap = new HashMap<>();

        build = JDABuilder.createDefault("ODE4MTgxMTU4MDE3NDMzNjMz.GQvfoE.Ijcp5khz4cF3GZJRgHTixnJmZeD5RLzh9eVu3I")
                .addEventListeners(new DiscordBot())
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES)
                .build();

        List<String> spotifyAudio = getSpotifyAudio("https://open.spotify.com/track/4UutBLYMby6whu2bMYh9Oa?si=7992e5d9868445ce");
        System.out.println("---------");
        System.out.println(spotifyAudio.get(0));
        System.out.println(spotifyAudio.get(1));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(".play")) {

        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String command = event.getName();

        if (command.equals("play")) {

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

            MusicBot bot = botMap.get(event.getGuild().getId());

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

        if (command.equals("skip")) {

            Guild guild = event.getGuild();

            if (guild == null) return;

            MusicBot bot = botMap.get(event.getGuild().getId());

            if (bot.getNumberOfTracks() == 0) {
                event.reply("Não há nenhuma música para ser skippada").queue();
                return;
            }

            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("**Baguncinha**");
            if (bot.getNumberOfTracks() > 1) {
                AudioTrack audioTrack = bot.getQueue().get(1);

                builder.setDescription("\nMúsica skippada.\n**Tocando agora:** _" + audioTrack.getInfo().title + "_");
            } else builder.setDescription("\nMúsica skippada.");

            if (guild.getId().equals("643498262363504664")) builder.setThumbnail(getRandomImageUrl());

            builder.setColor(Color.decode(getRandomColor()));

            MessageEmbed embed = builder.build();

            noSkip = true;

            bot.skip();

            noSkip = false;

            event.replyEmbeds(embed).queue();
        }

        if(command.equals("info")){

            Guild guild = event.getGuild();

            if (guild == null) return;

            MusicBot bot = botMap.get(event.getGuild().getId());

            if (bot.getNumberOfTracks() == 0) {
                event.reply("Não há nenhuma música tocando ou na fila.").queue();
                return;
            }

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
            builder.setDescription("\n**Música tocando agora:** _" + bot.getCurrentSong().getInfo().title + " - " + getMusicTime(bot.getCurrentSong().getDuration()) + "_\n\n**Fila (" + bot.getNumberOfTracks() + "):**\n" + String.join("\n", list));

            if (guild.getId().equals("643498262363504664")) builder.setThumbnail(getRandomImageUrl());

            builder.setColor(Color.decode(getRandomColor()));

            MessageEmbed embed = builder.build();

            event.replyEmbeds(embed).queue();
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandDataList = new ArrayList<>();
        commandDataList.add(Commands.slash("play", "Nao use esse comando").addOption(OptionType.STRING, "query", "A URL do video do youtube."));
        commandDataList.add(Commands.slash("skip", "Nao use esse comando"));
        commandDataList.add(Commands.slash("info", "Nao use esse comando"));
        event.getGuild().updateCommands().addCommands(commandDataList).queue();

        botMap.put(event.getGuild().getId(), new MusicBot(event.getGuild()));
    }

}