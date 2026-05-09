package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation for CommunityMemberDao.
 */
public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findByName(String name) {
        String sql = "SELECT name, email, birth_year, phone, city, membership_type FROM community_member WHERE name = ?";

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                CommunityMember member = new CommunityMember();
                member.setName(resultSet.getString("name"));
                member.setEmail(resultSet.getString("email"));

                int birthYear = resultSet.getInt("birth_year");
                member.setBirthYear(resultSet.wasNull() ? null : birthYear);

                member.setPhone(resultSet.getString("phone"));
                member.setCity(resultSet.getString("city"));
                member.setMembershipType(resultSet.getString("membership_type"));

                // Load favorite disciplines
                String discSql = "SELECT d.name FROM discipline d INNER JOIN member_discipline md ON md.discipline_name = d.name WHERE md.member_name = ? ORDER BY d.name";
                try (PreparedStatement discStmt = connection.prepareStatement(discSql)) {
                    discStmt.setString(1, member.getName());
                    try (ResultSet discResult = discStmt.executeQuery()) {
                        List<Discipline> disciplines = new ArrayList<>();
                        while (discResult.next()) {
                            disciplines.add(new Discipline(discResult.getString("name")));
                        }
                        member.setFavoriteDisciplines(disciplines);
                    }
                }

                return Optional.of(member);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load community member by name.", e);
        }
    }

    @Override
    public List<CommunityMember> findAll() {
        String sql = "SELECT name, email, birth_year, phone, city, membership_type FROM community_member ORDER BY name";
        List<CommunityMember> members = new ArrayList<>();

        try (Connection connection = ConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                CommunityMember member = new CommunityMember();
                member.setName(resultSet.getString("name"));
                member.setEmail(resultSet.getString("email"));

                int birthYear = resultSet.getInt("birth_year");
                member.setBirthYear(resultSet.wasNull() ? null : birthYear);

                member.setPhone(resultSet.getString("phone"));
                member.setCity(resultSet.getString("city"));
                member.setMembershipType(resultSet.getString("membership_type"));

                // Load favorite disciplines
                String discSql = "SELECT d.name FROM discipline d INNER JOIN member_discipline md ON md.discipline_name = d.name WHERE md.member_name = ? ORDER BY d.name";
                try (PreparedStatement discStmt = connection.prepareStatement(discSql)) {
                    discStmt.setString(1, member.getName());
                    try (ResultSet discResult = discStmt.executeQuery()) {
                        List<Discipline> disciplines = new ArrayList<>();
                        while (discResult.next()) {
                            disciplines.add(new Discipline(discResult.getString("name")));
                        }
                        member.setFavoriteDisciplines(disciplines);
                    }
                }

                members.add(member);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to load community members.", e);
        }
        return members;
    }
}

