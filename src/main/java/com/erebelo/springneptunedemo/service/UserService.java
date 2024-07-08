package com.erebelo.springneptunedemo.service;

import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(String id);

    UserResponse insert(UserRequest request);

    UserResponse update(String id, UserRequest request);

    void delete(String id);

    FollowResponse follow(String fromId, String toId, FollowRequest request);

    void unfollow(String fromId, String toId);

}
