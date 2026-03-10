package com.weam.appointments.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public Optional<UserRecord> findByUsername(String username) {

        String sql = "SELECT username, password, role FROM users WHERE username = ?";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) return Optional.empty();

                return Optional.of(
                        new UserRecord(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("role")
                        )
                );
            }

        } catch (Exception e) {
            throw new RuntimeException("DB query failed", e);
        }
    }
    @Override
    public boolean addUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username,password,role) VALUES (?,?,?)";

        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);

            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            return false;
        }
    }
}