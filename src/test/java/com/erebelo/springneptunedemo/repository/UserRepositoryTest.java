package com.erebelo.springneptunedemo.repository;

import com.erebelo.springneptunedemo.domain.graph.node.UserNode;
import com.erebelo.springneptunedemo.repository.impl.UserRepositoryImpl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        Map<Object, Object> map = new HashMap<>();
        map.put("id", "123");

        given(traversalSource.V()).willReturn(gtVertex);
        given(gtVertex.hasLabel(anyString())).willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.toList()).willReturn(Collections.singletonList(map));

        var result = repository.findAll();

        assertThat(result).isNotNull();

        verify(traversalSource).V();
        verify(gtVertex).hasLabel("User");
        verify(gtVertex).elementMap();
        verify(gtVertexMap).toList();
    }

    @Test
    void testInsertSuccessfully() {
        Map<Object, Object> map = new HashMap<>();
        map.put("id", "123");

        given(traversalSource.addV(anyString())).willReturn(gtVertex);
        given(gtVertex.elementMap()).willReturn(gtVertexMap);
        given(gtVertexMap.next()).willReturn(map);

        var result = repository.insert(new UserNode());

        assertThat(result).isNotNull();

        verify(traversalSource).addV("User");
        verify(gtVertex).elementMap();
        verify(gtVertexMap).next();
    }
}
