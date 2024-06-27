package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.node.UserNode;
import com.erebelo.springneptunedemo.domain.relationship.FollowRelationship;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserRepository {

    private final GraphTraversalSource traversalSource;

    @Autowired
    public UserRepository(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    public List<UserNode> findAll() {
        List<UserNode> userList = new ArrayList<>();
        traversalSource.V().hasLabel("User").forEachRemaining(vertex -> {
            UserNode userNode = mapVertexToUserNode(vertex);
            userList.add(userNode);
        });
        return userList;
    }

    public Optional<UserNode> findById(String id) {
        return Optional.ofNullable(traversalSource.V().hasLabel("User").has("id", id).tryNext())
                .map(vertex -> vertex.isPresent() ? mapVertexToUserNode(vertex.get()) : null);
    }

    public UserNode save(UserNode user) {
        // Assuming user id is Long type
        Vertex vertex = traversalSource.addV("User")
                .property("id", user.getId() == null ? UUID.randomUUID().toString() : user.getId())
                .property("username", user.getUsername())
                .property("name", user.getName())
                .next();

        saveRelationships(user);

        return mapVertexToUserNode(vertex);
    }

    private void saveRelationships(UserNode user) {
        if (!ObjectUtils.isEmpty(user.getFollowing())) {
            // Save following relationships
            user.getFollowing().forEach(relationship -> {
                traversalSource.V().hasLabel("User").has("id", user.getId())
                        .addE("FOLLOWS")
                        .to(traversalSource.V().hasLabel("User").has("id", relationship.getUser().getId()))
                        .property("id", relationship.getId())
                        .property("sinceAt", relationship.getSinceAt())
                        .next();
            });
        }

        if (!ObjectUtils.isEmpty(user.getFollowers())) {
            // Save follower relationships
            user.getFollowers().forEach(relationship -> {
                traversalSource.V().hasLabel("User").has("id", user.getId())
                        .addE("FOLLOWED_BY")
                        .to(traversalSource.V().hasLabel("User").has("id", relationship.getUser().getId()))
                        .property("id", relationship.getId())
                        .property("sinceAt", relationship.getSinceAt())
                        .next();
            });
        }
    }

    public void deleteById(String id) {
        // Delete user vertex and associated relationships
        traversalSource.V().hasLabel("User").has("id", id).drop().iterate();
    }

    private UserNode mapVertexToUserNode(Vertex vertex) {
        UserNode userNode = new UserNode();
        userNode.setId(vertex.value("id"));
        userNode.setUsername(vertex.value("username"));
        userNode.setName(vertex.value("name"));

        // Map relationships
        userNode.setFollowing(mapFollowRelationships(vertex, "FOLLOWS"));
        userNode.setFollowers(mapFollowRelationships(vertex, "FOLLOWED_BY"));

        return userNode;
    }

    private Set<FollowRelationship> mapFollowRelationships(Vertex vertex, String relationshipLabel) {
        Set<FollowRelationship> relationships = new HashSet<>();
        vertex.edges(Direction.OUT, relationshipLabel).forEachRemaining(edge -> {
            FollowRelationship followRelationship = new FollowRelationship();
            followRelationship.setId(edge.value("id"));
            followRelationship.setSinceAt(edge.value("sinceAt"));

            // Assuming UserNode is the other end of the relationship
            followRelationship.setUser(mapVertexToUserNode(edge.inVertex()));

            relationships.add(followRelationship);
        });
        return relationships;
    }
}
