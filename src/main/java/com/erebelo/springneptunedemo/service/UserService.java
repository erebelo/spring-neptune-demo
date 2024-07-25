package com.erebelo.springneptunedemo.service;

import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.edge.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.node.UserResponse;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserResponse> findAll(String name, String addressState, Integer limit, Integer page);

    UserResponse findById(String id);

    UserResponse insert(UserRequest request);

    UserResponse update(String id, UserRequest request);

    UserResponse patch(String id, Map<String, Object> requestMap);

    void delete(String id);

    FollowResponse follow(String fromId, String toId, FollowRequest request);

    void unfollow(String fromId, String toId);

}
