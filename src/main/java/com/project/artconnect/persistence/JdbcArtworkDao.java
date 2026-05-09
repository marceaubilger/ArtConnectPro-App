package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        String sql = "SELECT title, creation_year, type, medium, dimensions, description, price, status, artist_name FROM artwork ORDER BY title";
        List<Artwork> artworks = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
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

                String artistName = resultSet.getString("artist_name");
                if (artistName != null && !artistName.isBlank()) {
                    String artistSql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist WHERE name = ?";
                    try (PreparedStatement artistStmt = connection.prepareStatement(artistSql)) {
                        artistStmt.setString(1, artistName);
                        try (ResultSet artistResult = artistStmt.executeQuery()) {
                            if (artistResult.next()) {
                                Artist artist = new Artist();
                                artist.setName(artistResult.getString("name"));
                                artist.setBio(artistResult.getString("bio"));
                                int artistBirthYear = artistResult.getInt("birth_year");
                                artist.setBirthYear(artistResult.wasNull() ? null : artistBirthYear);
                                artist.setContactEmail(artistResult.getString("contact_email"));
                                artist.setPhone(artistResult.getString("phone"));
                                artist.setCity(artistResult.getString("city"));
                                artist.setWebsite(artistResult.getString("website"));
                                artist.setSocialMedia(artistResult.getString("social_media"));
                                artist.setActive(artistResult.getBoolean("is_active"));

                                String discSql = "SELECT d.name FROM discipline d INNER JOIN artist_discipline ad ON ad.discipline_name = d.name WHERE ad.artist_name = ? ORDER BY d.name";
                                try (PreparedStatement discStmt = connection.prepareStatement(discSql)) {
                                    discStmt.setString(1, artist.getName());
                                    try (ResultSet discResult = discStmt.executeQuery()) {
                                        List<Discipline> disciplines = new ArrayList<>();
                                        while (discResult.next()) {
                                            disciplines.add(new Discipline(discResult.getString("name")));
                                        }
                                        artist.setDisciplines(disciplines);
                                    }
                                }
                                artwork.setArtist(artist);
                            } else {
                                Artist artist = new Artist();
                                artist.setName(artistName);
                                artwork.setArtist(artist);
                            }
                        }
                    }
                }

                String tagSql = "SELECT t.name FROM artwork_tag t INNER JOIN artwork_artwork_tag at ON at.tag_name = t.name WHERE at.artwork_title = ? ORDER BY t.name";
                try (PreparedStatement tagStmt = connection.prepareStatement(tagSql)) {
                    tagStmt.setString(1, artwork.getTitle());
                    try (ResultSet tagResult = tagStmt.executeQuery()) {
                        List<ArtworkTag> tags = new ArrayList<>();
                        while (tagResult.next()) {
                            tags.add(new ArtworkTag(tagResult.getString("name")));
                        }
                        artwork.setTags(tags);
                    }
                }

                artworks.add(artwork);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load artworks from database.", e);
        }
        return artworks;
    }

    @Override
    public void save(Artwork artwork) {
        String sql = "INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, artwork.getTitle());
                if (artwork.getCreationYear() == null) {
                    statement.setNull(2, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(2, artwork.getCreationYear());
                }
                statement.setString(3, artwork.getType());
                statement.setString(4, artwork.getMedium());
                statement.setString(5, artwork.getDimensions());
                statement.setString(6, artwork.getDescription());
                statement.setDouble(7, artwork.getPrice());
                statement.setString(8, artwork.getStatus() == null ? null : artwork.getStatus().name());
                statement.setString(9, artwork.getArtist() == null ? null : artwork.getArtist().getName());
                statement.executeUpdate();
            }

            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM artwork_artwork_tag WHERE artwork_title = ?")) {
                delete.setString(1, artwork.getTitle());
                delete.executeUpdate();
            }

            List<ArtworkTag> tags = artwork.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (ArtworkTag tag : tags) {
                    if (tag != null && tag.getName() != null && !tag.getName().isBlank()) {
                        try (PreparedStatement ensureTag = connection.prepareStatement(
                                "INSERT IGNORE INTO artwork_tag (name) VALUES (?)")) {
                            ensureTag.setString(1, tag.getName());
                            ensureTag.executeUpdate();
                        }

                        try (PreparedStatement link = connection.prepareStatement(
                                "INSERT INTO artwork_artwork_tag (artwork_title, tag_name) VALUES (?, ?)")) {
                            link.setString(1, artwork.getTitle());
                            link.setString(2, tag.getName());
                            link.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save artwork.", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String sql = "UPDATE artwork SET creation_year = ?, type = ?, medium = ?, dimensions = ?, description = ?, price = ?, status = ?, artist_name = ? WHERE title = ?";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (artwork.getCreationYear() == null) {
                    statement.setNull(1, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(1, artwork.getCreationYear());
                }
                statement.setString(2, artwork.getType());
                statement.setString(3, artwork.getMedium());
                statement.setString(4, artwork.getDimensions());
                statement.setString(5, artwork.getDescription());
                statement.setDouble(6, artwork.getPrice());
                statement.setString(7, artwork.getStatus() == null ? null : artwork.getStatus().name());
                statement.setString(8, artwork.getArtist() == null ? null : artwork.getArtist().getName());
                statement.setString(9, artwork.getTitle());
                statement.executeUpdate();
            }

            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM artwork_artwork_tag WHERE artwork_title = ?")) {
                delete.setString(1, artwork.getTitle());
                delete.executeUpdate();
            }

            List<ArtworkTag> tags = artwork.getTags();
            if (tags != null && !tags.isEmpty()) {
                for (ArtworkTag tag : tags) {
                    if (tag != null && tag.getName() != null && !tag.getName().isBlank()) {
                        try (PreparedStatement ensureTag = connection.prepareStatement(
                                "INSERT IGNORE INTO artwork_tag (name) VALUES (?)")) {
                            ensureTag.setString(1, tag.getName());
                            ensureTag.executeUpdate();
                        }

                        try (PreparedStatement link = connection.prepareStatement(
                                "INSERT INTO artwork_artwork_tag (artwork_title, tag_name) VALUES (?, ?)")) {
                            link.setString(1, artwork.getTitle());
                            link.setString(2, tag.getName());
                            link.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update artwork.", e);
        }
    }

    @Override
    public void delete(String title) {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteLinks = connection.prepareStatement(
                    "DELETE FROM artwork_artwork_tag WHERE artwork_title = ?")) {
                deleteLinks.setString(1, title);
                deleteLinks.executeUpdate();
            }

            try (PreparedStatement deleteArtwork = connection.prepareStatement(
                    "DELETE FROM artwork WHERE title = ?")) {
                deleteArtwork.setString(1, title);
                deleteArtwork.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete artwork.", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        String sql = "SELECT title, creation_year, type, medium, dimensions, description, price, status, artist_name FROM artwork WHERE LOWER(artist_name) = LOWER(?) ORDER BY title";
        List<Artwork> artworks = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, artistName);

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

                    String fetchedArtistName = resultSet.getString("artist_name");
                    if (fetchedArtistName != null && !fetchedArtistName.isBlank()) {
                        String artistSql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist WHERE name = ?";
                        try (PreparedStatement artistStmt = connection.prepareStatement(artistSql)) {
                            artistStmt.setString(1, fetchedArtistName);
                            try (ResultSet artistResult = artistStmt.executeQuery()) {
                                if (artistResult.next()) {
                                    Artist artist = new Artist();
                                    artist.setName(artistResult.getString("name"));
                                    artist.setBio(artistResult.getString("bio"));
                                    int artistBirthYear = artistResult.getInt("birth_year");
                                    artist.setBirthYear(artistResult.wasNull() ? null : artistBirthYear);
                                    artist.setContactEmail(artistResult.getString("contact_email"));
                                    artist.setPhone(artistResult.getString("phone"));
                                    artist.setCity(artistResult.getString("city"));
                                    artist.setWebsite(artistResult.getString("website"));
                                    artist.setSocialMedia(artistResult.getString("social_media"));
                                    artist.setActive(artistResult.getBoolean("is_active"));

                                    String discSql = "SELECT d.name FROM discipline d INNER JOIN artist_discipline ad ON ad.discipline_name = d.name WHERE ad.artist_name = ? ORDER BY d.name";
                                    try (PreparedStatement discStmt = connection.prepareStatement(discSql)) {
                                        discStmt.setString(1, artist.getName());
                                        try (ResultSet discResult = discStmt.executeQuery()) {
                                            List<Discipline> disciplines = new ArrayList<>();
                                            while (discResult.next()) {
                                                disciplines.add(new Discipline(discResult.getString("name")));
                                            }
                                            artist.setDisciplines(disciplines);
                                        }
                                    }
                                    artwork.setArtist(artist);
                                } else {
                                    Artist artist = new Artist();
                                    artist.setName(fetchedArtistName);
                                    artwork.setArtist(artist);
                                }
                            }
                        }
                    }

                    String tagSql = "SELECT t.name FROM artwork_tag t INNER JOIN artwork_artwork_tag at ON at.tag_name = t.name WHERE at.artwork_title = ? ORDER BY t.name";
                    try (PreparedStatement tagStmt = connection.prepareStatement(tagSql)) {
                        tagStmt.setString(1, artwork.getTitle());
                        try (ResultSet tagResult = tagStmt.executeQuery()) {
                            List<ArtworkTag> tags = new ArrayList<>();
                            while (tagResult.next()) {
                                tags.add(new ArtworkTag(tagResult.getString("name")));
                            }
                            artwork.setTags(tags);
                        }
                    }

                    artworks.add(artwork);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load artworks for artist.", e);
        }
        return artworks;
    }
}
