package com.subd.forum.server;


import com.subd.forum.DAO.*;
import com.subd.forum.models.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {
    private final ServiceDAO serviceDAO;

    @Autowired
    public ServiceController(ServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    @GetMapping(value = "/api/service/status")
    public ResponseEntity statusDB() {
        Status status = serviceDAO.statusDB();
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }


    @PostMapping(value = "/api/service/clear")
    public ResponseEntity clearDB() {
        serviceDAO.clearDB();
        Status status = serviceDAO.statusDB();
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }
}
