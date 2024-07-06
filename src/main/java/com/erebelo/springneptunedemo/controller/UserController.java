package com.erebelo.springneptunedemo.controller;

import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import com.erebelo.springneptunedemo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> findAll() {
        LOGGER.info("Getting all users");
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        LOGGER.info("Getting user by id: {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> insert(@Valid @RequestBody UserRequest request) {
        LOGGER.info("Inserting user: {}", request);
        var response = service.insert(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        LOGGER.info("Updating user by id: {} {}", id, request);
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        LOGGER.info("Deleting user by id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id1}/follow/{id2}")
    public ResponseEntity<Void> followUser(@PathVariable String id1, @PathVariable String id2) {
        LOGGER.info("User id: {} following the user id: {}", id1, id2);
        service.followUser(id1, id2);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id1}/unfollow/{id2}")
    public ResponseEntity<Void> unfollowUser(@PathVariable String id1, @PathVariable String id2) {
        LOGGER.info("User id: {} unfollowing the user id: {}", id1, id2);
        service.unfollowUser(id1, id2);
        return ResponseEntity.noContent().build();
    }
}
