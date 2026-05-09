package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for GalleryDao.
 */
public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT name, address, owner_name, opening_hours, contact_phone, rating, website FROM gallery WHERE id = ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Gallery gallery = new Gallery();
                gallery.setName(resultSet.getString("name"));
                gallery.setAddress(resultSet.getString("address"));
                gallery.setOwnerName(resultSet.getString("owner_name"));
                gallery.setOpeningHours(resultSet.getString("opening_hours"));
                gallery.setContactPhone(resultSet.getString("contact_phone"));
                gallery.setRating(resultSet.getDouble("rating"));
                gallery.setWebsite(resultSet.getString("website"));

                return Optional.of(gallery);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load gallery by id.", e);
        }
    }

    @Override
    public List<Gallery> findAll() {
        String sql = "SELECT name, address, owner_name, opening_hours, contact_phone, rating, website FROM gallery ORDER BY name";
        List<Gallery> galleries = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Gallery gallery = new Gallery();
                gallery.setName(resultSet.getString("name"));
                gallery.setAddress(resultSet.getString("address"));
                gallery.setOwnerName(resultSet.getString("owner_name"));
                gallery.setOpeningHours(resultSet.getString("opening_hours"));
                gallery.setContactPhone(resultSet.getString("contact_phone"));
                gallery.setRating(resultSet.getDouble("rating"));
                gallery.setWebsite(resultSet.getString("website"));

                galleries.add(gallery);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load galleries.", e);
        }
        return galleries;
    }
}

