package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC implementation of ArtistService using JdbcArtistDao.
 */
public class JdbcArtistService implements ArtistService {
    private final ArtistDao artistDao;

    public JdbcArtistService(ArtistDao artistDao) {
        this.artistDao = artistDao;
    }

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        List<Artist> artists = artistDao.findAll();
        return artists.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        if (artist != null) {
            artistDao.save(artist);
        }
    }

    @Override
    public void updateArtist(Artist artist) {
        if (artist != null) {
            artistDao.update(artist);
        }
    }

    @Override
    public void deleteArtist(String name) {
        if (name != null && !name.isBlank()) {
            artistDao.delete(name);
        }
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        return artistDao.findAll().stream()
                .flatMap(a -> a.getDisciplines().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> artists = artistDao.findAll();

        if (query != null && !query.isBlank()) {
            String queryLower = query.toLowerCase();
            artists = artists.stream()
                    .filter(a -> a.getName().toLowerCase().contains(queryLower))
                    .collect(Collectors.toList());
        }

        if (disciplineName != null && !disciplineName.isBlank()) {
            String discLower = disciplineName.toLowerCase();
            artists = artists.stream()
                    .filter(a -> a.getDisciplines().stream()
                            .anyMatch(d -> d.getName().toLowerCase().contains(discLower)))
                    .collect(Collectors.toList());
        }

        if (city != null && !city.isBlank()) {
            String cityLower = city.toLowerCase();
            artists = artists.stream()
                    .filter(a -> a.getCity() != null && a.getCity().toLowerCase().contains(cityLower))
                    .collect(Collectors.toList());
        }

        return artists;
    }
}

