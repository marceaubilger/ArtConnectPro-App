package com.project.artconnect.service.impl;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.service.CommunityService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC implementation of CommunityService using JdbcCommunityMemberDao.
 */
public class JdbcCommunityService implements CommunityService {
    private final CommunityMemberDao communityMemberDao;

    public JdbcCommunityService(CommunityMemberDao communityMemberDao) {
        this.communityMemberDao = communityMemberDao;
    }

    @Override
    public List<CommunityMember> getAllMembers() {
        return communityMemberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return communityMemberDao.findByName(name);
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) {
            return List.of();
        }
        return member.getReviews();
    }
}

