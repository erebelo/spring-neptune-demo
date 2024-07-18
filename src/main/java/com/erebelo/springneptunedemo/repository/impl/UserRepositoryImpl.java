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

    private final GraphTraversalSource traversalSource;

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

        List<Map<Object, Object>> vertexMapList = traversalSource.V()
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
                            // Delete the vertex and its edges
                            traversalSource.V(vertex.get(T.id)).drop().iterate();
                        }, () -> {
                            throw new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id);
                        }
                );
    }

    @Override
    public List<FollowEdge> findEdgesByUserIdAndDirection(String userId, Direction direction) {
        GraphTraversal<Vertex, Vertex> gtVertex = retrieveGraphTraversalById(userId);

        // Retrieve all edges which the vertex direction is IN or OUT
        List<Map<Object, Object>> edgeMapList = direction == Direction.IN ?
                gtVertex.inE(FOLLOW_EDGE_LABEL).elementMap().toList() :
                gtVertex.outE(FOLLOW_EDGE_LABEL).elementMap().toList();

        return edgeMapList.stream()
                .map(rel -> {
                    // Map edge properties
                    FollowEdge followEdge = mapVertexAndEdgeToGraphObject(rel, FollowEdge.class);

                    // Retrieve IN and OUT edge vertices
                    followEdge.setIn(this.findById(((Map<?, ?>) rel.get(Direction.IN)).get(T.id).toString()));
                    followEdge.setOut(this.findById(((Map<?, ?>) rel.get(Direction.OUT)).get(T.id).toString()));

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
            GraphTraversal<Edge, Edge> gtEdge = traversalSource.addE(FOLLOW_EDGE_LABEL).from(__.V(fromVertexId)).to(__.V(toVertexId));
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
        // Retrieve vertex properties
        Map<Object, Object> fromVertexMap = retrieveVertexPropertiesById(fromId);
        Map<Object, Object> toVertexMap = retrieveVertexPropertiesById(toId);

        Object fromVertexId = fromVertexMap.get(T.id);
        Object toVertexId = toVertexMap.get(T.id);

        // Check if the edge exists
        if (edgeExists(fromVertexId, toVertexId)) {
            traversalSource.V(fromVertexId)
                    .outE(FOLLOW_EDGE_LABEL)
                    .where(__.inV().hasId(toVertexId))
                    .drop()
                    .iterate();
        } else {
            throw new ConflictException(String.format(NO_EXISTING_EDGE_ERROR_MESSAGE, fromId, toId));
        }
    }

    private Map<Object, Object> retrieveVertexPropertiesById(String id) {
        return traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toStream()
                .filter(v -> id.equalsIgnoreCase(v.get(T.id).toString()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

    // TODO
    private GraphTraversal<Vertex, Vertex> retrieveGraphTraversalById(String id) {
        return traversalSource.V()
                .hasLabel(USER_VERTEX_LABEL)
                .elementMap()
                .toStream()
                .filter(v -> id.equalsIgnoreCase(v.get(T.id).toString()))
                .map(v -> traversalSource.V(v.get(T.id)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

    private boolean edgeExists(Object fromVertexId, Object toVertexId) {
        return traversalSource.V(fromVertexId).outE(FOLLOW_EDGE_LABEL).inV().hasId(toVertexId).hasNext();
    }
}
