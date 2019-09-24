package com.chriniko.jsonmerger.sample;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TypeConverter {

    @Getter
    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T convert(Map<String, Object> map, Class<T> clazz) {
        String encoded = encode(map);
        return decodeValue(encoded, clazz);
    }

    public <T> T convert(JsonNode jsonNode, Class<T> clazz) {
        String encoded = encode(jsonNode);
        return decodeValue(encoded, clazz);
    }

    public <T> T convert(String input, Class<T> clazz) {
        return decodeValue(input, clazz);
    }

    public <T> T convert(String input, TypeReference<T> typeRef) {
        return decodeValue(input, typeRef);
    }

    public String toJson(Object input) {
     return encode(input);
    }

    public JsonNode toJsonNode(Object input) {
        try {
            String encoded = encode(input);
            return mapper.readTree(encoded);
        } catch (IOException e) {
            throw new ProcessingException(e);
        }
    }

    public JsonNode toJsonNode(String input) {
        try {
            return mapper.readTree(input);
        } catch (IOException e) {
            throw new ProcessingException(e);
        }
    }

    public <T> Stream<T> convert(List<Map<String, Object>> m, Class<T> clazz) {
        return m.stream().map(r -> this.convert(r, clazz));
    }

    public String encode(Object obj) throws ProcessingException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    public <T> T decodeValue(String str, Class<T> clazz) {
        try {
            return mapper.readValue(str, clazz);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    public <T> T decodeValue(String str, TypeReference<T> type) {
        try {
            return mapper.readValue(str, type);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }
}
