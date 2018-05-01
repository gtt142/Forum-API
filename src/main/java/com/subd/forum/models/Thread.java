package com.subd.forum.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class Thread {
    private Integer id;
    private Integer votes;
    private String  slug;
    private String  description;
//    @JsonFormat(timezone = "UTC", pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private String created;
    private String  message;
    private String  forum;
    private String  title;
    private String  author;


    public Thread() {
    }

    public Thread(Integer id, Integer votes, String description, String created, String message, String forum, String slug, String title, String author) {
        this.id = id;
        this.votes = votes;
        this.description = description;
        this.created = created;
        this.message = message;
        this.forum = forum;
        this.slug = slug;
        this.title = title;
        this.author = author;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
