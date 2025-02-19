package com.erebelo.springneptunedemo.repository;

import static org.apache.tinkerpop.gremlin.process.traversal.Merge.onCreate;
import static org.apache.tinkerpop.gremlin.process.traversal.Merge.onMatch;
import static org.apache.tinkerpop.gremlin.process.traversal.TextP.regex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.exception.model.ConflictException;
import com.erebelo.springneptunedemo.repository.impl.UserRepositoryImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import org.apache.tinkerpop.gremlin.driver.exception.ResponseException;
import org.apache.tinkerpop.gremlin.process.traversal.Merge;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.FailStep;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.message.ResponseStatusCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @InjectMocks
    private UserRepositoryImpl repository;

    @Mock
    private GraphTraversalSource traversalSource;

    @Mock
    private GraphTraversal<Vertex, Vertex> gtVertex;

    @Mock
    private GraphTraversal<Vertex, Map<Object, Object>> gtVertexMap;

    @Test
    void testFindAllSuccessful() {
        given(traversalSource.V()).willReturn(gtVertex);
        given(gtVertex.hasLabel(anyString())).willReturn(gtVertex);
        given(gtVertex.has(anyString(), any(P.class))).willReturn(gtVertex);
        given(gtVertex.range(anyLong(), anyLong())).willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.toList()).willReturn(Collections.singletonList(new HashMap<>()));

        List<UserNode> response = repository.findAll("John", "CA", 10, 1);

        assertThat(response).isNotNull().hasSize(1);

        verify(traversalSource).V();
        verify(gtVertex).hasLabel("User");
        verify(gtVertex).has("name", regex("(?i)John"));
        verify(gtVertex).has("address_state", regex("^(?i)CA$"));
        verify(gtVertex).range(0, 10);
        verify(gtVertex).elementMap();
        verify(gtVertexMap).toList();
    }

    @Test
    void testInsertSuccessful() {
        given(traversalSource.mergeV(anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), ArgumentMatchers.<Traversal<Object, Object>>any()))
                .willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.next()).willReturn(new HashMap<>());

        UserNode response = repository.insert(new UserNode(null, "@john", "John", null));

        assertThat(response).isNotNull();

        verify(traversalSource).mergeV(Map.of(T.label, "User", "username", "@john"));
        verify(gtVertex).option(eq(onCreate), anyMap());
        verify(gtVertex).option(eq(onMatch), ArgumentMatchers.<Traversal<Object, Object>>any());
        verify(gtVertex).elementMap();
        verify(gtVertexMap).next();
    }

    @Test
    void testInsertThrowsConstraintCompletionException() {
        ResponseException responseException = new ResponseException(ResponseStatusCode.SERVER_ERROR_FAIL_STEP,
                "{\"message\": \"User already exists\"}");

        given(traversalSource.mergeV(anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), ArgumentMatchers.<Traversal<Object, Object>>any()))
                .willThrow(new CompletionException(responseException));
        UserNode userNode = new UserNode(null, "@john", "John", null);

        ConflictException exception = assertThrows(ConflictException.class, () -> repository.insert(userNode));

        assertEquals("User already exists", exception.getMessage());

        verify(traversalSource).mergeV(Map.of(T.label, "User", "username", "@john"));
        verify(gtVertex).option(eq(onCreate), anyMap());
        verify(gtVertex).option(eq(onMatch), ArgumentMatchers.<Traversal<Object, Object>>any());
    }

    @Test
    void testInsertThrowsJsonProcessingExceptionAtCompletionException() {
        ResponseException responseException = new ResponseException(ResponseStatusCode.SERVER_ERROR_FAIL_STEP,
                "User already exists");

        given(traversalSource.mergeV(anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), ArgumentMatchers.<Traversal<Object, Object>>any()))
                .willThrow(new CompletionException(responseException));
        UserNode userNode = new UserNode(null, "@john", "John", null);

        CompletionException exception = assertThrows(CompletionException.class, () -> repository.insert(userNode));

        assertTrue(exception.getMessage().contains("User already exists"));

        verify(traversalSource).mergeV(Map.of(T.label, "User", "username", "@john"));
        verify(gtVertex).option(eq(onCreate), anyMap());
        verify(gtVertex).option(eq(onMatch), ArgumentMatchers.<Traversal<Object, Object>>any());
    }

    @Test
    void testInsertThrowsFailException() {
        FailStep.FailException failException = new FailStep.FailException(mock(Traversal.Admin.class),
                mock(Traverser.Admin.class), "User already exists", Map.of("key", "value"));

        given(traversalSource.mergeV(anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), anyMap())).willReturn(gtVertex);
        given(gtVertex.option(any(Merge.class), ArgumentMatchers.<Traversal<Object, Object>>any()))
                .willThrow(failException);
        UserNode userNode = new UserNode(null, "@john", "John", null);

        ConflictException exception = assertThrows(ConflictException.class, () -> repository.insert(userNode));

        assertEquals("User already exists", exception.getMessage());

        verify(traversalSource).mergeV(Map.of(T.label, "User", "username", "@john"));
        verify(gtVertex).option(eq(onCreate), anyMap());
        verify(gtVertex).option(eq(onMatch), ArgumentMatchers.<Traversal<Object, Object>>any());
    }
}
