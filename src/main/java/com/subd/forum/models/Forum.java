package com.subd.forum.models;

public class Forum {
    private Integer id;
    private Integer posts;
    private Integer threads;
    private String  description;
    private String  slug;
    private String  title;
    private String  user;

    public Forum() {
    }

    public Forum(Integer id, Integer posts, Integer threads, String description, String slug, String title, String user) {
        this.id = id;
        this.posts = posts;
        this.threads = threads;
        this.description = description;
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public Forum(String description, String slug, String title, String user) {
        this.description = description;
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String username) {
        this.user = username;
    }

}
