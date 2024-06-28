package com.erebelo.springneptunedemo.service.impl;

import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.UserLazyResponse;
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
    public List<UserLazyResponse> findAll() {
        var nodeList = repository.findAll();
        return mapper.lazyNodeListToResponseList(nodeList);
    }

    @Override
    public UserResponse findById(String id) {
        var node = repository.findById(id).orElse(null);
        return mapper.nodeToResponse(node);
    }

    @Override
    public UserLazyResponse insert(UserRequest request) {
        var node = mapper.requestToNode(request);
        node = repository.insert(node);

        return mapper.lazyNodeToResponse(node);
    }

    @Override
    public UserLazyResponse update(String id, UserRequest request) {
        var node = mapper.requestToNode(request);
        node.setId(id);
        node = repository.update(node);

        return mapper.lazyNodeToResponse(node);
    }

    @Override
    public void delete(String id) {
        repository.deleteById(id);
    }

    @Override
    public void followUser(String id1, String id2) {
        repository.saveRelationships(id1, id2);
    }

    @Override
    public void unfollowUser(String id1, String id2) {
        repository.removeRelationship(id1, id2);
    }
}
