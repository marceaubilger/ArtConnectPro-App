package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ExhibitionDao.
 */
public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        String sql = """
                SELECT e.name, e.start_date, e.end_date, e.description,
                       g.name AS gallery_name, g.address, g.owner_name, g.opening_hours, g.contact_phone, g.rating, g.website
                FROM exhibition e
                LEFT JOIN gallery g ON g.id = e.gallery_id
                ORDER BY e.start_date DESC, e.name
                """;

        List<Exhibition> exhibitions = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Exhibition exhibition = mapExhibition(resultSet);
                exhibition.setArtworks(loadExhibitionArtworks(connection, exhibition.getTitle()));
                exhibitions.add(exhibition);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load exhibitions.", e);
        }

        return exhibitions;
    }

    @Override
    public void save(Exhibition exhibition) {
        String sql = """
                INSERT INTO exhibition (name, start_date, end_date, gallery_id, description)
                VALUES (?, ?, ?, (SELECT id FROM gallery WHERE name = ?), ?)
                """;

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                bindExhibition(statement, exhibition);
                statement.executeUpdate();
            }

            saveArtworkLinks(connection, exhibition);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save exhibition.", e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String sql = """
                UPDATE exhibition
                SET start_date = ?, end_date = ?, gallery_id = (SELECT id FROM gallery WHERE name = ?), description = ?
                WHERE name = ?
                """;

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, exhibition.getStartDate() == null ? null : Timestamp.valueOf(exhibition.getStartDate().atStartOfDay()));
                statement.setTimestamp(2, exhibition.getEndDate() == null ? null : Timestamp.valueOf(exhibition.getEndDate().atStartOfDay()));
                statement.setString(3, exhibition.getGallery() == null ? null : exhibition.getGallery().getName());
                statement.setString(4, exhibition.getDescription());
                statement.setString(5, exhibition.getTitle());
                statement.executeUpdate();
            }

            try (PreparedStatement deleteLinks = connection.prepareStatement(
                    "DELETE FROM exhibition_artwork WHERE exhibition_name = ?")) {
                deleteLinks.setString(1, exhibition.getTitle());
                deleteLinks.executeUpdate();
            }

            saveArtworkLinks(connection, exhibition);

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update exhibition.", e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteLinks = connection.prepareStatement(
                    "DELETE FROM exhibition_artwork WHERE exhibition_name = ?")) {
                deleteLinks.setString(1, title);
                deleteLinks.executeUpdate();
            }

            try (PreparedStatement deleteExhibition = connection.prepareStatement(
                    "DELETE FROM exhibition WHERE name = ?")) {
                deleteExhibition.setString(1, title);
                deleteExhibition.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete exhibition.", e);
        }
    }

    private Exhibition mapExhibition(ResultSet resultSet) throws SQLException {
        Exhibition exhibition = new Exhibition();
        exhibition.setTitle(resultSet.getString("name"));

        Timestamp start = resultSet.getTimestamp("start_date");
        exhibition.setStartDate(start == null ? null : start.toLocalDateTime().toLocalDate());

        Timestamp end = resultSet.getTimestamp("end_date");
        exhibition.setEndDate(end == null ? null : end.toLocalDateTime().toLocalDate());

        String description = resultSet.getString("description");
        exhibition.setDescription(description);
        exhibition.setTheme(description);

        String galleryName = resultSet.getString("gallery_name");
        if (galleryName != null && !galleryName.isBlank()) {
            Gallery gallery = new Gallery();
            gallery.setName(galleryName);
            gallery.setAddress(resultSet.getString("address"));
            gallery.setOwnerName(resultSet.getString("owner_name"));
            gallery.setOpeningHours(resultSet.getString("opening_hours"));
            gallery.setContactPhone(resultSet.getString("contact_phone"));
            gallery.setRating(resultSet.getDouble("rating"));
            gallery.setWebsite(resultSet.getString("website"));
            exhibition.setGallery(gallery);
        }

        return exhibition;
    }

    private List<Artwork> loadExhibitionArtworks(Connection connection, String exhibitionTitle) throws SQLException {
        String sql = """
                SELECT a.title, a.creation_year, a.type, a.medium, a.dimensions, a.description, a.price, a.status
                FROM exhibition_artwork ea
                INNER JOIN artwork a ON a.title = ea.artwork_title
                WHERE ea.exhibition_name = ?
                ORDER BY a.title
                """;

        List<Artwork> artworks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, exhibitionTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Artwork artwork = new Artwork();
                    artwork.setTitle(resultSet.getString("title"));

                    int creationYear = resultSet.getInt("creation_year");
                    artwork.setCreationYear(resultSet.wasNull() ? null : creationYear);

                    artwork.setType(resultSet.getString("type"));
                    artwork.setMedium(resultSet.getString("medium"));
                    artwork.setDimensions(resultSet.getString("dimensions"));
                    artwork.setDescription(resultSet.getString("description"));
                    artwork.setPrice(resultSet.getDouble("price"));

                    String status = resultSet.getString("status");
                    if (status != null && !status.isBlank()) {
                        artwork.setStatus(Artwork.Status.valueOf(status));
                    }

                    artworks.add(artwork);
                }
            }
        }

        return artworks;
    }

    private void bindExhibition(PreparedStatement statement, Exhibition exhibition) throws SQLException {
        statement.setString(1, exhibition.getTitle());
        statement.setTimestamp(2, exhibition.getStartDate() == null ? null : Timestamp.valueOf(exhibition.getStartDate().atStartOfDay()));
        statement.setTimestamp(3, exhibition.getEndDate() == null ? null : Timestamp.valueOf(exhibition.getEndDate().atStartOfDay()));
        statement.setString(4, exhibition.getGallery() == null ? null : exhibition.getGallery().getName());
        statement.setString(5, exhibition.getDescription());
    }

    private void saveArtworkLinks(Connection connection, Exhibition exhibition) throws SQLException {
        List<Artwork> artworks = exhibition.getArtworks();
        if (artworks == null || artworks.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO exhibition_artwork (exhibition_name, artwork_title) VALUES (?, ?)";
        for (Artwork artwork : artworks) {
            if (artwork != null && artwork.getTitle() != null && !artwork.getTitle().isBlank()) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, exhibition.getTitle());
                    statement.setString(2, artwork.getTitle());
                    statement.executeUpdate();
                }
            }
        }
    }
}

