package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        String sql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist ORDER BY name";
        List<Artist> artists = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Artist artist = new Artist();
                artist.setName(resultSet.getString("name"));
                artist.setBio(resultSet.getString("bio"));

                int birthYear = resultSet.getInt("birth_year");
                artist.setBirthYear(resultSet.wasNull() ? null : birthYear);

                artist.setContactEmail(resultSet.getString("contact_email"));
                artist.setPhone(resultSet.getString("phone"));
                artist.setCity(resultSet.getString("city"));
                artist.setWebsite(resultSet.getString("website"));
                artist.setSocialMedia(resultSet.getString("social_media"));
                artist.setActive(resultSet.getBoolean("is_active"));

                // Load disciplines
                String disciplineSql = "SELECT d.name FROM discipline d " +
                        "INNER JOIN artist_discipline ad ON ad.discipline_name = d.name " +
                        "WHERE ad.artist_name = ? ORDER BY d.name";
                try (PreparedStatement discStatement = connection.prepareStatement(disciplineSql)) {
                    discStatement.setString(1, artist.getName());
                    try (ResultSet discResult = discStatement.executeQuery()) {
                        List<Discipline> disciplines = new ArrayList<>();
                        while (discResult.next()) {
                            disciplines.add(new Discipline(discResult.getString("name")));
                        }
                        artist.setDisciplines(disciplines);
                    }
                }

                artists.add(artist);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load artists.", e);
        }
        return artists;
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, artist.getName());
                statement.setString(2, artist.getBio());
                if (artist.getBirthYear() == null) {
                    statement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(3, artist.getBirthYear());
                }
                statement.setString(4, artist.getContactEmail());
                statement.setString(5, artist.getPhone());
                statement.setString(6, artist.getCity());
                statement.setString(7, artist.getWebsite());
                statement.setString(8, artist.getSocialMedia());
                statement.setBoolean(9, artist.isActive());
                statement.executeUpdate();
            }

            // Delete existing disciplines
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM artist_discipline WHERE artist_name = ?")) {
                delete.setString(1, artist.getName());
                delete.executeUpdate();
            }

            // Insert new disciplines
            List<Discipline> disciplines = artist.getDisciplines();
            if (disciplines != null && !disciplines.isEmpty()) {
                for (Discipline discipline : disciplines) {
                    if (discipline != null && discipline.getName() != null && !discipline.getName().isBlank()) {
                        try (PreparedStatement ensureDisc = connection.prepareStatement(
                                "INSERT IGNORE INTO discipline (name) VALUES (?)")) {
                            ensureDisc.setString(1, discipline.getName());
                            ensureDisc.executeUpdate();
                        }

                        try (PreparedStatement link = connection.prepareStatement(
                                "INSERT INTO artist_discipline (artist_name, discipline_name) VALUES (?, ?)")) {
                            link.setString(1, artist.getName());
                            link.setString(2, discipline.getName());
                            link.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to save artist.", e);
        }
    }

    @Override
    public void update(Artist artist) {
        String sql = "UPDATE artist SET bio = ?, birth_year = ?, contact_email = ?, phone = ?, city = ?, website = ?, social_media = ?, is_active = ? WHERE name = ?";

        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, artist.getBio());
                if (artist.getBirthYear() == null) {
                    statement.setNull(2, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(2, artist.getBirthYear());
                }
                statement.setString(3, artist.getContactEmail());
                statement.setString(4, artist.getPhone());
                statement.setString(5, artist.getCity());
                statement.setString(6, artist.getWebsite());
                statement.setString(7, artist.getSocialMedia());
                statement.setBoolean(8, artist.isActive());
                statement.setString(9, artist.getName());
                statement.executeUpdate();
            }

            // Delete existing disciplines
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM artist_discipline WHERE artist_name = ?")) {
                delete.setString(1, artist.getName());
                delete.executeUpdate();
            }

            // Insert new disciplines
            List<Discipline> disciplines = artist.getDisciplines();
            if (disciplines != null && !disciplines.isEmpty()) {
                for (Discipline discipline : disciplines) {
                    if (discipline != null && discipline.getName() != null && !discipline.getName().isBlank()) {
                        try (PreparedStatement ensureDisc = connection.prepareStatement(
                                "INSERT IGNORE INTO discipline (name) VALUES (?)")) {
                            ensureDisc.setString(1, discipline.getName());
                            ensureDisc.executeUpdate();
                        }

                        try (PreparedStatement link = connection.prepareStatement(
                                "INSERT INTO artist_discipline (artist_name, discipline_name) VALUES (?, ?)")) {
                            link.setString(1, artist.getName());
                            link.setString(2, discipline.getName());
                            link.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update artist.", e);
        }
    }

    @Override
    public void delete(String artistName) {
        try (Connection connection = ConnectionManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteLinks = connection.prepareStatement(
                    "DELETE FROM artist_discipline WHERE artist_name = ?")) {
                deleteLinks.setString(1, artistName);
                deleteLinks.executeUpdate();
            }

            try (PreparedStatement deleteArtist = connection.prepareStatement(
                    "DELETE FROM artist WHERE name = ?")) {
                deleteArtist.setString(1, artistName);
                deleteArtist.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete artist.", e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        String sql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active " +
                "FROM artist WHERE LOWER(city) = LOWER(?) ORDER BY name";
        List<Artist> artists = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, city);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Artist artist = new Artist();
                    artist.setName(resultSet.getString("name"));
                    artist.setBio(resultSet.getString("bio"));

                    int birthYear = resultSet.getInt("birth_year");
                    artist.setBirthYear(resultSet.wasNull() ? null : birthYear);

                    artist.setContactEmail(resultSet.getString("contact_email"));
                    artist.setPhone(resultSet.getString("phone"));
                    artist.setCity(resultSet.getString("city"));
                    artist.setWebsite(resultSet.getString("website"));
                    artist.setSocialMedia(resultSet.getString("social_media"));
                    artist.setActive(resultSet.getBoolean("is_active"));

                    // Load disciplines
                    String disciplineSql = "SELECT d.name FROM discipline d " +
                            "INNER JOIN artist_discipline ad ON ad.discipline_name = d.name " +
                            "WHERE ad.artist_name = ? ORDER BY d.name";
                    try (PreparedStatement discStatement = connection.prepareStatement(disciplineSql)) {
                        discStatement.setString(1, artist.getName());
                        try (ResultSet discResult = discStatement.executeQuery()) {
                            List<Discipline> disciplines = new ArrayList<>();
                            while (discResult.next()) {
                                disciplines.add(new Discipline(discResult.getString("name")));
                            }
                            artist.setDisciplines(disciplines);
                        }
                    }

                    artists.add(artist);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load artists by city.", e);
        }
        return artists;
    }
}
