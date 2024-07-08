package com.erebelo.springneptunedemo.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("graph")
public class GraphController {

    private final GraphTraversalSource g;

    @GetMapping(path = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getGraphData() {
        Map<String, Object> graphData = new HashMap<>();

        // Collect vertices
        Map<Object, Map<String, Object>> vertices = new HashMap<>();
        g.V().forEachRemaining(v -> {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("label", v.label());
            v.properties().forEachRemaining(p -> properties.put(p.key(), p.value()));

            vertices.put(v.id(), properties);
        });

        // Collect edges
        Map<Object, Map<String, Object>> edges = new HashMap<>();
        g.E().forEachRemaining(e -> {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("label", e.label());
            properties.put("source", e.outVertex().id());
            properties.put("target", e.inVertex().id());
            e.properties().forEachRemaining(p -> properties.put(p.key(), p.value()));

            edges.put(e.id(), properties);
        });

        graphData.put("vertices", vertices);
        graphData.put("edges", edges);

        return graphData;
    }
}
