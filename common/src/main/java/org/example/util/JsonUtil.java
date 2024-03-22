package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
