package faas.study.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {


    private JsonUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * <p>
     * Object to JSON string
     * </p>
     */
    public static String object2Json(Object obj) {
        String result = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
        }
        return result;
    }

    /**
     * <p>
     * JSON to Object
     * </p>
     */
    public static <T> T json2Object(String json, Class<T> cls) {
        T result = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            result = objectMapper.readValue(json, cls);
        } catch (IOException e) {
        }

        return result;
    }

}

