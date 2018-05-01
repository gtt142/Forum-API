package com.subd.forum.models;

public class Status {
    private Integer forum;
    private Integer thread;
    private Integer post;
    private Integer user;

    public Status() {
    }

    public Status(Integer forum, Integer thread, Integer post, Integer user) {
        this.forum = forum;
        this.thread = thread;
        this.post = post;
        this.user = user;
    }

    public Integer getForum() {
        return forum;
    }

    public void setForum(Integer forum) {
        this.forum = forum;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Integer getPost() {
        return post;
    }

    public void setPost(Integer post) {
        this.post = post;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }
}
