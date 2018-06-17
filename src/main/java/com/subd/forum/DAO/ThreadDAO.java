package com.subd.forum.DAO;

import com.subd.forum.mappers.ForumMapper;
import com.subd.forum.mappers.ThreadMapper;
import com.subd.forum.models.Thread;
import com.subd.forum.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


@Service
public class ThreadDAO {
    private final JdbcTemplate jdbcTemplate;

    final String insertThread = "INSERT INTO public.thread (description, created, forum, message, slug, title, author)" +
            " VALUES (?, COALESCE(?::TIMESTAMPTZ, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?)" +
            "RETURNING thread_id";
    final String updateForumThreadCount = "UPDATE forum SET threads = threads + 1 WHERE LOWER(slug) = LOWER(?)";
    final String selectThreadById = "SELECT * FROM public.thread WHERE thread_id = ?";
    final String selectThreadBySlug = "SELECT * FROM public.thread WHERE LOWER(slug) = LOWER(?)";
    final String updateThread = "UPDATE public.thread " +
            "SET title = COALESCE(?, title), message = COALESCE(?, message) " +
            "WHERE thread_id = ?";
    final String checkSQL = "SELECT vote FROM votes WHERE user_id = ? AND thread_id = ?";
    final String updVoice = "UPDATE votes SET vote = ? WHERE user_id = ? AND thread_id = ?";
    final String updThreadVotes = "UPDATE thread SET votes = votes + ? WHERE thread_id = ?";
    final String addVote = "INSERT INTO votes (user_id, thread_id, vote) VALUES (?, ?, ?)";
    final String addNewVisitors = "INSERT INTO forum_users (user_id, forum_id, nickname) VALUES (?, ?, ?) "
            + "ON CONFLICT (user_id, forum_id) DO NOTHING";

    private final UserDAO userDAO;


    @Autowired
    public ThreadDAO(JdbcTemplate jdbcTemplate, UserDAO userDAO){
        this.jdbcTemplate = jdbcTemplate;
        this.userDAO = userDAO;
    }

    public Thread add(Thread thread, Integer forumId) {

        Thread newThread = null;
        int id;
        try {
            id = jdbcTemplate.queryForObject(
                    insertThread, Integer.class,
                    thread.getDescription(), thread.getCreated(), thread.getForum(), thread.getMessage(),
                    thread.getSlug(), thread.getTitle(), thread.getAuthor());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
        String upd = updateForumThreadCount;
        try {
            jdbcTemplate.update(upd, thread.getForum());
        } catch (DataAccessException e) {
            return null;
        }

        try {
            newThread = this.jdbcTemplate.queryForObject(
                    selectThreadById,
                    new Object[]{id}, new ThreadMapper());

        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }

        Integer authorId = userDAO.getUserIdByName(thread.getAuthor());

        try {
            jdbcTemplate.update(addNewVisitors, authorId, forumId, thread.getAuthor());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        return newThread;
    }

    public Thread getById(Integer id) {
        Thread thread = null;
        try {
            thread = this.jdbcTemplate.queryForObject(
                    selectThreadById,
                    new Object[]{id}, new ThreadMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return thread;
    }

    public Thread getBySlug(String slug) {
        Thread thread = null;
        try {
            thread = this.jdbcTemplate.queryForObject(
                    selectThreadBySlug,
                    new Object[]{slug}, new ThreadMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return thread;
    }

    public List<Thread> getThreadByForum(String slug, Integer limit, String since, Boolean desc) {
        Timestamp time = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM thread WHERE LOWER(forum) = LOWER(?)");

        if(since != null) {
            if (desc) {
                queryBuilder.append("AND created <= ? ");
            } else
                queryBuilder.append("AND created >= ? ");

            String st = ZonedDateTime.parse(since).format(DateTimeFormatter.ISO_INSTANT);
            time = new Timestamp(ZonedDateTime.parse(st).toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli());
        }


        if(desc) {
            queryBuilder.append("ORDER BY created DESC ");
        } else
            queryBuilder.append("ORDER BY created ");

        queryBuilder.append("LIMIT ? ;");

        String query = queryBuilder.toString();

        ArrayList<Thread> threads = null;
        try {
            List<Map<String, Object>> rows;
            if(since != null)
                rows = jdbcTemplate.queryForList(query, slug, time, limit);
            else
                rows = jdbcTemplate.queryForList(query, slug, limit);

            threads = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String sl;
                if(row.get("slug") == null){
                    sl = null;
                } else {
                    sl = row.get("slug").toString();
                }
                String descr = null;
                if (row.get("description") != null) {
                    descr = row.get("description").toString();
                }


//                final Timestamp timestamp = Timestamp.valueOf(row.get("created").toString())
                threads.add(new Thread(
                                Integer.parseInt(row.get("thread_id").toString()), Integer.parseInt(row.get("votes").toString()),
                                descr,
                        Timestamp.valueOf(row.get("created").toString())
                                .toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                row.get("message").toString(), row.get("forum").toString(),
                                sl, row.get("title").toString(),
                                row.get("author").toString()
                        )
                );
            }
        }
        catch (DataAccessException e){
            e.printStackTrace();
            return null;
        }
        return threads;
    }

    public Thread updateById(Integer id, Thread updThread) {
        if (updThread.getTitle() != null || updThread.getMessage() != null) {
            try {
                jdbcTemplate.update(updateThread, updThread.getTitle(), updThread.getMessage(), id);
            } catch (DataAccessException e) {
                return null;
            }
        }

        return getById(id);
    }

    public Integer getRowsCount() {
        Integer count = null;
        final String sql = "SELECT count(*) from public.thread";
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }

    public Thread vote(Thread thread, Integer userId, Vote vote) {
        try {
            Integer previous = jdbcTemplate.queryForObject(checkSQL, Integer.class, userId,
                    thread.getId());

            if (vote.getVoice().equals(previous)) {
                return thread;
            }
            jdbcTemplate.update(updVoice, vote.getVoice(), userId, thread.getId());

            jdbcTemplate.update(updThreadVotes, vote.getVoice() * 2, thread.getId());
            thread.setVotes(vote.getVoice() * 2 + thread.getVotes());

        } catch(IncorrectResultSizeDataAccessException exception) {

            jdbcTemplate.update(addVote, userId, thread.getId(), vote.getVoice());

            jdbcTemplate.update(updThreadVotes, vote.getVoice(), thread.getId());

            thread.setVotes(vote.getVoice() + thread.getVotes());
        }
        return thread;
    }

    public void clearTable() {
        String sql = "TRUNCATE TABLE thread CASCADE";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        sql = "TRUNCATE TABLE votes CASCADE";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

}
