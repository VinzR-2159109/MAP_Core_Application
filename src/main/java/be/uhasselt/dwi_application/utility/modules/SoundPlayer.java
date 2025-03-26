package be.uhasselt.dwi_application.utility.modules;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class SoundPlayer {
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
            mediaPlayer.stop();
            mediaPlayer.dispose();
        });
    }

    public static void play(SoundType soundType) {
        SoundPlayer soundPlayer = new SoundPlayer(soundType);
        soundPlayer.play();
    }

    private void play() {
        mediaPlayer.stop();
        mediaPlayer.play();
    }

    private String getMediaURI(String path) {
        if (!path.startsWith("http")) {
            return new File(path).toURI().toString();
        }
        return path;
    }
}
