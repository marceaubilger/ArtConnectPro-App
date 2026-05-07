package com.project.artconnect.dao.impl;
import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.impl.InMemoryArtworkService;
import com.project.artconnect.service.impl.InMemoryWorkshopService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class WorkshopDaoImp implements WorkshopDao{
    private final InMemoryWorkshopService service = new InMemoryWorkshopService();

    public Optional<Workshop> findByTitle(String title){
        return service.getWorkshopByTitle(title);
    }

    public List<Workshop> findAll(){
        return service.getAllWorkshops();
    }
}
