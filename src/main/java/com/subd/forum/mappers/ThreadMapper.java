package com.subd.forum.mappers;

import com.subd.forum.models.Thread;
import org.springframework.jdbc.core.RowMapper;

import javax.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class ThreadMapper implements RowMapper<Thread> {
    @SuppressWarnings("NullableProblems")
    @Override
    public @NotNull Thread mapRow(@NotNull ResultSet rs, @NotNull int rowNum) throws SQLException {
//        final Timestamp timestamp = rs.getTimestamp("created");
//        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new Thread(rs.getInt("thread_id"), rs.getInt("votes"),
                rs.getString("description"),
//                dateFormat.format(timestamp.getTime()),
                rs.getTimestamp("created").toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        rs.getString("message"), rs.getString("forum"),
                rs.getString("slug"), rs.getString("title"),
                rs.getString("author"));
    }
}
