package com.project.artconnect.dao.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.service.impl.InMemoryArtistService;
import com.project.artconnect.model.Artist;

import java.util.List;

public class ArtistDaoImpl implements ArtistDao {
    private final InMemoryArtistService service = new InMemoryArtistService();

    public List<Artist> findAll() {
        return  service.getAllArtists();
    }

    public void save(Artist artist) {
        service.createArtist(artist);
    }

    public void update(Artist artist) {
        service.updateArtist(artist);
    }

    public void delete(String artistName) {
        service.deleteArtist(artistName);
    }

    public List<Artist> findByCity(String city) {
        return service.searchArtists(null, null, city);
    }
}
