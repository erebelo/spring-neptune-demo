package com.erebelo.springneptunedemo.repository.impl;

import com.erebelo.springneptunedemo.domain.node.UserNode;
import com.erebelo.springneptunedemo.domain.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final GraphTraversalSource traversalSource;
    private final ObjectMapper objectMapper;

    private static final String USER_VERTEX_LABEL = "User";
    private static final String FOLLOWS_EDGE_LABEL = "FOLLOWS";
    private static final String FOLLOWED_BY_EDGE_LABEL = "FOLLOWED_BY";
    private static final String ID_PROPERTY = "id";
    private static final String SINCE_AT_PROPERTY = "sinceAt";

    @Override
    public List<UserNode> findAll() {
        return traversalSource.V().hasLabel(USER_VERTEX_LABEL)
                .toList()
                .stream()
                .map(this::mapVertexToUserNode)
                .toList();
    }

    @Override
    public Optional<UserNode> findById(String id) {
        var vertex = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id).tryNext().orElse(null);
        return Optional.ofNullable(vertex).map(this::mapVertexToUserNode);
    }

    @Override
    public UserNode insert(UserNode user) {
        try {
            var vertex = traversalSource.addV(USER_VERTEX_LABEL).property(ID_PROPERTY, UUID.randomUUID().toString()).next();
            return updateVertexProperties(vertex, user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    @Override
    public UserNode update(UserNode user) {
        var vertex = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, user.getId()).tryNext().orElse(null);
        if (vertex != null) {
            return updateVertexProperties(vertex, user);
        } else {
            throw new IllegalArgumentException("User with id " + user.getId() + " not found.");
        }
    }

    @Override
    public void saveRelationships(String id1, String id2) {
        var vertex1 = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id1).tryNext().orElse(null);
        var vertex2 = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id2).tryNext().orElse(null);

        if (vertex1 != null && vertex2 != null) {
            var localDate = LocalDateTime.now();
            if (!relationshipExists(vertex1, vertex2, FOLLOWS_EDGE_LABEL)) {
                createRelationship(vertex1, vertex2, FOLLOWS_EDGE_LABEL, localDate);
            }
            if (!relationshipExists(vertex2, vertex1, FOLLOWED_BY_EDGE_LABEL)) {
                createRelationship(vertex2, vertex1, FOLLOWED_BY_EDGE_LABEL, localDate);
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void removeRelationship(String id1, String id2) {
        var vertex1 = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id1).tryNext().orElse(null);
        var vertex2 = traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id2).tryNext().orElse(null);

        if (vertex1 != null && vertex2 != null) {
            if (relationshipExists(vertex1, vertex2, FOLLOWS_EDGE_LABEL)) {
                removeRelationship(vertex1, vertex2, FOLLOWS_EDGE_LABEL);
            }
            if (relationshipExists(vertex2, vertex1, FOLLOWED_BY_EDGE_LABEL)) {
                removeRelationship(vertex2, vertex1, FOLLOWED_BY_EDGE_LABEL);
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    @Override
    public void deleteById(String id) {
        // Delete user vertex and associated relationships
        traversalSource.V().hasLabel(USER_VERTEX_LABEL).has(ID_PROPERTY, id).drop().iterate();
    }

    private UserNode updateVertexProperties(Vertex vertex, UserNode user) {
        try {
            // Convert UserNode to Map<String, Object>
            Map<String, Object> properties = objectMapper.convertValue(user, Map.class);

            // Iterate through the map and update vertex properties
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    vertex.property(entry.getKey(), entry.getValue().toString());
                }
            }

            return mapVertexToUserNode(vertex);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to update user properties", e);
        }
    }

    private boolean relationshipExists(Vertex fromVertex, Vertex toVertex, String relationshipLabel) {
        return traversalSource.V(fromVertex.id())
                .outE(relationshipLabel)
                .where(__.inV().hasId(toVertex.id()))
                .hasNext();
    }

    private void createRelationship(Vertex fromVertex, Vertex toVertex, String relationshipLabel, LocalDateTime localDate) {
        traversalSource.V(fromVertex.id())
                .addE(relationshipLabel)
                .to(__.V(toVertex.id()))  // Use __.V() to correctly spawn the child traversal
                .property(ID_PROPERTY, UUID.randomUUID().toString())
                .property(SINCE_AT_PROPERTY, localDate)
                .iterate();
    }

    private void removeRelationship(Vertex fromVertex, Vertex toVertex, String relationshipLabel) {
        traversalSource.V(fromVertex.id())
                .outE(relationshipLabel)
                .where(__.inV().hasId(toVertex.id()))
                .drop()
                .iterate();
    }

    private UserNode mapVertexToUserNode(Vertex userVertex) {
        // Use ObjectMapper to convert Map<String, Object> to UserNode
        var userNode = lazyMapVertexToUserNode(userVertex);
        userNode.setFollowing(mapFollowRelationships(userVertex, FOLLOWS_EDGE_LABEL));
        userNode.setFollowers(mapFollowRelationships(userVertex, FOLLOWED_BY_EDGE_LABEL));

        return userNode;
    }

    private UserNode lazyMapVertexToUserNode(Vertex userVertex) {
        // Convert Vertex properties to a Map<String, Object>
        Map<String, Object> propertiesMap = new HashMap<>();
        userVertex.properties().forEachRemaining(property -> propertiesMap.put(property.key(), property.value()));

        // Use ObjectMapper to convert Map<String, Object> to UserNode
        return objectMapper.convertValue(propertiesMap, UserNode.class);
    }

    private Set<FollowRelationship> mapFollowRelationships(Vertex userVertex, String relationshipLabel) {
        Set<FollowRelationship> relationships = new HashSet<>();
        userVertex.edges(Direction.OUT, relationshipLabel).forEachRemaining(edge -> {
            // Convert edge properties to Map<String, Object>
            Map<String, Object> propertiesMap = new HashMap<>();
            edge.properties().forEachRemaining(property -> propertiesMap.put(property.key(), property.value()));

            // Use ObjectMapper to convert Map<String, Object> to FollowRelationship
            var followRelationship = objectMapper.convertValue(propertiesMap, FollowRelationship.class);

            // Map the 'user' vertex in the relationship edge to a UserNode
            var userInVertex = edge.inVertex();
            var userNode = lazyMapVertexToUserNode(userInVertex);
            followRelationship.setUser(userNode);

            relationships.add(followRelationship);
        });

        return relationships;
    }
}
