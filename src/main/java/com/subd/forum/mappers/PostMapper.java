package com.subd.forum.mappers;


import com.subd.forum.models.Post;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class PostMapper implements RowMapper<Post> {
    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull Post mapRow(@NotNull ResultSet rs, @NotNull int rowNum) throws SQLException {
        return new Post(rs.getInt("post_id"), rs.getInt("votes"),
                rs.getString("description"),
                rs.getTimestamp("created").toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                rs.getString("message"), rs.getString("forum"),
                rs.getString("author"), rs.getBoolean("isEdited"),
                rs.getInt("parent"), rs.getInt("thread"));
    }
}
