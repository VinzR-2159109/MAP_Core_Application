package be.uhasselt.dwi_application.controller.WorkInstruction.LocationPicker;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.controller.MainController;
import be.uhasselt.dwi_application.controller.WorkInstruction.Manager.InstructionManagerController;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.workInstruction.Assembly;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.FxmlViews;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import be.uhasselt.dwi_application.utility.database.repository.position.PositionRepository;
import be.uhasselt.dwi_application.utility.database.repository.settings.Settings;
import be.uhasselt.dwi_application.utility.database.repository.settings.SettingsRepository;
import be.uhasselt.dwi_application.utility.network.MjpegStreamReader;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static be.uhasselt.dwi_application.utility.modules.Dialog.showExceptionDialog;

public class AssemblyLocationPickerController implements Controller {
    @FXML private TextField locationX1_field;
    @FXML private TextField locationX2_field;
    @FXML private TextField locationY1_field;
    @FXML private TextField locationY2_field;
    @FXML private Pane locationPicker_pane;
    @FXML private Pane gridPane;
    @FXML private ImageView videoStream_img;
    @FXML private Spinner<Integer> gridSize_spinner;
    @FXML private Button save_btn;

    private final AssemblyInstruction assemblyInstruction;
    private final Assembly assembly;
    private double startX, startY;
    private Rectangle selectionRectangle;
    private MjpegStreamReader streamReader;
    private int gridSize;
    private int videoFeedEnlargement = 2;

    public AssemblyLocationPickerController(Assembly assembly, AssemblyInstruction assemblyInstruction) {
        this.assembly = assembly;
        this.assemblyInstruction = assemblyInstruction;
    }

    @FXML
    public void initialize() {
        System.out.println("---Initializing AssemblyLocationPicker---");
        locationPicker_pane.translateXProperty().bind(videoStream_img.layoutXProperty());
        locationPicker_pane.translateYProperty().bind(videoStream_img.layoutYProperty());
        locationPicker_pane.prefWidthProperty().bind(videoStream_img.fitWidthProperty());
        locationPicker_pane.prefHeightProperty().bind(videoStream_img.fitHeightProperty());

        gridPane.translateXProperty().bind(videoStream_img.layoutXProperty());
        gridPane.translateYProperty().bind(videoStream_img.layoutYProperty());
        gridPane.prefWidthProperty().bind(videoStream_img.fitWidthProperty());
        gridPane.prefHeightProperty().bind(videoStream_img.fitHeightProperty());

        locationPicker_pane.setOnMousePressed(this::handleMousePress);
        locationPicker_pane.setOnMouseDragged(this::handleMouseDrag);
        locationPicker_pane.setOnMouseReleased(this::handleMouseRelease);

        save_btn.setOnAction(this::handleSaveBtn);

        gridSize = SettingsRepository.loadSettings().getGridSize();
        gridSize_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 50, gridSize));
        gridSize_spinner.valueProperty().addListener((_, _, newValue) -> {
            gridSize = newValue;
            Settings settings = SettingsRepository.loadSettings();
            settings.setGridSize(newValue);
            SettingsRepository.updateSettings(settings);

            locationPicker_pane.getChildren().clear();
            assemblyInstruction.clearPositions();
            InstructionRepository.getInstance().clearPositionsOfAssemblyInstruction(assemblyInstruction);
            drawGrid();
        });

        List<Position> positions = PositionRepository.getInstance().getAllByInstructionId(assemblyInstruction.getId()).orElseGet(ArrayList::new);
        System.out.println("Loading positions: " + positions);

        if (!positions.isEmpty()) {
            double minX = (positions.stream().mapToDouble(Position::getX).min().orElse(0)) * videoFeedEnlargement;
            double minY = (positions.stream().mapToDouble(Position::getY).min().orElse(0)) * videoFeedEnlargement;
            double maxX = snapToGrid(positions.stream().mapToDouble(Position::getX).max().orElse(0)) * videoFeedEnlargement;
            double maxY = snapToGrid(positions.stream().mapToDouble(Position::getY).max().orElse(0)) * videoFeedEnlargement;

            double width = maxX - minX;
            double height = maxY - minY;

            locationX1_field.setText(String.format("%.0f", minX / videoFeedEnlargement));
            locationX2_field.setText(String.format("%.0f", maxX / videoFeedEnlargement));
            locationY1_field.setText(String.format("%.0f", minY / videoFeedEnlargement));
            locationY2_field.setText(String.format("%.0f", maxY / videoFeedEnlargement));

            Rectangle rect = new Rectangle(minX, minY, width, height);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.BLUE);
            rect.setStrokeWidth(2);

            locationPicker_pane.getChildren().add(rect);
        }

        streamReader = new MjpegStreamReader("http://127.0.0.1:5000/video_feed");
        streamReader.start(this::updateImageView);
        drawGrid();
    }

    private void handleMousePress(MouseEvent event) {
        locationPicker_pane.getChildren().clear();
        assemblyInstruction.clearPositions();
        InstructionRepository.getInstance().clearPositionsOfAssemblyInstruction(assemblyInstruction);

        startX = snapToGrid(event.getX()) / videoFeedEnlargement;
        startY = snapToGrid(event.getY()) / videoFeedEnlargement;

        if (selectionRectangle != null) {
            locationPicker_pane.getChildren().remove(selectionRectangle);
        }

        selectionRectangle = new Rectangle(startX * videoFeedEnlargement, startY * videoFeedEnlargement, 0, 0);
        selectionRectangle.setFill(Color.TRANSPARENT);
        selectionRectangle.setStroke(Color.BLUE);
        selectionRectangle.setStrokeWidth(2);
    }

    private void handleMouseDrag(MouseEvent event) {
        double endX = snapToGrid(event.getX());
        double endY = snapToGrid(event.getY());

        // Update rectangle size dynamically, snapped to the grid
        double width = Math.abs(endX - startX * videoFeedEnlargement);
        double height = Math.abs(endY - startY * videoFeedEnlargement);

        selectionRectangle.setX(Math.min(startX * videoFeedEnlargement, endX));
        selectionRectangle.setY(Math.min(startY * videoFeedEnlargement, endY));
        selectionRectangle.setWidth(width);
        selectionRectangle.setHeight(height);

        // Ensure it's added only once
        if (!locationPicker_pane.getChildren().contains(selectionRectangle)) {
            locationPicker_pane.getChildren().add(selectionRectangle);
        }
    }

    private void handleMouseRelease(MouseEvent event) {
        double endX = snapToGrid(event.getX()) / videoFeedEnlargement;
        double endY = snapToGrid(event.getY()) / videoFeedEnlargement;

        double minX = Math.min(startX, endX);
        double minY = Math.min(startY, endY);
        double maxX = Math.max(startX, endX);
        double maxY = Math.max(startY, endY);

        // Update UI with top-left corner
        locationX1_field.setText(String.format("%.0f", minX));
        locationX2_field.setText(String.format("%.0f", endX));
        locationY1_field.setText(String.format("%.0f", minY));
        locationY2_field.setText(String.format("%.0f", endY));

        List<Position> newPositions = new ArrayList<>();

        for (double x = minX; x <= maxX; x += gridSize) {
            for (double y = minY; y <= maxY; y += gridSize) {
                newPositions.add(new Position(x, y));
            }
        }

        List<Long> positionIds = PositionRepository.getInstance().addPositionsBatch(newPositions);
        for (int i = 0; i < newPositions.size(); i++) {
            newPositions.get(i).setId(positionIds.get(i));
        }

        assemblyInstruction.addPositions(newPositions);
        InstructionRepository.getInstance().updateInstruction(assemblyInstruction);
        System.out.println("Adding new positions: " + newPositions);
    }


    private double snapToGrid(double value) {
        return (Math.round(value / gridSize) * gridSize);
    }

    public void drawGrid() {
        gridPane.getChildren().clear();

        double width = videoStream_img.getFitWidth();
        double height = videoStream_img.getFitHeight();

        for (double x = 0; x < width; x += gridSize) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);

            Label labelX = new Label(String.valueOf((int) x / videoFeedEnlargement));
            labelX.setTextFill(Color.DARKGRAY);
            double finalX = x;
            labelX.widthProperty().addListener((obs, oldWidth, newWidth) ->
                    labelX.setLayoutX(finalX + gridSize / 2.0 - newWidth.doubleValue() / 2.0)
            );
            gridPane.getChildren().add(labelX);
        }

        for (double y = 0; y < height; y += gridSize) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);

            Label labelY = new Label(String.valueOf((int) y / videoFeedEnlargement));
            labelY.setTextFill(Color.DARKGRAY);
            labelY.setLayoutX(2);
            double finalY = y;
            labelY.heightProperty().addListener((obs, oldHeight, newHeight) ->
                    labelY.setLayoutY(finalY + gridSize / 2.0 - newHeight.doubleValue() / 2.0)
            );
            gridPane.getChildren().add(labelY);
        }
    }


    private void updateImageView(Image image) {
        Platform.runLater(() -> {
            videoStream_img.setImage(image);
            videoStream_img.setPreserveRatio(true);

            double newWidth = image.getWidth() * videoFeedEnlargement;
            double newHeight = image.getHeight() * videoFeedEnlargement;

            videoStream_img.setFitWidth(newWidth);
            videoStream_img.setFitHeight(newHeight);

            drawGrid();
        });
    }

    private void handleSaveBtn(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FxmlViews.INSTRUCTION_CREATER));
            loader.setControllerFactory(_-> new InstructionManagerController(assembly));
            Parent instructionView = loader.load();
            Controller controller = loader.getController();

            MainController.getInstance().setContentView(instructionView, controller);
        } catch (Exception e) {
            e.printStackTrace();
            showExceptionDialog("Fault in handleTileClick", e);
        }
    }

    @Override
    public void cleanup() {
        streamReader.stop();
    }
}
