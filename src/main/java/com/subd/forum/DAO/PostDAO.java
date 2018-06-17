package com.subd.forum.DAO;

import com.subd.forum.mappers.PostMapper;
import com.subd.forum.models.Post;
import com.subd.forum.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
public class PostDAO {
    private final JdbcTemplate jdbcTemplate;

    final String insertStr = "INSERT INTO post (post_id, description, created, parent, forum, thread, message, author, history)\n" +
            "VALUES (?, ?, COALESCE(?::TIMESTAMPTZ, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?," +
            " array_append((SELECT history FROM post WHERE post_id = ?), ?))";
    final String updateForum = "UPDATE forum SET posts = posts + ? WHERE LOWER(slug) = LOWER(?)";
    final String nextId = "SELECT nextval('post_post_id_seq')";
    final String selectPosts = "SELECT * FROM public.post WHERE post_id = ?";
    final String selectPostThread = "SELECT thread FROM public.post WHERE post_id = ?";
    final String addNewVisitors = "INSERT INTO forum_users (user_id, forum_id) VALUES (?, ?) "
            + "ON CONFLICT (user_id, forum_id) DO NOTHING";
    final String historyIdByPostId = "SELECT history[1] FROM post WHERE post_id = ?";

    @Autowired
    public PostDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Post> addPosts(List<Post> posts, Integer forumId) {

        try(Connection con = jdbcTemplate.getDataSource().getConnection()) {
            con.setAutoCommit(false);
            PreparedStatement pst = con.prepareStatement(insertStr, Statement.NO_GENERATED_KEYS);
            PreparedStatement pst2 = con.prepareStatement(addNewVisitors, Statement.NO_GENERATED_KEYS);
            Integer newId;

            for (Post post: posts) {
                newId = jdbcTemplate.queryForObject(nextId, Integer.class);
                post.setId(newId);
                pst.setInt(1, post.getId());
                pst.setString(2, post.getDescription());
                pst.setString(3, post.getCreated());
                pst.setInt(4, post.getParent());
                pst.setString(5, post.getForum());
                pst.setInt(6, post.getThread());
                pst.setString(7,post.getMessage());
                pst.setString(8, post.getAuthor());
                pst.setInt(9, post.getParent());
                pst.setInt(10, post.getId());

                pst.addBatch();

                pst2.setInt(1, post.getAuthorId());
                pst2.setInt(2, forumId);
                pst2.addBatch();
            }

            pst.executeBatch();
            pst.close();

            if (posts.size() > 0) {
                pst = con.prepareStatement(updateForum, Statement.NO_GENERATED_KEYS);
                pst.setInt(1, posts.size());
                pst.setString(2, posts.get(0).getForum());
                pst.addBatch();
                pst.executeBatch();
                pst.close();
                pst2.executeBatch();
            }
            pst2.close();

            con.commit();
            con.setAutoCommit(true);
            con.close();
        }
        catch (SQLException e){
            e.printStackTrace();
            return null;
        }
        return posts;
    }

    public Post getById(Integer id) {
        Post post = null;
        try {
            post = this.jdbcTemplate.queryForObject(
                    selectPosts,
                    new Object[]{id}, new PostMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return post;
    }

    public Post getPostThreadById(Integer id) {
        Post post = null;
        try {
            final Integer thread = jdbcTemplate.queryForObject(selectPostThread, Integer.class, id);
            post = new Post();
            post.setThread(thread);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return post;
    }

    public Post updateById(Integer id, Post updPost, Post oldPost) {

        if(updPost.getMessage() == null || updPost.getMessage().equals(oldPost.getMessage())) {
            return getById(id);
        }

        StringBuilder sql = new StringBuilder()
                .append("UPDATE public.post " +
                        "SET message = COALESCE(?, message), isEdited = TRUE " +
                        "WHERE post_id = ?");

        try {
            jdbcTemplate.update(sql.toString(), updPost.getMessage(), id);
        } catch (DataAccessException e) {
            return null;
        }

        return getById(id);
    }

    public Integer getRowsCount() {
        Integer count = null;
        final String sql = "SELECT count(*) from public.post";
        try {
            count = jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
            count = 0;
        }
        return count;
    }

    public void clearTable() {
        final String sql = "TRUNCATE TABLE post CASCADE";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    private List<Post> getPostsFromDB(String sql, Integer threadId, Integer limit, Integer since, Boolean desc) {
        ArrayList<Post> posts = new ArrayList<>();
        try {
            List<Map<String, Object>> rows;
            if(since != null)
                rows = jdbcTemplate.queryForList(sql, threadId, since, limit);
            else
                rows = jdbcTemplate.queryForList(sql, threadId, limit);
            for (Map<String, Object> row : rows) {
                String description = null;
                if(row.get("description") != null) {
                    description = row.get("description").toString();
                }
                posts.add(new Post(
                                Integer.parseInt(row.get("post_id").toString()), Integer.parseInt(row.get("votes").toString()),
                                description,
                                Timestamp.valueOf(row.get("created").toString()).toInstant().atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                row.get("message").toString(), row.get("forum").toString(),
                                row.get("author").toString(), Boolean.parseBoolean(row.get("isEdited").toString()),
                                Integer.parseInt(row.get("parent").toString()), Integer.parseInt(row.get("thread").toString())
                        )
                );
            }
        }
        catch (DataAccessException e){
            e.printStackTrace();
            return null;
        }
        return posts;
    }

    private List<Post> flat(Integer threadId, Integer limit, Integer since, Boolean desc) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM post WHERE thread = ? ");

        String sign;
        if (desc) sign = "< ";
        else sign = "> ";

        if (since != null) {
            sqlBuilder.append(" AND post_id").append(sign).append("? ");
        }
        sqlBuilder.append("ORDER BY post_id ");
        if(desc) { sqlBuilder.append("DESC "); }


        if (limit != null) {
            sqlBuilder.append("LIMIT ?");
        }
        return  getPostsFromDB(sqlBuilder.toString(), threadId, limit, since, desc);

    }

    private List<Post> parentTree(Integer threadId, Integer limit, Integer since, Boolean desc) {
        String sign;
        if (desc) sign = "< ";
        else sign = "> ";

        StringBuilder historySqlBuilder = new StringBuilder()
                .append("SELECT post_id FROM post WHERE thread = ? AND parent=0 ");

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM post WHERE ");
        sqlBuilder.append("history[1] = ? ");

        if (since != null) {
            final Integer sinceParentId = jdbcTemplate.queryForObject(historyIdByPostId, Integer.class, since);
            historySqlBuilder.append(" AND post_id ").append(sign).append(sinceParentId.toString());
        }
        historySqlBuilder.append("ORDER BY post_id ");
        if(desc) { historySqlBuilder.append("DESC "); }

        if (limit != null) {
            historySqlBuilder.append(" LIMIT ?");
        }
        historySqlBuilder.append(";");


        sqlBuilder.append("ORDER BY history[1] ");
        if(desc) { sqlBuilder.append("DESC "); }
        sqlBuilder.append(", history");

        String historySql = historySqlBuilder.toString();
        String sql = sqlBuilder.toString();

        ArrayList<Post> posts = new ArrayList<>();
        List<Map<String, Object>> historyRows;
        try {
            historyRows = jdbcTemplate.queryForList(historySql, threadId ,limit);
            for (Map<String, Object> history: historyRows) {
                Integer historyId = Integer.parseInt(history.get("post_id").toString());

                List<Map<String, Object>> rows;
                rows = jdbcTemplate.queryForList(sql, historyId);
                for (Map<String, Object> row : rows) {
                    String description = null;
                    if (row.get("description") != null) {
                        description = row.get("description").toString();
                    }
                    posts.add(new Post(
                                    Integer.parseInt(row.get("post_id").toString()), Integer.parseInt(row.get("votes").toString()),
                                    description,
                                    Timestamp.valueOf(row.get("created").toString()).toInstant().atZone(ZoneId.systemDefault())
                                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                    row.get("message").toString(), row.get("forum").toString(),
                                    row.get("author").toString(), Boolean.parseBoolean(row.get("isEdited").toString()),
                                    Integer.parseInt(row.get("parent").toString()), Integer.parseInt(row.get("thread").toString())
                            )
                    );
                }
            }
        }
        catch (DataAccessException e){
            e.printStackTrace();
            return null;
        }
        return posts;


//        return  getPostsFromDB(sqlBuilder.toString(), threadId, limit, null, desc);

    }

    private List<Post> tree(Integer threadId, Integer limit, Integer since, Boolean desc) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM post WHERE thread = ? ");

        String sign;
        if (desc) sign = "< ";
        else sign = "> ";

        if (since != null) {
            sqlBuilder.append(" AND history ").append(sign).append("(SELECT history FROM post WHERE post_id = ?) ");
        }
        sqlBuilder.append(" ORDER BY history ");
        if(desc) { sqlBuilder.append("DESC "); }

        if (limit != null) {
            sqlBuilder.append("LIMIT ?");
        }

        return  getPostsFromDB(sqlBuilder.toString(), threadId, limit, since, desc);
    }

    public  List<Post> getPostsInThread(Integer threadId, Integer limit, String sort, Integer since, Boolean desc) {
        List<Post> posts = null;

        if(sort.equals("flat")){ posts = flat(threadId, limit, since, desc); }
        if(sort.equals("tree")){ posts = tree(threadId, limit, since, desc); }
        if(sort.equals("parent_tree")){ posts = parentTree(threadId, limit, since, desc); }

        return posts;
    }
}
