package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.graph.edge.FollowEdge;
import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

public interface UserRepository {

    List<UserNode> findAll();

    UserNode findById(String id);

    UserNode insert(UserNode node);

    UserNode update(String id, UserNode node);

    void deleteById(String id);

    List<FollowEdge> findEdgesByUserIdAndDirection(String userId, Direction direction);

    FollowEdge createEdge(String fromId, String toId, FollowEdge edge);

    void removeEdge(String fromId, String toId);

}
