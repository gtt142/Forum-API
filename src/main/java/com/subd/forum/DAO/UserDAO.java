package com.subd.forum.DAO;

import com.subd.forum.mappers.UserMapper;
import com.subd.forum.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserDAO {

    private final JdbcTemplate jdbcTemplate;

    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    final String selectUserByNickname = "SELECT * FROM public.users WHERE LOWER(nickname) = LOWER(?)";
    final String selectUserIdByNickname = "SELECT user_id FROM public.users WHERE LOWER(nickname) = LOWER(?)";
    final String selectUserById = "SELECT * FROM users WHERE user_id = ?";
//    final String refresh = "REFRESH MATERIALIZED VIEW forum_users";


    public User getByName(String name) {
        User user = null;
        try {
            user = this.jdbcTemplate.queryForObject(
                    selectUserByNickname,
                    new Object[]{name}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUserById(Integer id) {
        User user = null;
        try {
            user = this.jdbcTemplate.queryForObject(
                    selectUserById,
                    new Object[]{id}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return user;
    }

    public Integer getUserIdByName(String name) {
        Integer id = null;
        try {
            id = jdbcTemplate.queryForObject(selectUserIdByNickname, Integer.class, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    public Integer getRowsCount() {
        Integer count = null;
        final String sql = "SELECT count(*) from public.users";
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }

    public Boolean isExist(String nick) {
        String sql = "SELECT nickname FROM users WHERE nickname = ?";

        try {
            jdbcTemplate.queryForObject(sql, String.class, nick);
        } catch (EmptyResultDataAccessException e){
            return false;
        }
        return true;
    }

    public void clearTable() {
        final String sql = "TRUNCATE TABLE users CASCADE";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }


    }

    public List<User> getUsersByForum(Integer forumId, Integer limit, String since, Boolean desc) {

        String compare = (desc != null && desc ? "< " : "> ");
        String order = (desc != null && desc ? "DESC " : "ASC ");

        StringBuilder builder = new StringBuilder("SELECT * ");
        builder.append("FROM users u WHERE u.nickname IN (");
        builder.append("SELECT nickname FROM forum_users WHERE forum_users.forum_id = ? ) ");
                // JOIN forum_users fu ON (u.user_id = fu.user_id AND fu.forum_id = ?) ");
//        builder.append(" WHERE fu.forum_id = ? ");
        if (since != null) {
            builder.append(" AND LOWER(u.nickname COLLATE \"C\") ").append(compare).append("LOWER('").append(String.valueOf(since)).append("' COLLATE \"C\") ");
        }
        builder.append(" ORDER BY LOWER(u.nickname COLLATE \"C\") ").append(order);
        if (limit != null) {
            builder.append("LIMIT ").append(String.valueOf(limit));
        }

        String sql = builder.toString();

        ArrayList<User> users = new ArrayList<>();
        try {
            List<Map<String, Object>> rows;
//            // TODO денормализовать forum_users
//            try {
//                jdbcTemplate.execute(refresh);
//            } catch (DataAccessException e) {
//                e.printStackTrace();
//            }
            rows = jdbcTemplate.queryForList(sql, forumId);

            for (Map<String, Object> row : rows) {
                users.add(new User(
                                Integer.parseInt(row.get("user_id").toString()), row.get("nickname").toString(),
                                row.get("email").toString(), row.get("fullname").toString(),
                                row.get("about").toString()
                        )
                );
            }
        }
        catch (DataAccessException e){
            e.printStackTrace();
            return null;
        }
        return users;
    }
}
