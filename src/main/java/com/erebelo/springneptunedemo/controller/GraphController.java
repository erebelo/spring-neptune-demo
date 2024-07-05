package com.erebelo.springneptunedemo.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
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
        Map<Object, Map<String, Object>> nodes = new HashMap<>();
        g.V().forEachRemaining(v -> {
            Map<String, Object> properties = new HashMap<>();
            v.properties().forEachRemaining(p -> properties.put(p.key(), p.value()));
            nodes.put(v.id(), properties);
        });

        // Collect edges
        Map<Object, Map<String, Object>> edges = new HashMap<>();
        g.E().forEachRemaining(e -> {
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("id", e.id());
            edgeData.put("source", e.outVertex().id());
            edgeData.put("target", e.inVertex().id());
            edgeData.put("label", e.label());
            edges.put(e.id(), edgeData);
        });

        graphData.put("nodes", nodes);
        graphData.put("edges", edges);

        return graphData;
    }
}
