package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    public void handleSearch() {
        refreshTable();
    }

    @FXML
    public void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    public void handleAdd() {
        Artist artist = promptArtist(null);
        if (artist == null) {
            return;
        }
        artistService.createArtist(artist);
        refreshTable();
    }

    @FXML
    public void handleModify() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select an artist first.");
            return;
        }

        Artist artist = promptArtist(selected);
        if (artist == null) {
            return;
        }
        artistService.updateArtist(artist);
        refreshTable();
    }

    @FXML
    public void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Select an artist first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete artist");
        confirm.setHeaderText("Delete artist '" + selected.getName() + "'?");
        confirm.setContentText("This will remove the artist and related discipline links.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        artistService.deleteArtist(selected.getName());
        refreshTable();
    }

    private void refreshTable() {
        String query = searchField.getText();
        Discipline discipline = disciplineFilter.getValue();
        String disciplineName = discipline != null ? discipline.getName() : null;

        if ((query == null || query.isBlank()) && disciplineName == null) {
            artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
        } else {
            artistTable.setItems(FXCollections.observableArrayList(
                    artistService.searchArtists(query, disciplineName, null)));
        }
    }

    private Artist promptArtist(Artist existing) {
        String name = existing == null ? promptText("Artist name", null) : existing.getName();
        if (name == null || name.isBlank()) {
            return null;
        }

        String city = promptText("City", existing != null ? existing.getCity() : null);
        if (city == null) {
            return null;
        }
        String email = promptText("Email", existing != null ? existing.getContactEmail() : null);
        if (email == null) {
            return null;
        }
        Integer birthYear = promptInteger(existing != null && existing.getBirthYear() != null ? String.valueOf(existing.getBirthYear()) : null);
        if (birthYear == null) {
            return null;
        }
        String phone = promptText("Phone", existing != null ? existing.getPhone() : null);
        if (phone == null) {
            return null;
        }
        String website = promptText("Website", existing != null ? existing.getWebsite() : null);
        if (website == null) {
            return null;
        }
        String socialMedia = promptText("Social media", existing != null ? existing.getSocialMedia() : null);
        if (socialMedia == null) {
            return null;
        }
        String bio = promptText("Bio", existing != null ? existing.getBio() : null);
        if (bio == null) {
            return null;
        }
        String disciplinesText = promptText("Disciplines (comma separated)", existing != null ? existing.getDisciplines().stream().map(Discipline::getName).collect(Collectors.joining(", ")) : null);
        if (disciplinesText == null) {
            return null;
        }
        Boolean active = promptBoolean(existing == null || existing.isActive());
        if (active == null) {
            return null;
        }

        Artist artist = new Artist();
        artist.setName(name);
        artist.setCity(city);
        artist.setContactEmail(email);
        artist.setBirthYear(birthYear);
        artist.setPhone(phone);
        artist.setWebsite(website);
        artist.setSocialMedia(socialMedia);
        artist.setBio(bio);
        artist.setActive(active);
        artist.setDisciplines(parseDisciplines(disciplinesText));
        return artist;
    }

    private List<Discipline> parseDisciplines(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        List<Discipline> disciplines = new ArrayList<>();
        for (String part : text.split(",")) {
            String name = part.trim();
            if (!name.isBlank()) {
                disciplines.add(new Discipline(name));
            }
        }
        return disciplines;
    }

    private String promptText(String title, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue == null ? "" : defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private Integer promptInteger(String defaultValue) {
        String value = promptText("Birth year", defaultValue);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            showWarning("Please enter a valid birth year.");
            return null;
        }
    }

    private Boolean promptBoolean(boolean defaultValue) {
        List<String> choices = List.of("true", "false");
        javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(String.valueOf(defaultValue), choices);
        dialog.setTitle("Active");
        dialog.setHeaderText("Active");
        Optional<String> result = dialog.showAndWait();
        return result.map(Boolean::parseBoolean).orElse(null);
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
