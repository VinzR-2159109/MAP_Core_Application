package be.uhasselt.dwi_application.utility.modules;

import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SoundPlayer {
    private static final Set<MediaPlayer> activePlayers = new HashSet<>();
    private final MediaPlayer mediaPlayer;

    public enum SoundType {
        OK("MAP_OK_Sound.wav");

        private final String filename;

        SoundType(String filename) {
            this.filename = filename;
        }

        public String getPath() {
            return "src/main/resources/Sound/" + filename;
        }
    }

    private SoundPlayer(SoundType soundType) {
        Media media = new Media(getMediaURI(soundType.getPath()));
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose();
            activePlayers.remove(mediaPlayer);
        });

        mediaPlayer.setOnError(() -> {
            System.err.println("Media error: " + mediaPlayer.getError());
            activePlayers.remove(mediaPlayer);
        });
    }

    public static void play(SoundType soundType) {
        if (Platform.isFxApplicationThread()) {
            createAndPlay(soundType);
        } else {
            Platform.runLater(() -> createAndPlay(soundType));
        }
    }

    private static void createAndPlay(SoundType soundType) {
        SoundPlayer soundPlayer = new SoundPlayer(soundType);
        activePlayers.add(soundPlayer.mediaPlayer);
        soundPlayer._play();
    }

    private void _play() {
        mediaPlayer.play();
    }

    private String getMediaURI(String path) {
        return new File(path).toURI().toString();
    }
}
