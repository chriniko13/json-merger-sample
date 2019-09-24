package com.chriniko.jsonmerger.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.inject.Inject;
import java.util.*;

public class StateMerger {

    private static final int RECURSION_MAX_DEPTH = 10;

    private final TypeConverter typeConverter;

    @Inject
    public StateMerger(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
        System.out.println("typeConverter: " + typeConverter);
    }

    public Set<String> extractAllFields(JsonNode input) {
        Set<String> accumulator = new LinkedHashSet<>();
        _extractAllFields(input, accumulator, 0);
        return accumulator;
    }

    // Note: put
    public <E> E merge(E existingBomEntity, E patchedEntity, Set<String> sensitiveFieldNames) {
        JsonNode patchedEntityAsJson = typeConverter.toJsonNode(patchedEntity);
        return merge(existingBomEntity, patchedEntityAsJson, sensitiveFieldNames);
    }

    // Note: patch
    @SuppressWarnings("unchecked")
    public <E> E merge(E existingBomEntity, JsonNode patchedEntity, Set<String> sensitiveFieldNames) {

        JsonNode existingBomEntityAsJson = typeConverter.toJsonNode(existingBomEntity);

        List<String> accumulator = new LinkedList<>();
        _merge(existingBomEntityAsJson, patchedEntity, sensitiveFieldNames, accumulator, 0);

        String existingBomEntityAsString = typeConverter.encode(existingBomEntityAsJson);
        Class<E> eClazz = (Class<E>) existingBomEntity.getClass();
        return typeConverter.decodeValue(existingBomEntityAsString, eClazz);
    }

    private void _extractAllFields(JsonNode input, Set<String> accumulator, int currentDepth) {
        if (currentDepth >= RECURSION_MAX_DEPTH - 1) {
            throw new IllegalStateException("recursion max depth reached");
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = input.fields();
        while (iterator.hasNext()) {

            Map.Entry<String, JsonNode> entry = iterator.next();

            String key = entry.getKey();
            accumulator.add(key);

            JsonNode value = entry.getValue();
            if (value.isObject()) {
                _extractAllFields(value, accumulator, ++currentDepth);
            }
        }
    }

    private void _merge(JsonNode asIs, JsonNode toBe, Set<String> sensitiveFieldNames, List<String> childNames, int currentDepth) {
        Objects.requireNonNull(asIs);
        Objects.requireNonNull(toBe);

        if (currentDepth >= RECURSION_MAX_DEPTH - 1) {
            throw new IllegalStateException("recursion max depth reached");
        }

        for (Iterator<String> it = toBe.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            JsonNode value = toBe.get(fieldName);
            if (sensitiveFieldNames.contains(fieldName)) {
                continue;
            }

            if (value.isObject()) {
                childNames.add(fieldName);
                _merge(asIs, value, sensitiveFieldNames, childNames, ++currentDepth);
                childNames.clear();
            } else {
                if (childNames.isEmpty()) { // Note: not nested object case.
                    ((ObjectNode) asIs).set(fieldName, value);
                } else {
                    // Note: search for child.
                    JsonNode childNode = asIs;
                    for (String path : childNames) {
                        childNode = childNode.get(path);
                    }
                    if (childNode == null) { // Note: if not exists, create it.
                        String lastChildName = childNames.get(childNames.size() - 1);
                        ObjectNode newChildNode = typeConverter.getMapper().createObjectNode();

                        JsonNode walker = asIs;
                        for (int i = 0; i < childNames.size() - 1; i++) {
                            String path = childNames.get(i);
                            walker = walker.get(path);
                        }
                        ((ObjectNode) walker).set(lastChildName, newChildNode);
                        childNode = newChildNode;
                    }
                    ((ObjectNode) childNode).set(fieldName, value);
                }
            }
        }
    }

}
