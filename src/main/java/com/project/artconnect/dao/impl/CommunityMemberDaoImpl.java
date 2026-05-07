package com.project.artconnect.dao.impl;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.impl.InMemoryCommunityService;

import java.util.List;
import java.util.Optional;

public class CommunityMemberDaoImpl implements CommunityMemberDao {
    private final InMemoryCommunityService service = new InMemoryCommunityService();

    public Optional<CommunityMember> findByName(String name) {
        return service.getMemberByName(name);
    }

    public List<CommunityMember> findAll() {
        return service.getAllMembers();
    }
}
