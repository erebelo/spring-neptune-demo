package com.erebelo.springneptunedemo.service.impl;

import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.UserResponse;
import com.erebelo.springneptunedemo.mapper.UserMapper;
import com.erebelo.springneptunedemo.repository.UserRepository;
import com.erebelo.springneptunedemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Direction;
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

        List<FollowResponse> followers = repository.findEdgesByUserIdAndDirection(id, Direction.IN);
        List<FollowResponse> following = repository.findEdgesByUserIdAndDirection(id, Direction.OUT);

        // Parsing followers by OUT direction and following by IN direction
        followers.forEach(f -> setUserBasedOnDirection(f, Direction.OUT));
        following.forEach(f -> setUserBasedOnDirection(f, Direction.IN));

        var response = mapper.nodeToResponse(node);
        response.setFollowers(mapper.edgeListToUserFollowResponseList(followers, Direction.OUT.name()));
        response.setFollowing(mapper.edgeListToUserFollowResponseList(following, Direction.IN.name()));

        return response;
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
        var edge = mapper.requestToEdge(request);
        edge = repository.createEdge(fromId, toId, edge);

        return mapper.edgeToResponse(edge);
    }

    @Override
    public void unfollow(String fromId, String toId) {
        repository.removeEdge(fromId, toId);
    }

    private void setUserBasedOnDirection(FollowResponse followResponse, Direction direction) {
        if (direction == Direction.OUT && followResponse.getOut() != null) {
            followResponse.setOut(mapper.nodeToLazyResponse(repository.findById(followResponse.getOut().getId())));
        } else if (direction == Direction.IN && followResponse.getIn() != null) {
            followResponse.setIn(mapper.nodeToLazyResponse(repository.findById(followResponse.getIn().getId())));
        }
    }
}
