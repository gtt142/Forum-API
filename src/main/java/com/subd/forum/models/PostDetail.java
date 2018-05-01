package com.subd.forum.models;

public class PostDetail {
    private Post post;
    private User author;
    private Thread thread;
    private Forum forum;

    public PostDetail() {
    }

    public PostDetail(Post post, User author, Thread thread, Forum forum) {
        this.post = post;
        this.author = author;
        this.thread = thread;
        this.forum = forum;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }
}
