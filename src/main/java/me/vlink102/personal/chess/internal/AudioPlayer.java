package me.vlink102.personal.chess.internal;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class AudioPlayer {

    public enum AudioLink {
        CAPTURE,
        CASTLE,
        GAME_END,
        GAME_START,
        ILLEGAL,
        MOVE_CHECK,
        MOVE_OPPONENT,
        MOVE_SELF,
        PREMOVE,
        PROMOTE,
        TEN_SECONDS
    }

    private Clip clip;
    private URL url;

    public AudioPlayer(URL url) {
        this.url = url;
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    protected synchronized void open() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        clip = AudioSystem.getClip();
        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            }
        });
        clip.open(AudioSystem.getAudioInputStream(url.openStream()));
    }

    public synchronized void play() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (clip == null || !clip.isRunning()) {
            open();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public synchronized void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.flush();
            dispose();
        }
    }

    public synchronized void dispose() {
        try {
            clip.close();
        } finally {
            clip = null;
        }
    }

}