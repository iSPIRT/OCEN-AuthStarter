package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    public static <T> String toJson(T pojoInstance) {
        ObjectMapper mapper = new ObjectMapper();

        //Java Object -> JSON
        try {
            String json = mapper.writeValueAsString(new HeartbeatRequest<>(pojoInstance));
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> fromJson(String json) {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public static class HeartbeatRequest<T> {
        T event;

        public HeartbeatRequest(T event) {
            this.event = event;
        }

        public T getEvent() {
            return event;
        }

        public void setEvent(T event) {
            this.event = event;
        }
    }
}
