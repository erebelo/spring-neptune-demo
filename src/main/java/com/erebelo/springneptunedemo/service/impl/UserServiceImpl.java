package com.erebelo.springneptunedemo.service.impl;

import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import com.erebelo.springneptunedemo.mapper.UserMapper;
import com.erebelo.springneptunedemo.repository.UserRepository;
import com.erebelo.springneptunedemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public List<UserResponse> findAll() {
        var nodeList = repository.findAll();
        return mapper.nodeListToResponseList(nodeList);
    }

    @Override
    public UserResponse findById(String id) {
        var node = repository.findById(id);
        return mapper.nodeToResponse(node);
    }

    @Override
    public UserResponse insert(UserRequest request) {
        var node = mapper.requestToNode(request);
        node = repository.insert(node);

        return mapper.nodeToResponse(node);
    }

    @Override
    public UserResponse update(String id, UserRequest request) {
        var node = mapper.requestToNode(request);
        node = repository.update(id, node);

        return mapper.nodeToResponse(node);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public FollowResponse follow(String fromId, String toId, FollowRequest request) {
        var relationship = mapper.requestToRelationship(request);
        relationship = repository.createRelationship(fromId, toId, relationship);

        return mapper.relationshipToResponse(relationship);
    }

    @Override
    public void unfollow(String fromId, String toId) {
        repository.removeRelationship(fromId, toId);
    }
}
