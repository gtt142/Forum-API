package com.subd.forum.mappers;

import com.subd.forum.models.Forum;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ForumMapper implements RowMapper<Forum> {
    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull Forum mapRow(@NotNull ResultSet rs, @NotNull int rowNum) throws SQLException {
        return new Forum(rs.getInt("forum_id"), rs.getInt("posts"),
                rs.getInt("threads"),
                rs.getString("description"),
                rs.getString("slug"), rs.getString("title"),
                rs.getString("username"));
    }
}
