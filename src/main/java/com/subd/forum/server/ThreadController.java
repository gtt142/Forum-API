package com.subd.forum.server;

import com.subd.forum.DAO.PostDAO;
import com.subd.forum.DAO.ThreadDAO;
import com.subd.forum.DAO.UserDAO;
import com.subd.forum.mappers.ForumMapper;
import com.subd.forum.mappers.ThreadMapper;
import com.subd.forum.mappers.UserMapper;
import com.subd.forum.models.*;
import com.subd.forum.models.Thread;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.List;


@RestController
public class ThreadController {
    private final JdbcTemplate jdbcTemplate;
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;
    private final PostDAO postDAO;
    final Clock clock = Clock.systemDefaultZone();

    public ThreadController(JdbcTemplate jdbcTemplate, ThreadDAO threadDAO, UserDAO userDAO, PostDAO postDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.threadDAO = threadDAO;
        this.userDAO = userDAO;
        this.postDAO = postDAO;
    }

    @PostMapping(value = "/api/forum/{slug}/create", produces = "application/json")
    public ResponseEntity threadCreate(@RequestBody Thread thread, @PathVariable("slug") String slug) {
        User user;
        final String sql = "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE LOWER(nickname) = LOWER(?)";
        try {
            user = this.jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{thread.getAuthor()}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"author doesn't exist\"}");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        Forum forumExist;
        try {
            final String sql2 = "SELECT * FROM public.forum WHERE LOWER(slug) = LOWER(?)";
            forumExist = this.jdbcTemplate.queryForObject(
                    sql2,
                    new Object[]{slug}, new ForumMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"forum doesn't exist\"}");
        } catch (DataAccessException e) {
            forumExist = null;
            e.printStackTrace();
        }

        thread.setForum(forumExist.getSlug());

        Thread threadExist = null;
        try {
            final String sql3 = "SELECT * FROM public.thread WHERE LOWER(slug) = LOWER(?)";
            threadExist = this.jdbcTemplate.queryForObject(
                    sql3,
                    new Object[]{thread.getSlug()}, new ThreadMapper());
        } catch (EmptyResultDataAccessException e) {
            threadExist = null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        if (threadExist != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadExist);
        }

        Thread newThread = threadDAO.add(thread, forumExist.getId());
        if(newThread == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"message\": \"can't create\"}");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newThread);
    }

    @GetMapping(value = "/api/thread/{slug_or_id}/details")
    public ResponseEntity threadGet(@PathVariable("slug_or_id") String slugOrId) {
        Thread thread;
        if(isNumeric(slugOrId)) {
            thread = threadDAO.getById(Integer.parseInt(slugOrId));
        } else
            thread = threadDAO.getBySlug(slugOrId);

        if(thread == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
        }

        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @PostMapping(value = "/api/thread/{slug_or_id}/details", produces = "application/json")
    public ResponseEntity threadUpdate(@RequestBody Thread updThread, @PathVariable("slug_or_id") String slugOrId) {
        Thread thread;
        if(isNumeric(slugOrId)) {
            thread = threadDAO.getById(Integer.parseInt(slugOrId));
        } else
            thread = threadDAO.getBySlug(slugOrId);

        if(thread == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
        }

        Thread resThread = threadDAO.updateById(thread.getId(), updThread);
        return ResponseEntity.status(HttpStatus.OK).body(resThread);
    }

    @PostMapping(value = "/api/thread/{slug_or_id}/vote", produces = "application/json")
    public ResponseEntity threadUpdate(@RequestBody Vote vote, @PathVariable("slug_or_id") String slugOrId) {
        Thread thread;
        if(isNumeric(slugOrId)) {
            thread = threadDAO.getById(Integer.parseInt(slugOrId));
        } else
            thread = threadDAO.getBySlug(slugOrId);
        if(thread == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
        }

        User user = userDAO.getByName(vote.getNickname());

        if(user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"user doesn't exist\"}");
        }

        Thread resThread = threadDAO.vote(thread, user.getId(), vote);
        return ResponseEntity.status(HttpStatus.OK).body(resThread);
    }

    @GetMapping(value = "/api/thread/{slug_or_id}/posts")
    public ResponseEntity forumGet(@PathVariable("slug_or_id") String slugOrId,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "sort", required = false) String sort,
                                   @RequestParam(name = "since", required = false) Integer since,
                                   @RequestParam(name = "desc", required = false) Boolean desc) {

        Thread thread = null;
        if(isNumeric(slugOrId)) {
            thread = threadDAO.getById(Integer.parseInt(slugOrId));
        } else
            thread = threadDAO.getBySlug(slugOrId);

        if(thread == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
        }

        if(sort == null) { sort = "flat"; }
        if(limit == null) { limit = 500; }
        if(desc == null) { desc = false; }

        List<Post> posts;
        posts = postDAO.getPostsInThread(thread.getId(), limit, sort, since, desc);

        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }

    public static Boolean isNumeric(String numStr)
    {
        return numStr.matches("-?\\d+(\\.\\d+)?");
    }
}
