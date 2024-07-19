package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.repository.impl.UserRepositoryImpl;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.TextP.regex;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

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
    void testFindAllSuccessfully() {
        given(traversalSource.V()).willReturn(gtVertex);
        given(gtVertex.hasLabel(anyString())).willReturn(gtVertex);
        given(gtVertex.has(anyString(), any(P.class))).willReturn(gtVertex);
        given(gtVertex.range(anyLong(), anyLong())).willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.toList()).willReturn(Collections.singletonList(new HashMap<>()));

        var result = repository.findAll("John", "CA", 10, 1);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        verify(traversalSource).V();
        verify(gtVertex).hasLabel("User");
        verify(gtVertex).has("name", regex("(?i).*John.*"));
        verify(gtVertex).has("address_state", regex("(?i)CA"));
        verify(gtVertex).range(0, 10);
        verify(gtVertex).elementMap();
        verify(gtVertexMap).toList();
    }

    @Test
    void testInsertSuccessfully() {
        given(traversalSource.addV(anyString())).willReturn(gtVertex);
        given(gtVertex.property(any(Object.class), any(Object.class))).willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.next()).willReturn(new HashMap<>());

        var result = repository.insert(new UserNode());

        assertThat(result).isNotNull();

        verify(traversalSource).addV("User");
        verify(gtVertex).property(eq(T.id), anyString());
        verify(gtVertex).elementMap();
        verify(gtVertexMap).next();
    }
}
