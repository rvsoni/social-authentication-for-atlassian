package com.pawelniewiadomski.jira.openid.authentication;

import com.atlassian.json.marshal.Jsonable;
import com.atlassian.util.concurrent.LazyReference;
import lombok.experimental.UtilityClass;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
public class JsonableUtil {

    final LazyReference<ObjectMapper> om = new LazyReference<ObjectMapper>() {
        @Override
        protected ObjectMapper create() throws Exception {
            return new ObjectMapper();
        }
    };

    @Nonnull
    public static Jsonable toJsonable(@Nullable final Object object) {
        return writer -> writer.write(om.get().writeValueAsString(object));
    }
}
