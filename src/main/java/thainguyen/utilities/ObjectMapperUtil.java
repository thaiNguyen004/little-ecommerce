package thainguyen.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ObjectMapperUtil {

    private final ObjectMapper objectMapper;

    public <T> Map converObjectToMap(T entity) {
        return objectMapper.convertValue(entity, Map.class);
    }

    public Object convertMapToEntity(Map<String, Object> map, Class clazz) {
        return objectMapper.convertValue(map, clazz);
    }

}
