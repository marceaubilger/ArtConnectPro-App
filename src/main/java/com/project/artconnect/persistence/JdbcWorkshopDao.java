package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for WorkshopDao.
 */
public class JdbcWorkshopDao implements WorkshopDao {



    @Override
    public Optional<Workshop> findByTitle(String title) {
        String sql = "SELECT title, date, duration_minutes, max_participants, price, instructor_name, location, description, level FROM workshop WHERE title = ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Workshop workshop = new Workshop();
                workshop.setTitle(resultSet.getString("title"));

                Timestamp timestamp = resultSet.getTimestamp("date");
                workshop.setDate(timestamp == null ? null : timestamp.toLocalDateTime());

                workshop.setDurationMinutes(resultSet.getInt("duration_minutes"));
                workshop.setMaxParticipants(resultSet.getInt("max_participants"));
                workshop.setPrice(resultSet.getDouble("price"));
                workshop.setLocation(resultSet.getString("location"));
                workshop.setDescription(resultSet.getString("description"));
                workshop.setLevel(resultSet.getString("level"));

                String instructorName = resultSet.getString("instructor_name");
                if (instructorName != null && !instructorName.isBlank()) {
                    String artistSql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist WHERE name = ?";
                    try (PreparedStatement artistStmt = connection.prepareStatement(artistSql)) {
                        artistStmt.setString(1, instructorName);
                        try (ResultSet artistResult = artistStmt.executeQuery()) {
                            if (artistResult.next()) {
                                Artist artist = new Artist();
                                artist.setName(artistResult.getString("name"));
                                artist.setBio(artistResult.getString("bio"));
                                int birthYear = artistResult.getInt("birth_year");
                                artist.setBirthYear(artistResult.wasNull() ? null : birthYear);
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
                                workshop.setInstructor(artist);
                            } else {
                                Artist artist = new Artist();
                                artist.setName(instructorName);
                                workshop.setInstructor(artist);
                            }
                        }
                    }
                }

                return Optional.of(workshop);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load workshop by title.", e);
        }
    }

    @Override
    public List<Workshop> findAll() {
        String sql = "SELECT title, date, duration_minutes, max_participants, price, instructor_name, location, description, level FROM workshop ORDER BY title";
        List<Workshop> workshops = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Workshop workshop = new Workshop();
                workshop.setTitle(resultSet.getString("title"));

                Timestamp timestamp = resultSet.getTimestamp("date");
                workshop.setDate(timestamp == null ? null : timestamp.toLocalDateTime());

                workshop.setDurationMinutes(resultSet.getInt("duration_minutes"));
                workshop.setMaxParticipants(resultSet.getInt("max_participants"));
                workshop.setPrice(resultSet.getDouble("price"));
                workshop.setLocation(resultSet.getString("location"));
                workshop.setDescription(resultSet.getString("description"));
                workshop.setLevel(resultSet.getString("level"));

                String instructorName = resultSet.getString("instructor_name");
                if (instructorName != null && !instructorName.isBlank()) {
                    String artistSql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist WHERE name = ?";
                    try (PreparedStatement artistStmt = connection.prepareStatement(artistSql)) {
                        artistStmt.setString(1, instructorName);
                        try (ResultSet artistResult = artistStmt.executeQuery()) {
                            if (artistResult.next()) {
                                Artist artist = new Artist();
                                artist.setName(artistResult.getString("name"));
                                artist.setBio(artistResult.getString("bio"));
                                int birthYear = artistResult.getInt("birth_year");
                                artist.setBirthYear(artistResult.wasNull() ? null : birthYear);
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
                                workshop.setInstructor(artist);
                            } else {
                                Artist artist = new Artist();
                                artist.setName(instructorName);
                                workshop.setInstructor(artist);
                            }
                        }
                    }
                }

                workshops.add(workshop);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load workshops.", e);
        }
        return workshops;
    }
}

