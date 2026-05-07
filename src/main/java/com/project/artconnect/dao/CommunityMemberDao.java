package com.project.artconnect.dao;

import com.project.artconnect.model.CommunityMember;
import java.util.List;
import java.util.Optional;

public interface CommunityMemberDao {
    Optional<CommunityMember> findByName(String name);

    List<CommunityMember> findAll();
}
