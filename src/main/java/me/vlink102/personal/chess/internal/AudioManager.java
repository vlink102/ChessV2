package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class AudioManager {
    private final BoardGUI boardGUI;
    private final HashMap<AudioPlayer.AudioLink, File> audioFilesMap;

    private final HashMap<AudioPlayer.AudioLink, SingleAudioManager> playerMap;

    public AudioManager(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        this.audioFilesMap = new HashMap<>();
        this.playerMap = new HashMap<>();
        loadAudioMap();
    }

    public synchronized void play(AudioPlayer.AudioLink link) {
        playerMap.get(link).play();
    }

    public void loadAudioMap() {
        for (AudioPlayer.AudioLink value : AudioPlayer.AudioLink.values()) {
            audioFilesMap.put(value, Move.getFile("/sounds/default/" + value.toString().toLowerCase() + ".wav"));
        }
        for (AudioPlayer.AudioLink link : audioFilesMap.keySet()) {
            File file = audioFilesMap.get(link);
            try {
                playerMap.put(link, new SingleAudioManager(file.toURI().toURL()));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class SingleAudioManager {
        private final URL audioSource;

        public SingleAudioManager(URL audioSource) {
            this.audioSource = audioSource;
        }

        public synchronized void play() {
            AudioPlayer player = new AudioPlayer(audioSource);
            if (player.isPlaying()) {
                player.stop();
            } else {
                try {
                    player.play();
                } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
