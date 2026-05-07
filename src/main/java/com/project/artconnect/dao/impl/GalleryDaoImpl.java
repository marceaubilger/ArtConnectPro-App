package com.project.artconnect.dao.impl;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.impl.InMemoryGalleryService;
import java.util.List;
import java.util.Optional;

public class GalleryDaoImpl implements GalleryDao {
    private final InMemoryGalleryService service = new InMemoryGalleryService();

    @Override
    public Optional<Gallery> findById(Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }

        List<Gallery> galleries = service.getAllGalleries();
        int index = Math.toIntExact(id);
        if (index >= 0 && index < galleries.size()) {
            return Optional.of(galleries.get(index));
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        return service.getAllGalleries();
    }
}
