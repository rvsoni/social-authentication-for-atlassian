package it.jira;

import com.google.common.base.Preconditions;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ItEnvironment {

    public static final String LICENSE_3HR = "AAABCA0ODAoPeNpdj01PwkAURffzKyZxZ1IyUzARkllQ24gRaQMtGnaP8VEmtjPNfFT59yJVFyzfu\n" +
            "bkn796Ux0Bz6SmbUM5nbDzj97RISxozHpMUnbSq88poUaLztFEStUN6MJZ2TaiVpu/YY2M6tI6sQ\n" +
            "rtHmx8qd74EZ+TBIvyUU/AoYs7jiE0jzknWQxMuifA2IBlUbnQ7AulVjwN9AaU9atASs69O2dNFU\n" +
            "4wXJLc1aOUGw9w34JwCTTZoe7RPqUgep2X0Vm0n0fNut4gSxl/Jcnj9nFb6Q5tP/Ueu3L+0PHW4g\n" +
            "hZFmm2zZV5k6/95CbR7Y9bYGo/zGrV3Ir4jRbDyCA6vt34DO8p3SDAsAhQnJjLD5k9Fr3uaIzkXK\n" +
            "f83o5vDdQIUe4XequNCC3D+9ht9ZYhNZFKmnhc=X02dh";

    public static final String PLUGIN_KEY = "com.pawelniewiadomski.jira.jira-openid-authentication-plugin";

    public static Map<String, Object> getConfiguration() {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, Object> configuration;
        final File src = new File("it.json");

        Preconditions.checkArgument(src.exists(), "it.json doesn't exist in the working directory!");
        try {
            configuration = mapper.readValue(src, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configuration;
    }

}
