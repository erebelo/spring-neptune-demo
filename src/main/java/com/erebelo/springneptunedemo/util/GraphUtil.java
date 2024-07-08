package com.erebelo.springneptunedemo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphUtil {

    public static void cleanVertexAndEdgeProperties(GraphTraversal<?, ?> gtObject) {
        gtObject.properties().forEachRemaining(property -> {
            if (!property.key().equalsIgnoreCase("env")) {
                property.remove();
            }
        });
    }

    public static <T> void updateVertexAndEdgeProperties(GraphTraversal<?, ?> gtObject, T node) {
        try {
            // Convert Node to Map<String, Object>
            Map<String, Object> properties = ObjectMapperUtil.objectMapper.convertValue(node, Map.class);

            // Iterate through the Map and update Vertex/Edge properties
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getValue() instanceof Map) {
                        // Flatten nested Object Map into Vertex/Edge properties
                        flattenNestedProperties(gtObject, entry.getKey(), (Map<String, Object>) entry.getValue());
                    } else {
                        gtObject.property(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error updating vertex/edge properties", e);
            throw new IllegalArgumentException("Error updating vertex/edge properties: " + e.getMessage(), e);
        }
    }

    private static void flattenNestedProperties(GraphTraversal<?, ?> gtObject, String prefix,
            Map<String, Object> nestedProperties) {
        for (Map.Entry<String, Object> entry : nestedProperties.entrySet()) {
            String key = prefix + "_" + entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                gtObject.property(key, value.toString());
            }
        }
    }

    public static <T> T mapVertexAndEdgeToNode(Map<Object, Object> propertiesMap, Class<T> clazz) {
        try {
            // Parse properties dynamically and generically
            Map<String, Object> parsedProperties = parseProperties(propertiesMap);

            // Use ObjectMapper to convert parsed properties to the target class
            return ObjectMapperUtil.objectMapper.convertValue(parsedProperties, clazz);
        } catch (Exception e) {
            log.error("Unexpected error while mapping vertex/edge properties to node object", e);
            throw new IllegalArgumentException("Unexpected error while mapping vertex/edge properties to node object: " + e.getMessage(),
                    e);
        }
    }

    private static Map<String, Object> parseProperties(Map<Object, Object> propertiesMap) {
        Map<String, Object> result = new HashMap<>();

        // Group properties based on their prefixes and flatten nested properties
        for (Map.Entry<Object, Object> entry : propertiesMap.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            // Split the key into parts based on underscores
            String[] parts = key.split("_");
            if (parts.length > 1) {
                // Handle nested property
                var prefix = parts[0];
                var nestedKey = String.join("_", Arrays.copyOfRange(parts, 1, parts.length));

                // Create nested map if it doesn't exist
                Map<String, Object> nestedMap = (Map<String, Object>) result.computeIfAbsent(prefix, k -> new HashMap<>());
                nestedMap.put(nestedKey, value);
            } else {
                result.put(key, value);
            }
        }

        return result;
    }
}
