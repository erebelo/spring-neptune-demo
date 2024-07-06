package com.erebelo.springneptunedemo.service;

import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    UserResponse findById(String id);

    UserResponse insert(UserRequest request);

    UserResponse update(String id, UserRequest request);

    void delete(String id);

    void followUser(String id1, String id2);

    void unfollowUser(String id1, String id2);

}
