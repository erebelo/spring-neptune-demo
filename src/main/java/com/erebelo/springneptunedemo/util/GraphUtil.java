package com.erebelo.springneptunedemo.util;

import static com.erebelo.springneptunedemo.util.ObjectMapperUtil.objectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.springframework.http.HttpMethod;

@UtilityClass
public class GraphUtil {

    private static final String NESTED_PROPERTY_DELIMITER = "_";

    private static final String UPDATE_PROPERTIES_ERROR_MESSAGE = "Error updating vertex/edge properties: ";
    private static final String MAP_OBJECT_ERROR_MESSAGE = "Unexpected error while mapping vertex/edge properties to "
            + "graph object: ";

    @SuppressWarnings("unchecked")
    public static <T> void updateVertexAndEdgeProperties(GraphTraversal<?, ?> gtObject, T graphObject, String op) {
        try {
            // List to collect properties whose value is null to be dropped
            List<String> propertiesToDrop = new ArrayList<>();

            // Convert Graph Object to Map<String, Object>
            Map<String, Object> properties = objectMapper.convertValue(graphObject, new TypeReference<>() {
            });

            // Iterate through the Map and update Vertex/Edge properties
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value != null) {
                    if (value instanceof Map) {
                        // Flatten nested Object Map into Vertex/Edge properties
                        flattenNestedProperties(gtObject, key, (Map<String, Object>) value, propertiesToDrop);
                    } else {
                        gtObject.property(key, value);
                    }
                } else {
                    // Collect property if value is null
                    propertiesToDrop.add(key);
                }
            }

            // Drop all collected properties at once
            if (op.equals(HttpMethod.PUT.name()) || op.equals(HttpMethod.PATCH.name())) {
                if (!propertiesToDrop.isEmpty()) {
                    gtObject.properties(propertiesToDrop.toArray(new String[0])).drop();
                }
                gtObject.iterate();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(UPDATE_PROPERTIES_ERROR_MESSAGE + e.getMessage(), e);
        }
    }

    private static void flattenNestedProperties(GraphTraversal<?, ?> gtObject, String prefix,
            Map<String, Object> nestedProperties, List<String> propertiesToDrop) {
        for (Map.Entry<String, Object> entry : nestedProperties.entrySet()) {
            String key = prefix + NESTED_PROPERTY_DELIMITER + entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                gtObject.property(key, value);
            } else {
                // Collect property if value is null
                propertiesToDrop.add(key);
            }
        }
    }

    public static <T> T mapVertexAndEdgeToGraphObject(Map<Object, Object> propertiesMap, Class<T> clazz) {
        try {
            // Parse vertex properties dynamically and generically
            Map<String, Object> parsedProperties = parseVertexProperties(propertiesMap);

            // Convert parsed properties to the target class
            return objectMapper.convertValue(parsedProperties, clazz);
        } catch (Exception e) {
            throw new IllegalArgumentException(MAP_OBJECT_ERROR_MESSAGE + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
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
                String prefix = parts[0];
                String nestedKey = String.join(NESTED_PROPERTY_DELIMITER, Arrays.copyOfRange(parts, 1, parts.length));

                // Create nested map if it doesn't exist
                Map<String, Object> nestedMap = (Map<String, Object>) result.computeIfAbsent(prefix,
                        k -> new HashMap<>());
                nestedMap.put(nestedKey, value);
            } else {
                result.put(key, value);
            }
        }

        return result;
    }
}
