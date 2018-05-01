package com.subd.forum.mappers;

import com.subd.forum.models.User;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserMapper implements RowMapper<User> {
    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull User mapRow(@NotNull ResultSet rs, @NotNull int rowNum) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("nickname"),
                rs.getString("email"), rs.getString("fullname"),
                rs.getString("about"));
    }
}
