package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.domain.graph.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

public interface UserRepository {

    List<UserNode> findAll();

    UserNode findById(String id);

    UserNode insert(UserNode node);

    UserNode update(String id, UserNode node);

    void deleteById(String id);

    List<FollowResponse> findRelationshipsByUserIdAndDirection(String userId, Direction direction);

    FollowRelationship createRelationship(String fromId, String toId, FollowRelationship relationship);

    void removeRelationship(String fromId, String toId);

}
