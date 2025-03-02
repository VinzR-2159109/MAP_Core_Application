package be.uhasselt.dwi_application.controller.WorkInstruction.LocationPicker;

import be.uhasselt.dwi_application.controller.Controller;
import be.uhasselt.dwi_application.model.basic.Position;
import be.uhasselt.dwi_application.model.workInstruction.AssemblyInstruction;
import be.uhasselt.dwi_application.utility.database.repository.instruction.InstructionRepository;
import be.uhasselt.dwi_application.utility.database.repository.position.PositionRepository;
import be.uhasselt.dwi_application.utility.network.MjpegStreamReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class AssemblyLocationPickerController implements Controller {
    @FXML private TextField locationX1_field;
    @FXML private TextField locationX2_field;
    @FXML private TextField locationY1_field;
    @FXML private TextField locationY2_field;
    @FXML private Pane locationPicker_pane;
    @FXML private Pane gridPane;
    @FXML private ImageView videoStream_img;
    @FXML private Spinner<Integer> gridSize_spinner;

    private AssemblyInstruction assemblyInstruction;
    private double startX, startY;
    private Rectangle selectionRectangle;
    private MjpegStreamReader streamReader;
    
    private int gridSize;

    public AssemblyLocationPickerController(AssemblyInstruction assemblyInstruction) {
        this.assemblyInstruction = assemblyInstruction;
        this.gridSize = 40;
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

        gridSize_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 50, gridSize));
        gridSize_spinner.valueProperty().addListener((_, _, newValue) -> {
            gridSize = newValue;
            locationPicker_pane.getChildren().clear();
            assemblyInstruction.clearPositions();
            InstructionRepository.getInstance().clearPositionsOfAssemblyInstruction(assemblyInstruction);
            drawGrid();
        });

        List<Position> positions = PositionRepository.getInstance().getAllByInstructionId(assemblyInstruction.getId());

        if (!positions.isEmpty()) {
            double minX = positions.stream().mapToDouble(Position::getX).min().orElse(0);
            double minY = positions.stream().mapToDouble(Position::getY).min().orElse(0);
            double maxX = positions.stream().mapToDouble(Position::getX).max().orElse(0);
            double maxY = positions.stream().mapToDouble(Position::getY).max().orElse(0);

            double width = maxX - minX + gridSize;
            double height = maxY - minY + gridSize;

            locationX1_field.setText(String.format("%.0f", minX));
            locationX2_field.setText(String.format("%.0f", maxX));
            locationY1_field.setText(String.format("%.0f", minY));
            locationY2_field.setText(String.format("%.0f", maxY));

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

        startX = snapToGrid(event.getX());
        startY = snapToGrid(event.getY());

        if (selectionRectangle != null) {
            locationPicker_pane.getChildren().remove(selectionRectangle);
        }

        selectionRectangle = new Rectangle(startX, startY, 0, 0);
        selectionRectangle.setFill(Color.TRANSPARENT);
        selectionRectangle.setStroke(Color.BLUE);
        selectionRectangle.setStrokeWidth(2);
    }

    private void handleMouseDrag(MouseEvent event) {
        double endX = snapToGrid(event.getX());
        double endY = snapToGrid(event.getY());

        // Update rectangle size dynamically, snapped to the grid
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);

        selectionRectangle.setX(Math.min(startX, endX));
        selectionRectangle.setY(Math.min(startY, endY));
        selectionRectangle.setWidth(width);
        selectionRectangle.setHeight(height);

        // Ensure it's added only once
        if (!locationPicker_pane.getChildren().contains(selectionRectangle)) {
            locationPicker_pane.getChildren().add(selectionRectangle);
        }
    }

    private void handleMouseRelease(MouseEvent event) {
        double endX = snapToGrid(event.getX());
        double endY = snapToGrid(event.getY());

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
    }


    private double snapToGrid(double value) {
        return (Math.round(value / gridSize) * gridSize) + (gridSize / 2);
    }

    private void drawGrid() {
        gridPane.getChildren().clear();
        double width = gridPane.getPrefWidth();
        double height = gridPane.getPrefHeight();

        for (double x = 0; x < width; x += gridSize) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);
        }

        for (double y = 0; y < height; y += gridSize) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.LIGHTGRAY);
            line.setStrokeWidth(0.5);
            gridPane.getChildren().add(line);
        }
    }

    private void updateImageView(Image image) {
        Platform.runLater(() -> videoStream_img.setImage(image));
    }

    @Override
    public void cleanup() {
        streamReader.stop();
    }
}
