package com.subd.forum.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {
    private Integer id;
    private Integer votes;
    private String  description;
    private String created;
    private String  message;
    private String  forum;
    private String  author;
    @JsonProperty
    private Boolean isEdited;
    private Integer parent;
    private Integer thread;

    public Post() {
    }

    public Post(Integer id, Integer votes, String description, String created, String message, String forum, String author, Boolean isEdited, Integer parent, Integer thread) {
        this.id = id;
        this.votes = votes;
        this.description = description;
        this.created = created;
        this.message = message;
        this.forum = forum;
        this.author = author;
        this.isEdited = isEdited;
        this.parent = parent;
        this.thread = thread;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
