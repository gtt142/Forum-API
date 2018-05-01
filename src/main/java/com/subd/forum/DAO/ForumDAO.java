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

    @Autowired
    public ForumDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public Boolean isExistBySlug(String slug) {
        try {
             this.jdbcTemplate.queryForObject(
                    "SELECT forum_id FROM public.forum WHERE slug ILIKE ?",
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
                    "SELECT * FROM public.forum WHERE forum_id = ?",
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
                    "SELECT * FROM public.forum WHERE LOWER(slug) = LOWER(?)",
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
