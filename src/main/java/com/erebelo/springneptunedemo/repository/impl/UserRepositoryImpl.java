package com.erebelo.springneptunedemo.repository.impl;

import static com.erebelo.springneptunedemo.constant.UserConstant.ADDRESS_STATE_PROPERTY;
import static com.erebelo.springneptunedemo.constant.UserConstant.EDGE_CONSTRAINT_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.EXISTING_EDGE_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.FOLLOW_EDGE_LABEL;
import static com.erebelo.springneptunedemo.constant.UserConstant.GREMLIN_QUERY_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.JSON_PROCESSING_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.NAME_PROPERTY;
import static com.erebelo.springneptunedemo.constant.UserConstant.NO_EXISTING_EDGE_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.REGEX_CASE_INSENSITIVE;
import static com.erebelo.springneptunedemo.constant.UserConstant.REGEX_LIKE_CASE_INSENSITIVE;
import static com.erebelo.springneptunedemo.constant.UserConstant.USERNAME_PROPERTY;
import static com.erebelo.springneptunedemo.constant.UserConstant.USERS_NOT_FOUND_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.USER_ALREADY_EXISTS_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.USER_CONSTRAINT_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.USER_NOT_FOUND_ERROR_MESSAGE;
import static com.erebelo.springneptunedemo.constant.UserConstant.USER_VERTEX_LABEL;
import static com.erebelo.springneptunedemo.util.GraphUtil.mapVertexAndEdgeToGraphObject;
import static com.erebelo.springneptunedemo.util.GraphUtil.updateVertexAndEdgeProperties;
import static com.erebelo.springneptunedemo.util.ObjectMapperUtil.objectMapper;
import static com.erebelo.springneptunedemo.util.QueryUtil.calculatePaginationIndexes;
import static com.erebelo.springneptunedemo.util.QueryUtil.isValidProperty;
import static org.apache.tinkerpop.gremlin.process.traversal.Merge.onCreate;
import static org.apache.tinkerpop.gremlin.process.traversal.Merge.onMatch;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.regex;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.fail;

import com.erebelo.springneptunedemo.domain.graph.edge.FollowEdge;
import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.exception.model.ConflictException;
import com.erebelo.springneptunedemo.exception.model.NotFoundException;
import com.erebelo.springneptunedemo.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.driver.exception.ResponseException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.FailStep;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final GraphTraversalSource g;

    @Override
    public List<UserNode> findAll(String name, String addressState, Integer limit, Integer page) {
        GraphTraversal<Vertex, Vertex> gtVertex = g.V().hasLabel(USER_VERTEX_LABEL);

        // Apply a case-insensitive regex filter on 'name' property, which mimics a LIKE
        // query
        if (isValidProperty(name)) {
            gtVertex.has(NAME_PROPERTY, regex(REGEX_LIKE_CASE_INSENSITIVE + name));
        }

        // Apply a case-insensitive regex filter on 'address_state' property
        if (isValidProperty(addressState)) {
            gtVertex.has(ADDRESS_STATE_PROPERTY, regex(String.format(REGEX_CASE_INSENSITIVE, addressState)));
        }

        // Calculate the start and end indexes for pagination
        int[] indexes = calculatePaginationIndexes(limit, page);

        List<Map<Object, Object>> vertexMapList = gtVertex.range(indexes[0], indexes[1]).elementMap().toList();

        if (vertexMapList.isEmpty()) {
            throw new NotFoundException(USERS_NOT_FOUND_ERROR_MESSAGE);
        }

        return vertexMapList.stream().map(v -> mapVertexAndEdgeToGraphObject(v, UserNode.class)).toList();
    }

    @Override
    public UserNode findById(String id) {
        Map<Object, Object> vertexMap = retrieveVertexPropertiesById(id);
        return mapVertexAndEdgeToGraphObject(vertexMap, UserNode.class);
    }

    @Override
    public List<FollowEdge> findEdgesByUserIdAndDirection(String userId, Direction vertexDirection) {
        Vertex vertex = retrieveVertexById(userId);

        // From the vertex perspective, retrieve the edge whose vertex direction is
        // IN (from vertex/followers) or OUT (to vertex/following)
        List<Map<Object, Object>> edgeMapList = vertexDirection == Direction.IN
                ? g.V(vertex.id()).inE(FOLLOW_EDGE_LABEL).elementMap().toList()
                : g.V(vertex.id()).outE(FOLLOW_EDGE_LABEL).elementMap().toList();

        return edgeMapList.stream().map(rel -> {
            // Map edge properties
            FollowEdge followEdge = mapVertexAndEdgeToGraphObject(rel, FollowEdge.class);

            // From edge perspective, retrieve the vertex whose edge direction is
            // IN (to vertex) or OUT (from vertex)
            if (vertexDirection == Direction.IN) {
                followEdge.setOut(this.findById(((Map<?, ?>) rel.get(Direction.OUT)).get(T.id).toString()));
            } else {
                followEdge.setIn(this.findById(((Map<?, ?>) rel.get(Direction.IN)).get(T.id).toString()));
            }

            return followEdge;
        }).toList();
    }

    /*
     * Use `mergeV()` to enforce the constraint on properties with better execution
     * performance. For insertion without constraints, consider using:
     * `g.addV(USER_VERTEX_LABEL).property(T.id, UUID.randomUUID().toString())`.
     */
    @Override
    public UserNode insert(UserNode node) {
        try {
            GraphTraversal<Vertex, Vertex> gtVertex = g
                    .mergeV(Map.of(T.label, USER_VERTEX_LABEL, USERNAME_PROPERTY, node.getUsername()))
                    .option(onCreate, Map.of(T.label, USER_VERTEX_LABEL, T.id, UUID.randomUUID().toString()))
                    .option(onMatch, fail(USER_ALREADY_EXISTS_ERROR_MESSAGE + node.getUsername()));
            updateVertexAndEdgeProperties(gtVertex, node, HttpMethod.POST.name());

            GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
            return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
        } catch (CompletionException e) {
            // AWS Neptune processes Gremlin queries asynchronously,
            // often resulting in CompletionException when fail() is invoked
            if (e.getCause() instanceof ResponseException responseException) {
                try {
                    Map<String, Object> errorProperties = objectMapper.readValue(responseException.getMessage(),
                            new TypeReference<>() {
                            });
                    log.error(USER_CONSTRAINT_ERROR_MESSAGE);
                    throw new ConflictException((String) errorProperties.get("message"));
                } catch (JsonProcessingException jsonProcessingException) {
                    log.error(JSON_PROCESSING_ERROR_MESSAGE, jsonProcessingException);
                }
            }
            log.error(GREMLIN_QUERY_ERROR_MESSAGE);
            throw e;
        } catch (FailStep.FailException e) {
            // TinkerGraph processes Gremlin queries synchronously locally,
            // resulting in FailStep.FailException when fail() is invoked
            log.error(USER_CONSTRAINT_ERROR_MESSAGE);
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    public UserNode update(String id, UserNode node) {
        Vertex vertex = retrieveVertexById(id);

        // Check if the username is not in use
        checkUsernameConflict(vertex.id(), node.getUsername());

        GraphTraversal<Vertex, Vertex> gtVertex = g.V(vertex.id());
        updateVertexAndEdgeProperties(gtVertex, node, HttpMethod.PUT.name());

        gtVertex = g.V(vertex.id());
        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public UserNode patch(String id, Map<String, Object> requestMap) {
        Vertex vertex = retrieveVertexById(id);

        // Check if the username is not in use
        String username = (String) requestMap.get(USERNAME_PROPERTY);
        if (username != null) {
            checkUsernameConflict(vertex.id(), username);
        }

        GraphTraversal<Vertex, Vertex> gtVertex = g.V(vertex.id());
        updateVertexAndEdgeProperties(gtVertex, requestMap, HttpMethod.PATCH.name());

        gtVertex = g.V(vertex.id());
        GraphTraversal<Vertex, Map<Object, Object>> vertexTraversal = gtVertex.elementMap();
        return mapVertexAndEdgeToGraphObject(vertexTraversal.next(), UserNode.class);
    }

    @Override
    public void deleteById(String id) {
        Vertex vertex = retrieveVertexById(id);
        g.V(vertex.id()).drop().iterate();
    }

    /*
     * Use `mergeE()` to enforce the constraint on properties with better execution
     * performance. For insertion without constraints, consider using:
     * `g.addE(FOLLOW_EDGE_LABEL).from(__.V(fromVertexId)).to(__.V(toVertexId)).
     * property(T.id, UUID.randomUUID().toString());`.
     */
    @Override
    public FollowEdge createEdge(String fromId, String toId, FollowEdge edge) {
        // Retrieve vertex properties
        Map<Object, Object> fromVertexMap = retrieveVertexPropertiesById(fromId);
        Map<Object, Object> toVertexMap = retrieveVertexPropertiesById(toId);

        Object fromVertexId = fromVertexMap.get(T.id);
        Object toVertexId = toVertexMap.get(T.id);

        try {
            GraphTraversal<Edge, Edge> gtEdge = g
                    .mergeE(Map.of(T.label, FOLLOW_EDGE_LABEL, Direction.from, fromVertexId, Direction.to, toVertexId))
                    .option(onCreate,
                            Map.of(T.label, FOLLOW_EDGE_LABEL, Direction.from, fromVertexId, Direction.to, toVertexId,
                                    T.id, UUID.randomUUID().toString()))
                    .option(onMatch, fail(String.format(EXISTING_EDGE_ERROR_MESSAGE, fromId, toId)));
            updateVertexAndEdgeProperties(gtEdge, edge, HttpMethod.POST.name());

            // Map edge properties
            GraphTraversal<Edge, Map<Object, Object>> edgeTraversal = gtEdge.elementMap();
            FollowEdge followEdge = mapVertexAndEdgeToGraphObject(edgeTraversal.next(), FollowEdge.class);

            // Map IN and OUT edge vertices
            followEdge.setIn(mapVertexAndEdgeToGraphObject(toVertexMap, UserNode.class));
            followEdge.setOut(mapVertexAndEdgeToGraphObject(fromVertexMap, UserNode.class));

            return followEdge;
        } catch (CompletionException e) {
            // AWS Neptune processes Gremlin queries asynchronously,
            // often resulting in CompletionException when fail() is invoked
            if (e.getCause() instanceof ResponseException responseException) {
                try {
                    Map<String, Object> errorProperties = objectMapper.readValue(responseException.getMessage(),
                            new TypeReference<>() {
                            });
                    log.error(EDGE_CONSTRAINT_ERROR_MESSAGE);
                    throw new ConflictException((String) errorProperties.get("message"));
                } catch (JsonProcessingException jsonProcessingException) {
                    log.error(JSON_PROCESSING_ERROR_MESSAGE, jsonProcessingException);
                }
            }
            log.error(GREMLIN_QUERY_ERROR_MESSAGE);
            throw e;
        } catch (FailStep.FailException e) {
            // TinkerGraph processes Gremlin queries synchronously locally,
            // resulting in FailStep.FailException when fail() is invoked
            log.error(EDGE_CONSTRAINT_ERROR_MESSAGE);
            throw new ConflictException(e.getMessage());
        }
    }

    @Override
    public void removeEdge(String fromId, String toId) {
        Vertex fromVertex = retrieveVertexById(fromId);
        Vertex toVertex = retrieveVertexById(toId);

        // Check if the edge exists
        if (edgeExists(fromVertex.id(), toVertex.id())) {
            g.V(fromVertex.id()).outE(FOLLOW_EDGE_LABEL).where(__.inV().hasId(toVertex.id())).drop().iterate();
        } else {
            throw new ConflictException(String.format(NO_EXISTING_EDGE_ERROR_MESSAGE, fromId, toId));
        }
    }

    private Map<Object, Object> retrieveVertexPropertiesById(String id) {
        return g.V().hasLabel(USER_VERTEX_LABEL).has(T.id, id).elementMap().tryNext()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

    private Vertex retrieveVertexById(String id) {
        return g.V().hasLabel(USER_VERTEX_LABEL).has(T.id, id).tryNext()
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + id));
    }

    private void checkUsernameConflict(Object vertexId, String username) {
        g.V().hasLabel(USER_VERTEX_LABEL).has(USERNAME_PROPERTY, username).not(__.hasId(vertexId)).tryNext()
                .ifPresent(vertex -> {
                    throw new ConflictException(USER_ALREADY_EXISTS_ERROR_MESSAGE + username);
                });
    }

    private boolean edgeExists(Object fromVertexId, Object toVertexId) {
        return g.V(fromVertexId).outE(FOLLOW_EDGE_LABEL).inV().hasId(toVertexId).hasNext();
    }
}
