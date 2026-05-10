package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.service.ArtworkService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC implementation of ArtworkService using JdbcArtworkDao.
 */
public class JdbcArtworkService implements ArtworkService {
    private final ArtworkDao artworkDao;
    private final ArtistDao artistDao;

    public JdbcArtworkService(ArtworkDao artworkDao, ArtistDao artistDao) {
        this.artworkDao = artworkDao;
        this.artistDao = artistDao;
    }

    @Override
    public List<Artwork> getAllArtworks() {
        return artworkDao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }
        List<Artwork> artworks = artworkDao.findAll();
        return artworks.stream()
                .filter(a -> a.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null || artist.getName() == null || artist.getName().isBlank()) {
            return List.of();
        }
        return artworkDao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        if (artwork != null) {
            artworkDao.save(artwork);
        }
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        if (artwork != null) {
            artworkDao.update(artwork);
        }
    }

    @Override
    public void deleteArtwork(String title) {
        if (title != null && !title.isBlank()) {
            artworkDao.delete(title);
        }
    }
}

