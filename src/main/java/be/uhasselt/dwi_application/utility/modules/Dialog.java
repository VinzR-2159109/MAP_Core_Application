package be.uhasselt.dwi_application.utility.modules;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class Dialog {

    public static void showErrorDialog(String title, String header, String message, String buttonText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);

            ButtonType okButton = new ButtonType(buttonText);
            alert.getButtonTypes().setAll(okButton);

            alert.showAndWait();
        });
    }

    public static void showExceptionDialog(String title, Exception e) {
        showExceptionDialog(title, "Error occured", e);
    }

    public static void showExceptionDialog(String title, String header , Exception e) {
        Platform.runLater(() -> {
            String cause = (e.getCause() != null) ? e.getCause().toString() : "";
            String message = (e.getMessage() != null) ? e.getMessage() : "No message available";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(cause + "\n\n" + message);

            alert.showAndWait();
        });
    }


    public static void showErrorDialogWithChoice(
            String title,
            String header,
            String message,
            String confirmButtonText,
            String cancelButtonText,
            Runnable onConfirm) {

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);

            ButtonType confirmButton = new ButtonType(confirmButtonText);
            ButtonType cancelButton = new ButtonType(cancelButtonText, ButtonType.CANCEL.getButtonData());

            alert.getButtonTypes().setAll(confirmButton, cancelButton);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == confirmButton) {
                onConfirm.run();
            }
        });
    }

    public static void showInfoDialog(String title, String header, String message, String buttonText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(message);

            ButtonType okButton = new ButtonType(buttonText);
            alert.getButtonTypes().setAll(okButton);

            alert.showAndWait();
        });
    }


}
