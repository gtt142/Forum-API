package com.subd.forum.server;

import com.subd.forum.DAO.ForumDAO;
import com.subd.forum.DAO.ThreadDAO;
import com.subd.forum.DAO.UserDAO;
import com.subd.forum.mappers.ForumMapper;
import com.subd.forum.mappers.UserMapper;
import com.subd.forum.models.Forum;
import com.subd.forum.models.Thread;
import com.subd.forum.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ForumController {
    private final JdbcTemplate jdbcTemplate;
    private final ForumDAO forumDAO;
    private final UserDAO userDAO;
    private final ThreadDAO threadDAO;


    public ForumController(JdbcTemplate jdbcTemplate, ForumDAO forumDAO, ThreadDAO threadDAO, UserDAO userDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.userDAO = userDAO;
    }

    final String selectUsersByNickname = "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE LOWER(nickname) = LOWER(?)";
    final String selectFromForumBySlug = "SELECT * FROM public.forum WHERE LOWER(slug) = LOWER(?)";
    final String insertForum = "INSERT INTO public.forum (description, slug, title, username) VALUES (?, ?, ?, ?)";

    @PostMapping(value = "/api/forum/create", produces = "application/json")
    public ResponseEntity forumCreate(@RequestBody Forum forum) {
        User user = null;
        try {
            user = this.jdbcTemplate.queryForObject(
                    selectUsersByNickname,
                    new Object[]{forum.getUser()}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"user doesn't exist\"}");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        Forum forumExist = null;
        try {
            forumExist = this.jdbcTemplate.queryForObject(
                    selectFromForumBySlug,
                    new Object[]{forum.getSlug()}, new ForumMapper());
        } catch (EmptyResultDataAccessException e) {
            forumExist = null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        if (forumExist != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumExist);
        }

        forum.setUser(user.getNickname());

        try {
            this.jdbcTemplate.update(
                    insertForum,
                    forum.getDescription(), forum.getSlug(), forum.getTitle(), forum.getUser());
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"DB Insert error\"}");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(forum);
    }

    @GetMapping(value = "/api/forum/{slug}/details")
    public ResponseEntity forumGet(@PathVariable("slug") String slug) {
        Forum forum = null;
        try {
            forum = this.jdbcTemplate.queryForObject(
                    selectFromForumBySlug,
                    new Object[]{slug}, new ForumMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find forum\"}");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    @GetMapping(value = "/api/forum/{slug}/threads")
    public ResponseEntity forumGetThreads(@PathVariable("slug") String slug,
                                  @RequestParam(name = "limit", required = false) Integer limit,
                                  @RequestParam(name = "since", required = false) String since,
                                  @RequestParam(name = "desc", required = false) Boolean desc) {

        if(!forumDAO.isExistBySlug(slug)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find forum\"}");
        }

        if(desc == null){ desc = false; }

        List<Thread> threads = threadDAO.getThreadByForum(slug, limit, since, desc);

        if(threads == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find threads in forum\"}");
        }

        return ResponseEntity.status(HttpStatus.OK).body(threads);

    }

    @GetMapping(value = "/api/forum/{slug}/users")
    public ResponseEntity forumGetUsers(@PathVariable("slug") String slug,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "since", required = false) String since,
                                   @RequestParam(name = "desc", required = false) Boolean desc) {
        Integer forumId = forumDAO.getForumIdBySlug(slug);
        if(forumId == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find forum\"}");
        }

        if(desc == null){ desc = false; }

        List<User> users = userDAO.getUsersByForum(forumId, limit, since, desc);

        if(users == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find users in forum\"}");
        }

        return ResponseEntity.status(HttpStatus.OK).body(users);
    }


}
