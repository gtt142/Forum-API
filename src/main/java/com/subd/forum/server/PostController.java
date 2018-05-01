package com.subd.forum.server;

import com.subd.forum.DAO.ForumDAO;
import com.subd.forum.DAO.PostDAO;
import com.subd.forum.DAO.ThreadDAO;
import com.subd.forum.DAO.UserDAO;
import com.subd.forum.models.Post;
import com.subd.forum.models.PostDetail;
import com.subd.forum.models.Thread;
import com.subd.forum.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

@RestController
public class PostController {
    private final PostDAO postDAO;
    private final ThreadDAO threadDAO;
    private final UserDAO userDAO;
    private final ForumDAO forumDAO;

    @Autowired
    public PostController(PostDAO postDAO, ThreadDAO threadDAO, UserDAO userDAO, ForumDAO forumDAO) {
        this.postDAO = postDAO;
        this.threadDAO = threadDAO;
        this.userDAO = userDAO;
        this.forumDAO = forumDAO;
    }

    @PostMapping(value = "/api/thread/{slug_or_id}/create", produces = "application/json")
    public ResponseEntity postCreate(@RequestBody List<Post> posts, @PathVariable("slug_or_id") String slugOrId) {
        Thread thread;
        if(isNumeric(slugOrId)) {
            thread = threadDAO.getById(Integer.parseInt(slugOrId));
        } else
            thread = threadDAO.getBySlug(slugOrId);

        if(thread == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
        }

        Date date = new Date();
        Timestamp curTime = new Timestamp(date.getTime());
        String time = curTime.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        for(Post newPost: posts) {
            if(newPost.getAuthor() != null) {
                User user = userDAO.getByName(newPost.getAuthor());
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"user unregistered\"}");
                }
            }

            if(newPost.getParent() == null) {
                newPost.setParent(0);
            }
            if(newPost.getParent() != 0) {

                Post parent = postDAO.getById(newPost.getParent());
                if (parent == null || !thread.getId().equals(parent.getThread())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\": \"parent id is wrong\"}");
                }
            }
            newPost.setThread(thread.getId());
            newPost.setForum(thread.getForum());
            newPost.setEdited(false);
            if(newPost.getCreated() == null) {
                newPost.setCreated(time);
            }
        }

        List<Post> newPosts;
        newPosts = postDAO.addPosts(posts);
        if (newPosts == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"posts doesn't exist\"}");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(newPosts);
    }

    @GetMapping(value = "/api/post/{id}/details")
    public ResponseEntity postGet(@PathVariable("id") Integer id, @RequestParam(name = "related", required = false) Set<String> related) {

        PostDetail postDetail = new PostDetail();
        postDetail.setPost(postDAO.getById(id));
        if (postDetail.getPost() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"posts doesn't exist\"}");
        }

        if(related != null) {

            if(related.contains("thread")) {
                postDetail.setThread(threadDAO.getById(postDetail.getPost().getThread()));
                if(postDetail.getThread() == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"thread doesn't exist\"}");
                }
            }

            if(related.contains("forum")) {
                postDetail.setForum(forumDAO.getBySlug(postDetail.getPost().getForum()));
                if(postDetail.getForum() == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"forum doesn't exist\"}");
                }
            }

            if(related.contains("user")) {

                postDetail.setAuthor(userDAO.getByName(postDetail.getPost().getAuthor()));
                if(postDetail.getAuthor() == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"user doesn't exist\"}");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(postDetail);
    }

    @PostMapping(value = "/api/post/{id}/details", produces = "application/json")
    public ResponseEntity threadUpdate(@RequestBody Post updPost, @PathVariable("id") Integer id) {
        Post post = postDAO.getById(id);
        if(post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"post doesn't exist\"}");
        }

        Post resPost = postDAO.updateById(post.getId(), updPost, post);
        if(resPost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"post doesn't exist\"}");
        }
        return ResponseEntity.status(HttpStatus.OK).body(resPost);
    }


    public static Boolean isNumeric(String numStr)
    {
        return numStr.matches("-?\\d+(\\.\\d+)?");
    }
}
