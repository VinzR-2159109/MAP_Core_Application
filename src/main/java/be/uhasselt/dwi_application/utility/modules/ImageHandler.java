package be.uhasselt.dwi_application.utility.modules;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class ImageHandler {
    private static final String DEFAULT_IMAGE_PATH = "resources/Image/default.png";

    public static Image uploadImage(Window ownerWindow) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(ownerWindow);
        if (file == null) {
            throw new IOException("No image was selected.");
        }

        return new Image(file.toURI().toString());
    }

    public static Image loadImage(String imagePath) {
        System.out.println("Loading image from: " + imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = null;
            try {
                imageFile = new File(new java.net.URI(imagePath));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString());
            }
        }

        File defaultImageFile = new File(DEFAULT_IMAGE_PATH);
        if (defaultImageFile.exists()) {
            System.out.println("Image not found at: " + imagePath);
            return new Image(defaultImageFile.toURI().toString());
        } else {
            System.err.println("Warning: Default image not found at " + DEFAULT_IMAGE_PATH);
            return null;
        }
    }
}
