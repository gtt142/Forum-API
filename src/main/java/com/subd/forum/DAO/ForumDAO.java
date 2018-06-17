package com.subd.forum.DAO;

import com.subd.forum.mappers.ForumMapper;
import com.subd.forum.models.Forum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ForumDAO {
    private final JdbcTemplate jdbcTemplate;

    final String selectForumIdBySlug = "SELECT forum_id FROM public.forum WHERE LOWER(slug) = LOWER(?)";
    final String selectFromForumById = "SELECT * FROM public.forum WHERE forum_id = ?";
    final String selectFromForumBySlug = "SELECT * FROM public.forum WHERE LOWER(slug) = LOWER(?)";

    @Autowired
    public ForumDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getForumIdBySlug(String slug) {
        Integer id = null;
        try {
            id = jdbcTemplate.queryForObject(selectForumIdBySlug, Integer.class, slug);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    public Boolean isExistBySlug(String slug) {
        try {
             this.jdbcTemplate.queryForObject(
                    selectForumIdBySlug,
                    Integer.class, slug);
        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Forum getById(Integer id) {
        Forum forum = null;
        try {
            forum = this.jdbcTemplate.queryForObject(
                    selectFromForumById,
                    new Object[]{id}, new ForumMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return forum;
    }

    public Forum getBySlug(String slug) {
        Forum forum = null;
        try {
            forum = this.jdbcTemplate.queryForObject(
                    selectFromForumBySlug,
                    new Object[]{slug}, new ForumMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return forum;
    }

    public Integer getRowsCount() {
        Integer count = null;
        final String sql = "SELECT count(*) from public.forum";
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }

    public void clearTable() {
        final String sql = "TRUNCATE TABLE forum CASCADE";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}
