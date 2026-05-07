package com.project.artconnect.dao.impl;
import com.project.artconnect.model.Artist;
import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.impl.InMemoryArtistService;
import com.project.artconnect.service.impl.InMemoryArtworkService;
import java.util.List;

public class ArtworksDaoImpl implements ArtworkDao {
    private final InMemoryArtworkService service = new InMemoryArtworkService();
    private final InMemoryArtistService serviceArtists = new InMemoryArtistService();

    public List<Artwork> findAll() {
        return  service.getAllArtworks();
    }

    public void save(Artwork artwork) {
        service.createArtwork(artwork);
    }

    public void update(Artwork artwork) {
        service.updateArtwork(artwork);
    }

    public void delete(String artworkName) {
        service.deleteArtwork(artworkName);
    }

    public List<Artwork> findByArtistName(String artistName) {
        Artist a = serviceArtists.getArtistByName(artistName).orElse(null);
        return service.getArtworksByArtist(a);
    }
}