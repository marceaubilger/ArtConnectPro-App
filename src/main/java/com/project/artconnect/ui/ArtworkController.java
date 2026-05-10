package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtworkController {
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        refreshTable();
    }

    @FXML
    public void handleAdd() {
        Artwork artwork = promptArtwork(null);
        if (artwork == null) {
            return;
        }
        artworkService.createArtwork(artwork);
        refreshTable();
    }

    @FXML
    public void handleModify() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select an artwork first.");
            return;
        }

        Artwork artwork = promptArtwork(selected);
        if (artwork == null) {
            return;
        }
        artworkService.updateArtwork(artwork);
        refreshTable();
    }

    @FXML
    public void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select an artwork first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete artwork");
        confirm.setHeaderText("Delete artwork '" + selected.getTitle() + "'?");
        confirm.setContentText("This will remove the artwork and related tag links.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        artworkService.deleteArtwork(selected.getTitle());
        refreshTable();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private Artwork promptArtwork(Artwork existing) {
        String title = existing == null ? promptText("Title", null) : existing.getTitle();
        if (title == null || title.isBlank()) {
            return null;
        }
        Integer creationYear = promptInteger(existing != null && existing.getCreationYear() != null ? String.valueOf(existing.getCreationYear()) : null);
        if (creationYear == null) {
            return null;
        }
        String type = promptText("Type", existing != null ? existing.getType() : null);
        if (type == null) {
            return null;
        }
        String medium = promptText("Medium", existing != null ? existing.getMedium() : null);
        if (medium == null) {
            return null;
        }
        String dimensions = promptText("Dimensions", existing != null ? existing.getDimensions() : null);
        if (dimensions == null) {
            return null;
        }
        String description = promptText("Description", existing != null ? existing.getDescription() : null);
        if (description == null) {
            return null;
        }
        Double price = promptDouble(existing != null ? String.valueOf(existing.getPrice()) : null);
        if (price == null) {
            return null;
        }
        ChoiceDialog<Artwork.Status> statusDialog = new ChoiceDialog<>(existing != null && existing.getStatus() != null ? existing.getStatus() : Artwork.Status.FOR_SALE, List.of(Artwork.Status.values()));
        statusDialog.setTitle("Status");
        statusDialog.setHeaderText("Status");
        Optional<Artwork.Status> statusResult = statusDialog.showAndWait();
        if (statusResult.isEmpty()) {
            return null;
        }
        String artistName = promptText("Artist name", existing != null && existing.getArtist() != null ? existing.getArtist().getName() : null);
        if (artistName == null) {
            return null;
        }
        Artist artist = artistService.getArtistByName(artistName).orElse(null);
        if (artist == null) {
            showWarning("Artist '" + artistName + "' does not exist. Create the artist first.");
            return null;
        }
        String tagsText = promptText("Tags (comma separated)", existing != null ? existing.getTags().stream().map(ArtworkTag::getName).collect(Collectors.joining(", ")) : null);
        if (tagsText == null) {
            return null;
        }

        Artwork artwork = new Artwork();
        artwork.setTitle(title);
        artwork.setCreationYear(creationYear);
        artwork.setType(type);
        artwork.setMedium(medium);
        artwork.setDimensions(dimensions);
        artwork.setDescription(description);
        artwork.setPrice(price);
        artwork.setStatus(statusResult.get());
        artwork.setArtist(artist);
        artwork.setTags(parseTags(tagsText));
        return artwork;
    }


    private List<ArtworkTag> parseTags(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        List<ArtworkTag> tags = new ArrayList<>();
        for (String part : text.split(",")) {
            String name = part.trim();
            if (!name.isBlank()) {
                tags.add(new ArtworkTag(name));
            }
        }
        return tags;
    }

    private String promptText(String title, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private Integer promptInteger(String defaultValue) {
        String value = promptText("Creation year", defaultValue);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showWarning("Please enter a valid creation year.");
            return null;
        }
    }

    private Double promptDouble(String defaultValue) {
        String value = promptText("Price", defaultValue);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            showWarning("Please enter a valid price.");
            return null;
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
