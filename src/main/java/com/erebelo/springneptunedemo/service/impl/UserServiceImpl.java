package com.erebelo.springneptunedemo.service.impl;

import static com.erebelo.springneptunedemo.util.ObjectMapperUtil.objectMapper;

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
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    private static final String ADDRESS_PROPERTY = "address";

    private static final String INVALID_PAYLOAD_ERROR_MESSAGE = "Request body is mandatory and must contain some "
            + "attribute";

    @Override
    public List<UserResponse> findAll(String name, String addressState, Integer limit, Integer page) {
        log.info("Fetching all users by name: {}, addressState: {}, limit: {}, and page: {}", name, addressState, limit,
                page);
        var nodeList = repository.findAll(name, addressState, limit, page);

        log.info("Users successfully retrieved: {}", nodeList);
        return mapper.nodeListToResponseList(nodeList);
    }

    @Override
    public UserResponse findById(String id) {
        log.info("Fetching user by id: {}", id);

        var node = repository.findById(id);
        var followers = repository.findEdgesByUserIdAndDirection(id, Direction.IN);
        var following = repository.findEdgesByUserIdAndDirection(id, Direction.OUT);

        var response = mapper.nodeToResponse(node);
        response.setFollowers(mapper.edgeListToLazyFollowResponseList(followers, Direction.OUT.name()));
        response.setFollowing(mapper.edgeListToLazyFollowResponseList(following, Direction.IN.name()));

        log.info("User successfully retrieved: {}", response);
        return response;
    }

    @Override
    public UserResponse insert(UserRequest request) {
        log.info("Creating user");

        var node = mapper.requestToNode(request);
        node = repository.insert(node);

        log.info("User created successfully: {}", node);
        return mapper.nodeToResponse(node);
    }

    @Override
    public UserResponse update(String id, UserRequest request) {
        log.info("Updating user with id: {}", id);

        // Objects nested in the request dto classes must be instantiated for the update
        // request to work properly
        if (request.getAddress() == null) {
            request.setAddress(new UserAddressRequest());
        }

        var node = mapper.requestToNode(request);
        node = repository.update(id, node);

        log.info("User updated successfully: {}", node);
        return mapper.nodeToResponse(node);
    }

    @Override
    public UserResponse patch(String id, Map<String, Object> requestMap) {
        log.info("Patching user with id: {}", id);

        if (ObjectUtils.isEmpty(requestMap)) {
            throw new BadRequestException(INVALID_PAYLOAD_ERROR_MESSAGE);
        }

        if (requestMap.containsKey(ADDRESS_PROPERTY) && requestMap.get(ADDRESS_PROPERTY) == null) {
            requestMap.put(ADDRESS_PROPERTY, objectMapper.convertValue(new UserAddress(), Map.class));
        }

        var node = repository.patch(id, requestMap);

        log.info("User updated successfully: {}", node);
        return mapper.nodeToResponse(node);
    }

    @Override
    public void delete(String id) {
        log.info("Deleting user with id: {}", id);

        repository.deleteById(id);
        log.info("User deleted successfully");
    }

    @Override
    public FollowResponse follow(String fromId, String toId, FollowRequest request) {
        log.info("User id: {} following user id: {}", fromId, toId);

        var edge = mapper.requestToEdge(request);
        edge = repository.createEdge(fromId, toId, edge);

        log.info("User id: {} followed successfully user id: {}", fromId, toId);
        return mapper.edgeToResponse(edge);
    }

    @Override
    public void unfollow(String fromId, String toId) {
        log.info("User id: {} unfollowing user id: {}", fromId, toId);

        repository.removeEdge(fromId, toId);
        log.info("User id: {} successfully unfollowed user id: {}", fromId, toId);
    }
}
