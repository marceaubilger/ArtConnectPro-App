package com.project.artconnect.service.impl;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.service.GalleryService;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of GalleryService using JdbcGalleryDao.
 */
public class JdbcGalleryService implements GalleryService {
    private final GalleryDao galleryDao;

    public JdbcGalleryService(GalleryDao galleryDao) {
        this.galleryDao = galleryDao;
    }

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        List<Gallery> galleries = galleryDao.findAll();
        return galleries.stream()
                .filter(g -> g.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null || gallery.getName() == null || gallery.getName().isBlank()) {
            return List.of();
        }
        Optional<Gallery> foundGallery = getGalleryByName(gallery.getName());
        return foundGallery.map(Gallery::getExhibitions).orElse(List.of());
    }
}

