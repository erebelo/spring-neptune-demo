package com.erebelo.springneptunedemo.repository.impl;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.domain.graph.relationship.FollowRelationship;
import com.erebelo.springneptunedemo.domain.response.FollowResponse;
import com.erebelo.springneptunedemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.erebelo.springneptunedemo.util.GraphUtil.cleanVertexAndEdgeProperties;
import static com.erebelo.springneptunedemo.util.GraphUtil.mapVertexAndEdgeToGraphObject;
import static com.erebelo.springneptunedemo.util.GraphUtil.updateVertexAndEdgeProperties;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final GraphTraversalSource traversalSource;

    private static final String USER_VERTEX_LABEL = "User";
    private static final String FOLLOW_EDGE_LABEL = "FOLLOW";

    @Override
    public List<UserNode> findAll() {
        List<Map<Object, Object>> vertexMapList = traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toList();

        if (vertexMapList.isEmpty()) {
            throw new IllegalArgumentException("No users found");
        }

        return vertexMapList.stream()
                .map(v -> mapVertexAndEdgeToGraphObject(v, UserNode.class))
                .toList();
    }

    @Override
    public UserNode findById(String id) {
        Map<Object, Object> vertexMap = retrieveVertexPropertiesById(id);
        return mapVertexAndEdgeToGraphObject(vertexMap, UserNode.class);
    }

    @Override
    public UserNode insert(UserNode node) {
        GraphTraversal<Vertex, Vertex> gtVertex = traversalSource.addV(USER_VERTEX_LABEL);
        updateVertexAndEdgeProperties(gtVertex, node);

        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public UserNode update(String id, UserNode node) {
        GraphTraversal<Vertex, Vertex> gtVertex = retrieveGraphTraversalById(id);
        cleanVertexAndEdgeProperties(gtVertex);

        gtVertex = retrieveGraphTraversalById(id);
        updateVertexAndEdgeProperties(gtVertex, node);

        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public void deleteById(String id) {
        traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toStream()
                .filter(v -> id.equalsIgnoreCase(v.get(T.id).toString()))
                .findFirst()
                .ifPresentOrElse(vertex -> {
                            // Delete the vertex and its relationships
                            traversalSource.V(vertex.get(T.id)).drop().iterate();
                        }, () -> {
                            throw new IllegalArgumentException("User not found by id: " + id);
                        }
                );
    }

    @Override
    public List<FollowResponse> findRelationshipsByUserIdAndDirection(String userId, Direction direction) {
        GraphTraversal<Vertex, Vertex> gtVertex = retrieveGraphTraversalById(userId);

        List<Map<Object, Object>> edgeMapList = direction == Direction.IN ?
                gtVertex.inE(FOLLOW_EDGE_LABEL).elementMap().toList() :
                gtVertex.outE(FOLLOW_EDGE_LABEL).elementMap().toList();

        return edgeMapList.stream()
                .map(rel -> mapVertexAndEdgeToGraphObject(rel, FollowResponse.class))
                .toList();
    }

    @Override
    public FollowRelationship createRelationship(String fromId, String toId, FollowRelationship relationship) {
        Map<Object, Object> fromVertexMap = retrieveVertexPropertiesById(fromId);
        Map<Object, Object> toVertexMap = retrieveVertexPropertiesById(toId);

        Vertex fromVertex = traversalSource.V(fromVertexMap.get(T.id)).next();
        Vertex toVertex = traversalSource.V(toVertexMap.get(T.id)).next();

        if (!relationshipExists(fromVertex, toVertex)) {
            GraphTraversal<Edge, Edge> gtEdge = traversalSource.addE(FOLLOW_EDGE_LABEL).from(fromVertex).to(toVertex);
            updateVertexAndEdgeProperties(gtEdge, relationship);

            GraphTraversal<Edge, Map<Object, Object>> edgeTraversal = gtEdge.elementMap();
            FollowRelationship followRelationship = mapVertexAndEdgeToGraphObject(edgeTraversal.next(), FollowRelationship.class);

            // Map IN and OUT edge vertices for the FollowResponse objects
            if (followRelationship.getIn() != null) {
                followRelationship.setIn(findById(followRelationship.getIn().getId()));
            }
            if (followRelationship.getOut() != null) {
                followRelationship.setOut(findById(followRelationship.getOut().getId()));
            }

            return followRelationship;
        }

        throw new IllegalArgumentException(String.format("Existing relationship found from user id: %s to user id %s", fromId, toId));
    }

    @Override
    public void removeRelationship(String fromId, String toId) {
        Map<Object, Object> fromVertexMap = retrieveVertexPropertiesById(fromId);
        Map<Object, Object> toVertexMap = retrieveVertexPropertiesById(toId);

        Vertex fromVertex = traversalSource.V(fromVertexMap.get(T.id)).next();
        Vertex toVertex = traversalSource.V(toVertexMap.get(T.id)).next();

        if (relationshipExists(fromVertex, toVertex)) {
            traversalSource.V(fromVertex)
                    .outE(FOLLOW_EDGE_LABEL)
                    .where(__.inV().hasId(toVertex.id()))
                    .drop()
                    .iterate();
        } else {
            throw new IllegalArgumentException(String.format("No existing relationship found from user id: %s to user id: %s", fromId,
                    toId));
        }
    }

    private Map<Object, Object> retrieveVertexPropertiesById(String id) {
        return traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toStream()
                .filter(v -> id.equalsIgnoreCase(v.get(T.id).toString()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found by id: " + id));
    }

    private GraphTraversal<Vertex, Vertex> retrieveGraphTraversalById(String id) {
        return traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toStream()
                .filter(v -> id.equalsIgnoreCase(v.get(T.id).toString()))
                .map(v -> traversalSource.V(v.get(T.id)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found by id: " + id));
    }

    private boolean relationshipExists(Vertex fromVertex, Vertex toVertex) {
        return traversalSource.V(fromVertex).outE(FOLLOW_EDGE_LABEL).inV().hasId(toVertex.id()).hasNext();
    }
}
