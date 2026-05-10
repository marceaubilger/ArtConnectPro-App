package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;
import com.project.artconnect.persistence.*;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization. Uses JDBC DAOs for database persistence.
 */
public class ServiceProvider {
    private static final JdbcArtistDao artistDao = new JdbcArtistDao();
    private static final JdbcArtworkDao artworkDao = new JdbcArtworkDao();
    private static final JdbcGalleryDao galleryDao = new JdbcGalleryDao();
    private static final JdbcWorkshopDao workshopDao = new JdbcWorkshopDao();
    private static final JdbcCommunityMemberDao communityMemberDao = new JdbcCommunityMemberDao();

    private static final ArtistService artistService = new JdbcArtistService(artistDao);
    private static final ArtworkService artworkService = new JdbcArtworkService(artworkDao, artistDao);
    private static final GalleryService galleryService = new JdbcGalleryService(galleryDao);
    private static final WorkshopService workshopService = new JdbcWorkshopService(workshopDao);
    private static final CommunityService communityService = new JdbcCommunityService(communityMemberDao);

    public static ArtistService getArtistService() {
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        return communityService;
    }
}
