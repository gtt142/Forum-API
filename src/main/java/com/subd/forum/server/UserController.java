package com.subd.forum.server;

import com.subd.forum.mappers.UserMapper;
import com.subd.forum.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {
    private final JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping(value = "/api/user/{nickname}/create", produces = "application/json")
    public ResponseEntity userCreate(@RequestBody User user, @PathVariable("nickname") String nickname) {
        user.setNickname(nickname);
        if (user.filled() && user.valid()) {
            User userN = null;
            try {
                userN = this.jdbcTemplate.queryForObject(
                        "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE nickname ILIKE ?",
                        new Object[]{user.getNickname()}, new UserMapper());
            } catch (EmptyResultDataAccessException e) {
                userN = null;
            } catch (DataAccessException e) {
                e.printStackTrace();
            }

            User userE = null;
            try {
                userE = this.jdbcTemplate.queryForObject(
                        "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE email ILIKE ?",
                        new Object[]{user.getEmail()}, new UserMapper());
            } catch (EmptyResultDataAccessException e) {
                userE = null;
            } catch (DataAccessException e) {
                e.printStackTrace();
            }

            if (userE != null || userN != null) {
                User[] users = null;
                if (userE == null) {
                    users = new User[1];
                    users[0] = userN;
                }
                if (userN == null) {
                    users = new User[1];
                    users[0] = userE;
                }
                if (userE != null && userN != null) {
                    if (!userE.getId().equals(userN.getId())) {
                        users = new User[2];
                        users[0] = userN;
                        users[1] = userE;
                    } else {
                        users = new User[1];
                        users[0] = userE;
                    }
                }
                return ResponseEntity.status(HttpStatus.CONFLICT).body(users);
            }

            this.jdbcTemplate.update(
                    "INSERT INTO public.\"users\" (nickname, fullname, email, about) values (?, ?, ?, ?)",
                    nickname, user.getFullname(), user.getEmail(), user.getAbout());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\": \"not valid format\"}");
    }

    @GetMapping(value = "/api/user/{nickname}/profile")
    public ResponseEntity userGet(@PathVariable("nickname") String nickname) {
        User user = null;
        try {
            user = this.jdbcTemplate.queryForObject(
                    "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE nickname ILIKE ?",
                    new Object[]{nickname}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find user with id #42\"}");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PostMapping(value = "/api/user/{nickname}/profile")
    public ResponseEntity userUpdate(@RequestBody User user, @PathVariable("nickname") String nickname) {
        User userN = null;
        try {
            userN = this.jdbcTemplate.queryForObject(
                    "SELECT nickname FROM public.\"users\" WHERE nickname ILIKE ? OR email ILIKE ?",
                    new Object[]{user.getNickname(), user.getEmail()},
                    (rs, rwNumber) -> new User(rs.getString("nickname")));
        } catch (EmptyResultDataAccessException e) {
            userN = null;
        } catch (DataAccessException e) {
            e.printStackTrace();
        }

        if (userN != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\": \"This info is already registered\"}");
        }

        User userRes = null;
        this.jdbcTemplate.update(
                "UPDATE public.\"users\"\n" +
                        "SET nickname = COALESCE(?, nickname),\n" +
                        "  email = COALESCE(?, email),\n" +
                        "  fullname = COALESCE(?, fullname),\n" +
                        "  about = COALESCE(?, about)\n" +
                        "WHERE nickname ILIKE ?;",
                user.getNickname(), user.getEmail(), user.getFullname(), user.getAbout(), nickname);
        try {
            userRes = this.jdbcTemplate.queryForObject(
                    "SELECT user_id, nickname, fullname, email, about FROM public.\"users\" WHERE nickname ILIKE ?",
                    new Object[]{nickname}, new UserMapper());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\": \"Can't find user with id #42\"}");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.OK).body(userRes);
    }
}
