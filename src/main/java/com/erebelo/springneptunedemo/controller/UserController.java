package com.erebelo.springneptunedemo.controller;

import static com.erebelo.springneptunedemo.constant.BusinessConstant.MERGE_PATCH_MEDIA_TYPE;
import static com.erebelo.springneptunedemo.constant.BusinessConstant.USERS_FOLLOW_PATH;
import static com.erebelo.springneptunedemo.constant.BusinessConstant.USERS_PATH;
import static com.erebelo.springneptunedemo.constant.BusinessConstant.USERS_UNFOLLOW_PATH;

import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.edge.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.node.UserResponse;
import com.erebelo.springneptunedemo.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Validated
@RestController
@RequestMapping(USERS_PATH)
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserResponse>> findAll(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "addressState", required = false) String addressState,
            @Min(1) @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit,
            @Min(1) @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        log.info("Getting all users with params: [name: {}, addressState: {}, limit: {}, page: {}]", name, addressState,
                limit, page);
        return ResponseEntity.ok(service.findAll(name, addressState, limit, page));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        log.info("Getting user by id: {}", id);
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> insert(@Valid @RequestBody UserRequest request) {
        log.info("Inserting user: {}", request);
        var response = service.insert(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        log.info("Updating user by id: {} {}", id, request);
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping(value = "/{id}", consumes = MERGE_PATCH_MEDIA_TYPE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> patch(@PathVariable String id,
            @Valid @RequestBody Map<String, Object> requestMap) {
        log.info("Patching user by id: {} {}", id, requestMap.toString());
        return ResponseEntity.ok(service.patch(id, requestMap));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting user by id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(USERS_FOLLOW_PATH)
    public ResponseEntity<FollowResponse> follow(@PathVariable String fromId, @PathVariable String toId,
            @Valid @RequestBody FollowRequest request) {
        log.info("Creating FOLLOW edge from user id: {} to user id: {} by object: {}", fromId, toId, request);
        return ResponseEntity.ok(service.follow(fromId, toId, request));
    }

    @DeleteMapping(USERS_UNFOLLOW_PATH)
    public ResponseEntity<Void> unfollow(@PathVariable String fromId, @PathVariable String toId) {
        log.info("User id: {} unfollowing user id: {}", fromId, toId);
        service.unfollow(fromId, toId);
        return ResponseEntity.noContent().build();
    }
}
