package com.erebelo.springneptunedemo.repository.impl;

import com.erebelo.springneptunedemo.domain.graph.edge.FollowEdge;
import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.exception.model.ConflictException;
import com.erebelo.springneptunedemo.exception.model.NotFoundException;
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
import java.util.UUID;

import static com.erebelo.springneptunedemo.util.GraphUtil.cleanVertexAndEdgeProperties;
import static com.erebelo.springneptunedemo.util.GraphUtil.mapVertexAndEdgeToGraphObject;
import static com.erebelo.springneptunedemo.util.GraphUtil.updateVertexAndEdgeProperties;
import static com.erebelo.springneptunedemo.util.QueryUtil.calculatePaginationIndexes;
import static com.erebelo.springneptunedemo.util.QueryUtil.propertyLikeRegex;
import static com.erebelo.springneptunedemo.util.QueryUtil.propertyRegex;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.regex;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final GraphTraversalSource g;

    private static final String USER_VERTEX_LABEL = "User";
    private static final String FOLLOW_EDGE_LABEL = "FOLLOW";
    private static final String NAME_PROPERTY = "name";
    private static final String ADDRESS_STATE_PROPERTY = "address_state";
    private static final String REGEX_CASE_INSENSITIVE = "(?i)";

    private static final String USERS_NOT_FOUND_ERROR_MESSAGE = "Users not found";
    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "User not found by id: ";
    private static final String EXISTING_EDGE_ERROR_MESSAGE = "Existing edge found from user id: %s to user id: %s";
    private static final String NO_EXISTING_EDGE_ERROR_MESSAGE = "No existing edge found from user id: %s to user id: %s";

    @Override
    public List<UserNode> findAll(String name, String addressState, Integer limit, Integer page) {
        // Calculate the start and end indexes for pagination
        int[] indexes = calculatePaginationIndexes(limit, page);

        List<Map<Object, Object>> vertexMapList = g.V()
                .hasLabel(USER_VERTEX_LABEL)
                .has(NAME_PROPERTY, regex(REGEX_CASE_INSENSITIVE + propertyLikeRegex(name)))
                .has(ADDRESS_STATE_PROPERTY, regex(REGEX_CASE_INSENSITIVE + propertyRegex(addressState)))
                .range(indexes[0], indexes[1])
                .elementMap()
                .toList();

        if (vertexMapList.isEmpty()) {
            throw new NotFoundException(USERS_NOT_FOUND_ERROR_MESSAGE);
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
        GraphTraversal<Vertex, Vertex> gtVertex = g.addV(USER_VERTEX_LABEL).property(T.id, UUID.randomUUID().toString());
        updateVertexAndEdgeProperties(gtVertex, node);

        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public UserNode update(String id, UserNode node) {
        Vertex vertex = retrieveVertexById(id);

        GraphTraversal<Vertex, Vertex> gtVertex = g.V(vertex.id());
        cleanVertexAndEdgeProperties(gtVertex);

        gtVertex = g.V(vertex.id());
        updateVertexAndEdgeProperties(gtVertex, node);

        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public void deleteById(String id) {
        Vertex vertex = retrieveVertexById(id);
        g.V(vertex.id()).drop().iterate();
    }

    @Override
    public List<FollowEdge> findEdgesByUserIdAndDirection(String userId, Direction vertexDirection) {
        Vertex vertex = retrieveVertexById(userId);

        // Retrieve all edges which the vertex direction is IN (followers) or OUT (following)
        List<Map<Object, Object>> edgeMapList = vertexDirection == Direction.IN ?
                g.V(vertex.id()).inE(FOLLOW_EDGE_LABEL).elementMap().toList() :
                g.V(vertex.id()).outE(FOLLOW_EDGE_LABEL).elementMap().toList();

        return edgeMapList.stream()
                .map(rel -> {
                    // Map edge properties
                    FollowEdge followEdge = mapVertexAndEdgeToGraphObject(rel, FollowEdge.class);

                    // Retrieve the vertex that the edge direction is IN (to vertex) or OUT (from vertex)
                    if (vertexDirection == Direction.IN) {
                        followEdge.setOut(this.findById(((Map<?, ?>) rel.get(Direction.OUT)).get(T.id).toString()));
                    } else {
                        followEdge.setIn(this.findById(((Map<?, ?>) rel.get(Direction.IN)).get(T.id).toString()));
                    }

                    return followEdge;
                })
                .toList();
    }

    @Override
    public FollowEdge createEdge(String fromId, String toId, FollowEdge edge) {
        // Retrieve vertex properties
        Map<Object, Object> fromVertexMap = retrieveVertexPropertiesById(fromId);
        Map<Object, Object> toVertexMap = retrieveVertexPropertiesById(toId);

        Object fromVertexId = fromVertexMap.get(T.id);
        Object toVertexId = toVertexMap.get(T.id);

        // Check if the edge does not exist
        if (!edgeExists(fromVertexId, toVertexId)) {
            GraphTraversal<Edge, Edge> gtEdge = g.addE(FOLLOW_EDGE_LABEL)
                    .from(__.V(fromVertexId))
                    .to(__.V(toVertexId))
                    .property(T.id, UUID.randomUUID().toString());
            updateVertexAndEdgeProperties(gtEdge, edge);

            // Map edge properties
            GraphTraversal<Edge, Map<Object, Object>> edgeTraversal = gtEdge.elementMap();
            FollowEdge followEdge = mapVertexAndEdgeToGraphObject(edgeTraversal.next(), FollowEdge.class);

            // Map IN and OUT edge vertices
            followEdge.setIn(mapVertexAndEdgeToGraphObject(toVertexMap, UserNode.class));
            followEdge.setOut(mapVertexAndEdgeToGraphObject(fromVertexMap, UserNode.class));

            return followEdge;
        }

        throw new ConflictException(String.format(EXISTING_EDGE_ERROR_MESSAGE, fromId, toId));
    }

    @Override
    public void removeEdge(String fromId, String toId) {
        Vertex fromVertex = retrieveVertexById(fromId);
        Vertex toVertex = retrieveVertexById(toId);

        // Check if the edge exists
        if (edgeExists(fromVertex.id(), toVertex.id())) {
            g.V(fromVertex.id())
                    .outE(FOLLOW_EDGE_LABEL)
                    .where(__.inV().hasId(toVertex.id()))
                    .drop()
                    .iterate();
        } else {
            throw new ConflictException(String.format(NO_EXISTING_EDGE_ERROR_MESSAGE, fromId, toId));
        }
    }

    private Map<Object, Object> retrieveVertexPropertiesById(String id) {
        return g.V()
                .hasLabel(USER_VERTEX_LABEL)
                .has(T.id, id)
                .elementMap()
                .tryNext()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

//    private GraphTraversal<Vertex, Vertex> retrieveGraphTraversalById(String id) {
//        GraphTraversal<Vertex, Vertex> gtVertex = g.V().hasLabel(USER_VERTEX_LABEL).has(T.id, id);
//
//        if (gtVertex.hasNext()) {
//            return gtVertex;
//        }
//
//        throw new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id);
//    }

    private Vertex retrieveVertexById(String id) {
        return g.V()
                .hasLabel(USER_VERTEX_LABEL)
                .has(T.id, id)
                .tryNext()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

    private boolean edgeExists(Object fromVertexId, Object toVertexId) {
        return g.V(fromVertexId).outE(FOLLOW_EDGE_LABEL).inV().hasId(toVertexId).hasNext();
    }
}
