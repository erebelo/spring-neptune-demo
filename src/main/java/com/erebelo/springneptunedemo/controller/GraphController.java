package com.erebelo.springneptunedemo.controller;

import com.erebelo.springneptunedemo.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Profile("local")
@RequiredArgsConstructor
@RequestMapping("graph")
public class GraphController {

    private final GraphTraversalSource g;

    private static final String ID_KEY = "id";
    private static final String IN_EDGE_KEY = "IN";
    private static final String OUT_EDGE_KEY = "OUT";

    @GetMapping(path = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getGraphData() {
        log.info("Retrieving all vertices and edges");

        Map<String, Object> graphData = new HashMap<>();
        graphData.put("vertices", collectVertices());
        graphData.put("edges", collectEdges());

        return graphData;
    }

    private Map<Object, Map<String, Object>> collectVertices() {
        Map<Object, Map<String, Object>> vertices = new HashMap<>();
        List<Map<Object, Object>> vertexMapList = g.V().elementMap().toList();

        if (!vertexMapList.isEmpty()) {
            for (Map<Object, Object> vertexMap : vertexMapList) {
                Map<String, Object> properties = convertElementMap(vertexMap);

                vertices.put(properties.get(ID_KEY), properties);
            }
        }

        return vertices;
    }

    private Map<Object, Map<String, Object>> collectEdges() {
        Map<Object, Map<String, Object>> edges = new HashMap<>();
        List<Map<Object, Object>> edgeMapList = g.E().elementMap().toList();

        if (!edgeMapList.isEmpty()) {
            for (Map<Object, Object> edgeMap : edgeMapList) {
                Map<String, Object> properties = convertElementMap(edgeMap);
                properties.put("target", extractIdFromNestedMap(properties, IN_EDGE_KEY));
                properties.put("source", extractIdFromNestedMap(properties, OUT_EDGE_KEY));

                edges.put(properties.get(ID_KEY), properties);
            }
        }

        return edges;
    }

    private Map<String, Object> convertElementMap(Map<Object, Object> elementMap) {
        Map<String, Object> properties = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> entry : elementMap.entrySet()) {
            properties.put(entry.getKey().toString(), entry.getValue());
        }

        return properties;
    }

    private Object extractIdFromNestedMap(Map<String, Object> properties, String key) {
        if (properties.get(key) instanceof Map) {
            Map<String, Object> nestedMap = ObjectMapperUtil.objectMapper.convertValue(properties.get(key), Map.class);
            properties.remove(key);
            return nestedMap.get(ID_KEY);
        }

        return null;
    }
}
