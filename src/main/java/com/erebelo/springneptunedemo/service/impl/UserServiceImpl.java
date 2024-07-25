package com.erebelo.springneptunedemo.service.impl;

import com.erebelo.springneptunedemo.domain.graph.node.UserAddress;
import com.erebelo.springneptunedemo.domain.request.FollowRequest;
import com.erebelo.springneptunedemo.domain.request.UserAddressRequest;
import com.erebelo.springneptunedemo.domain.request.UserRequest;
import com.erebelo.springneptunedemo.domain.response.edge.FollowResponse;
import com.erebelo.springneptunedemo.domain.response.node.UserResponse;
import com.erebelo.springneptunedemo.exception.model.BadRequestException;
import com.erebelo.springneptunedemo.mapper.UserMapper;
import com.erebelo.springneptunedemo.repository.UserRepository;
import com.erebelo.springneptunedemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

import static com.erebelo.springneptunedemo.util.ObjectMapperUtil.objectMapper;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    private static final String ADDRESS_PROPERTY = "address";

    private static final String INVALID_PAYLOAD_ERROR_MESSAGE = "Request body is mandatory and must contain some attribute";

    @Override
    public List<UserResponse> findAll(String name, String addressState, Integer limit, Integer page) {
        var nodeList = repository.findAll(name, addressState, limit, page);
        return mapper.nodeListToResponseList(nodeList);
    }

    @Override
    public UserResponse findById(String id) {
        var node = repository.findById(id);
        var followers = repository.findEdgesByUserIdAndDirection(id, Direction.IN);
        var following = repository.findEdgesByUserIdAndDirection(id, Direction.OUT);

        var response = mapper.nodeToResponse(node);
        response.setFollowers(mapper.edgeListToLazyFollowResponseList(followers, Direction.OUT.name()));
        response.setFollowing(mapper.edgeListToLazyFollowResponseList(following, Direction.IN.name()));

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
        // Objects nested in the request dto classes must be instantiated for the update request to work properly
        if (request.getAddress() == null) {
            request.setAddress(new UserAddressRequest());
        }

        var node = mapper.requestToNode(request);
        node = repository.update(id, node);

        return mapper.nodeToResponse(node);
    }

    @Override
    public UserResponse patch(String id, Map<String, Object> requestMap) {
        if (ObjectUtils.isEmpty(requestMap)) {
            throw new BadRequestException(INVALID_PAYLOAD_ERROR_MESSAGE);
        }

        if (requestMap.containsKey(ADDRESS_PROPERTY) && requestMap.get(ADDRESS_PROPERTY) == null) {
            requestMap.put(ADDRESS_PROPERTY, objectMapper.convertValue(new UserAddress(), Map.class));
        }

        var node = repository.patch(id, requestMap);
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
}
