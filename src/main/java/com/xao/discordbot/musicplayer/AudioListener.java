package com.xao.discordbot.musicplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.xao.discordbot.DiscordBot;

public class AudioListener extends AudioEventAdapter {

    private final MusicBot bot;

    public AudioListener(MusicBot bot) {
        this.bot = bot;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(DiscordBot.noSkip) return;

        System.out.println("removeu musica, size: " + bot.getQueue().size());
        bot.getQueue().remove(0);
        bot.playNextSong();
    }
}
