package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.service.ExhibitionService;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ExhibitionService using ExhibitionDao.
 */
public class JdbcExhibitionService implements ExhibitionService {
    private final ExhibitionDao exhibitionDao;

    public JdbcExhibitionService(ExhibitionDao exhibitionDao) {
        this.exhibitionDao = exhibitionDao;
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        return exhibitionDao.findAll();
    }

    @Override
    public Optional<Exhibition> getExhibitionByTitle(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }

        return exhibitionDao.findAll().stream()
                .filter(exhibition -> title.equalsIgnoreCase(exhibition.getTitle()))
                .findFirst();
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        if (exhibition != null && exhibition.getTitle() != null && !exhibition.getTitle().isBlank()) {
            exhibitionDao.save(exhibition);
        }
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        if (exhibition != null && exhibition.getTitle() != null && !exhibition.getTitle().isBlank()) {
            exhibitionDao.update(exhibition);
        }
    }

    @Override
    public void deleteExhibition(String title) {
        if (title != null && !title.isBlank()) {
            exhibitionDao.delete(title);
        }
    }
}

