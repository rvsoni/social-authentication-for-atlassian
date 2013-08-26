package it;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ItEnvironment {

    public static Map<String, Object> getConfiguration() {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> configuration;
        try {
            configuration = mapper.readValue(new File(ItEnvironment.class.getClassLoader().getResource("it.json").getFile()),
                    new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configuration;
    }

}
