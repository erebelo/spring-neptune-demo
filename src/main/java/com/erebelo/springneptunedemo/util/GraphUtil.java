package com.erebelo.springneptunedemo.util;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GraphUtil {

    private static final String NESTED_PROPERTY_DELIMITER = "_";

    private static final String UPDATE_PROPERTIES_ERROR_MESSAGE = "Error updating vertex/edge properties: ";
    private static final String MAP_OBJECT_ERROR_MESSAGE = "Unexpected error while mapping vertex/edge properties to graph object: ";

    public static <T> void updateVertexAndEdgeProperties(GraphTraversal<?, ?> gtObject, T graphObject) {
        try {
            // Convert Graph Object to Map<String, Object>
            Map<String, Object> properties = ObjectMapperUtil.objectMapper.convertValue(graphObject, new TypeReference<>() {
            });

            // Iterate through the Map and update Vertex/Edge properties
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    // Flatten nested Object Map into Vertex/Edge properties
                    flattenNestedProperties(gtObject, key, (Map<String, Object>) value);
                } else {
                    gtObject.property(key, value);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(UPDATE_PROPERTIES_ERROR_MESSAGE + e.getMessage(), e);
        }
    }

    private static void flattenNestedProperties(GraphTraversal<?, ?> gtObject, String prefix, Map<String, Object> nestedProperties) {
        for (Map.Entry<String, Object> entry : nestedProperties.entrySet()) {
            String key = prefix + NESTED_PROPERTY_DELIMITER + entry.getKey();
            gtObject.property(key, entry.getValue());
        }
    }

    public static <T> T mapVertexAndEdgeToGraphObject(Map<Object, Object> propertiesMap, Class<T> clazz) {
        try {
            // Parse vertex properties dynamically and generically
            Map<String, Object> parsedProperties = parseVertexProperties(propertiesMap);

            // Convert parsed properties to the target class
            return ObjectMapperUtil.objectMapper.convertValue(parsedProperties, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(MAP_OBJECT_ERROR_MESSAGE + e.getMessage(), e);
        }
    }

    private static Map<String, Object> parseVertexProperties(Map<Object, Object> propertiesMap) {
        Map<String, Object> result = new HashMap<>();

        // Group properties based on their prefixes and flatten nested properties
        for (Map.Entry<Object, Object> entry : propertiesMap.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            // Split the key into parts based on underscores
            String[] parts = key.split(NESTED_PROPERTY_DELIMITER);
            if (parts.length > 1) {
                // Handle nested property
                var prefix = parts[0];
                var nestedKey = String.join(NESTED_PROPERTY_DELIMITER, Arrays.copyOfRange(parts, 1, parts.length));

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
