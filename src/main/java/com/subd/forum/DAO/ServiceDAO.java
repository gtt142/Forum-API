package com.subd.forum.DAO;


import com.subd.forum.models.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceDAO {
    private final UserDAO userDAO;
    private final ForumDAO forumDAO;
    private final ThreadDAO threadDAO;
    private final PostDAO postDAO;

    @Autowired
    public ServiceDAO(UserDAO userDAO, ForumDAO forumDAO, ThreadDAO threadDAO, PostDAO postDAO){

        this.userDAO = userDAO;
        this.forumDAO = forumDAO;
        this.threadDAO = threadDAO;
        this.postDAO = postDAO;
    }

    public Status statusDB() {
        Status status = new Status();
        status.setForum(forumDAO.getRowsCount());
        status.setPost(postDAO.getRowsCount());
        status.setThread(threadDAO.getRowsCount());
        status.setUser(userDAO.getRowsCount());

        return status;
    }

    public void clearDB() {
        forumDAO.clearTable();
        threadDAO.clearTable();
        postDAO.clearTable();
        userDAO.clearTable();
    }

}
