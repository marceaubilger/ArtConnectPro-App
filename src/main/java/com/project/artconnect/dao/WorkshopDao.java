package com.project.artconnect.dao;

import com.project.artconnect.model.Workshop;
import java.util.List;
import java.util.Optional;

public interface WorkshopDao {
    Optional<Workshop> findByTitle(String title);

    List<Workshop> findAll();
}
