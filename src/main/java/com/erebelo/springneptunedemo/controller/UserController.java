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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Validated
@RestController
@RequestMapping(USERS_PATH)
@RequiredArgsConstructor
@Tag(name = "Users API")
public class UserController {

    private final UserService service;

    @Operation(summary = "GET Users")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserResponse> findAll(@RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "addressState", required = false) String addressState,
            @Min(1) @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit,
            @Min(1) @RequestParam(value = "page", required = false, defaultValue = "1") Integer page) {
        log.info("GET {}", USERS_PATH);
        return service.findAll(name, addressState, limit, page);
    }

    @Operation(summary = "GET User by Id")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse findById(@PathVariable String id) {
        log.info("GET {}/{}", USERS_PATH, id);
        return service.findById(id);
    }

    @Operation(summary = "POST Users")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse insert(@Valid @RequestBody UserRequest request, HttpServletResponse httpServletResponse) {
        log.info("POST {}", USERS_PATH);
        UserResponse response = service.insert(request);
        httpServletResponse.setHeader(HttpHeaders.LOCATION, ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.getId()).toUri().toString());
        return response;
    }

    @Operation(summary = "PUT Users")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UserRequest request) {
        log.info("PUT {}/{}", USERS_PATH, id);
        return service.update(id, request);
    }

    @Operation(summary = "PATCH Users")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", consumes = MERGE_PATCH_MEDIA_TYPE, produces = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse patch(@PathVariable String id, @Valid @RequestBody Map<String, Object> requestMap) {
        log.info("PATCH {}/{}", USERS_PATH, id);
        return service.patch(id, requestMap);
    }

    @Operation(summary = "DELETE Users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@PathVariable String id) {
        log.info("DELETE {}/{}", USERS_PATH, id);
        service.delete(id);
    }

    @Operation(summary = "POST Follow Users")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{fromId}" + USERS_FOLLOW_PATH + "/{toId}")
    public FollowResponse follow(@PathVariable String fromId, @PathVariable String toId,
            @Valid @RequestBody FollowRequest request) {
        log.info("POST {}{}", USERS_PATH, "/" + fromId + USERS_FOLLOW_PATH + "/" + toId);
        return service.follow(fromId, toId, request);
    }

    @Operation(summary = "POST Unfollow Users")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{fromId}" + USERS_UNFOLLOW_PATH + "/{toId}")
    public void unfollow(@PathVariable String fromId, @PathVariable String toId) {
        log.info("DELETE {}{}", USERS_PATH, "/" + fromId + USERS_UNFOLLOW_PATH + "/" + toId);
        service.unfollow(fromId, toId);
    }
}
