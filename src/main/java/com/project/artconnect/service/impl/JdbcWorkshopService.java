package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.WorkshopService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC implementation of WorkshopService using JdbcWorkshopDao.
 */
public class JdbcWorkshopService implements WorkshopService {
    private final WorkshopDao workshopDao;

    public JdbcWorkshopService(WorkshopDao workshopDao) {
        this.workshopDao = workshopDao;
    }

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        if (title == null || title.isBlank()) {
            return Optional.empty();
        }
        return workshopDao.findByTitle(title);
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop != null && member != null) {
            Booking booking = new Booking();
            booking.setWorkshop(workshop);
            booking.setMember(member);
            member.addBooking(booking);
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) {
            return List.of();
        }
        return member.getBookings();
    }
}

