package com.xao.discordbot.musicplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MusicBot {
    private final DefaultAudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final List<AudioTrack> queue;
    private final Guild guild;
    private AudioTrack currentSong;


    public MusicBot(Guild guild) {
        this.guild = guild;

        this.playerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(playerManager);

        this.player = playerManager.createPlayer();
        this.player.setVolume(100);

        guild.getAudioManager().setSendingHandler(new AudioPlayerHandler(player));
        this.queue = new ArrayList<>();

        AudioListener listener = new AudioListener(this);
        this.player.addListener(listener);
    }

    public boolean play(String url){
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean addedToQueue = new AtomicBoolean(false);

        playerManager.loadItem(url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                queue.add(track);
                if(currentSong == null){
                    currentSong = track;
                    playNextSong();
                }
                addedToQueue.set(true);
                latch.countDown();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                queue.addAll(playlist.getTracks());
                if(currentSong == null) playNextSong();
                addedToQueue.set(true);
                latch.countDown();
            }

            @Override
            public void noMatches() {
                addedToQueue.set(true);
                latch.countDown();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                addedToQueue.set(true);
                latch.countDown();
            }
        });

        // Espera até que a URL seja processada ou que ocorra um erro
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Processo interrompido");
        }

        return addedToQueue.get();
    }

    public void playNextSong() {

        if (queue.isEmpty()) {
            currentSong = null;
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                if(!queue.isEmpty()) return;

                guild.getAudioManager().closeAudioConnection();
            };
            executor.schedule(task, 180, TimeUnit.SECONDS);
            executor.shutdown();
            return;
        }

        AudioTrack nextTrack = queue.get(0);
        player.playTrack(nextTrack);

        currentSong = nextTrack;
    }

    public void skip(){
        queue.remove(0);

        if (queue.isEmpty()) {
            player.stopTrack();
            currentSong = null;
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                if(!queue.isEmpty()) return;

                guild.getAudioManager().closeAudioConnection();
            };
            executor.schedule(task, 180, TimeUnit.SECONDS);
            executor.shutdown();
            return;
        }

        AudioTrack nextTrack = queue.get(0);
        player.playTrack(nextTrack);

        currentSong = nextTrack;
    }

    public void stop() {
        player.stopTrack();
    }

    public int getNumberOfTracks() {
        return this.queue.size();
    }

    public List<AudioTrack> getQueue(){
        return this.queue;
    }

    public AudioTrack getCurrentSong(){
        return currentSong;
    }

}

