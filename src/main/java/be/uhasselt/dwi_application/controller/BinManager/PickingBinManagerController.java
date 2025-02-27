package be.uhasselt.dwi_application.controller.BinManager;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.picking.PickingBin;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.pickingBin.BinRepository;
import be.uhasselt.dwi_application.utility.network.MjpegStreamReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static be.uhasselt.dwi_application.utility.LoadView.getFXMLLoader;

public class PickingBinManagerController implements Initializable, Controller {
    @FXML private Pane markerOverlayPane;
    @FXML private ImageView videoStream_img;
    @FXML private VBox bins_vbox;
    @FXML private Button addBin_btn;

    private final Map<PickingBin, Circle> binMarkers = new HashMap<>();
    private final Map<PickingBin, BinItemController> binControllers = new HashMap<>();
    private MjpegStreamReader streamReader;
    private final BinRepository binRepository = new BinRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addBin_btn.setOnMouseClicked(_ -> addNewBin());

        startVideoStream();
        updateBinsUI();
    }

    private void addNewBin() {
        List<PickingBin> pickingBins = binRepository.getAll();
        PickingBin newBin = new PickingBin(0,0, (long) pickingBins.size());

        binRepository.add(newBin);
        updateBinsUI();
    }

    public void deleteBin(PickingBin bin) {
        binRepository.delete(bin);

        updateBinsUI();
    }

    private static final double SCALE_FACTOR = 2.0;
    private void updateBinsUI() {
        bins_vbox.getChildren().clear();

        List<PickingBin> pickingBins = binRepository.getAll();

        markerOverlayPane.getChildren().clear();

        for (PickingBin bin : pickingBins) {
            try {
                FXMLLoader loader = getFXMLLoader(FxmlViews.BIN_ITEM);
                VBox binBox = loader.load();

                BinItemController controller = loader.getController();
                controller.setBin(bin, this);
                binControllers.put(bin, controller);

                bins_vbox.getChildren().add(binBox);

                Circle marker = new Circle(15, Color.RED);
                Label binLabel = new Label(String.valueOf(bin.getId()));
                binLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                StackPane markerStack = new StackPane(marker, binLabel);
                markerStack.setTranslateX(bin.getPos_x() * SCALE_FACTOR);
                markerStack.setTranslateY(bin.getPos_y() * SCALE_FACTOR);
                final double[] mouseAnchorX = new double[1];
                final double[] mouseAnchorY = new double[1];

                markerStack.setOnMousePressed(event -> {
                    mouseAnchorX[0] = event.getSceneX() - markerStack.getTranslateX();
                    mouseAnchorY[0] = event.getSceneY() - markerStack.getTranslateY();
                });

                markerStack.setOnMouseDragged(event -> {
                    markerStack.setTranslateX(event.getSceneX() - mouseAnchorX[0]);
                    markerStack.setTranslateY(event.getSceneY() - mouseAnchorY[0]);
                });

                markerStack.setOnMouseReleased(event -> {
                    int newX = (int) (markerStack.getTranslateX() / SCALE_FACTOR);
                    int newY = (int) (markerStack.getTranslateY() / SCALE_FACTOR);
                    bin.setPosition(newX, newY);

                    BinItemController binController = binControllers.get(bin);
                    binController.updatePositionFields(newX, newY);
                });

                binMarkers.put(bin, marker);
                markerOverlayPane.getChildren().add(markerStack);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startVideoStream() {
        if (streamReader == null) {
            String STREAM_URL = "http://127.0.0.1:5000/video_feed";
            streamReader = new MjpegStreamReader(STREAM_URL);
            streamReader.start(this::updateImageView);
        }
    }

    private void updateImageView(Image image) {
        Platform.runLater(() -> videoStream_img.setImage(image));
    }

    public void stopStream() {
        System.out.println("<Stopping stream>");
        if (streamReader != null) {
            streamReader.stop();
        }
    }

    @Override
    public void cleanup() {
        stopStream();
    }
}
