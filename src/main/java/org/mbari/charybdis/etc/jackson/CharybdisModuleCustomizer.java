package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.time.Timecode;

@Singleton
@Priority(1)
public class CharybdisModuleCustomizer implements ObjectMapperCustomizer {

    @ConfigProperty(name = "charybdis.jackson.property-naming-strategy", defaultValue = "LOWER_CAMEL_CASE")
    String propertyNamingStrategy;

    public void customize(ObjectMapper mapper) {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(Timecode.class, new TimecodeSerializer());
        simpleModule.addSerializer(byte[].class, new ByteArraySerializer());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Quarkus will set strategy for most of the JSON returns. However, the mixins are added
        // after the face and explicity set the names. Here we just chane the mixin based on the
        // strategy.
        if (propertyNamingStrategy.equals("SNAKE_CASE")) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            mapper.addMixIn(Annotation.class, AnnotationMixinSC.class);
            mapper.addMixIn(Media.class, MediaMixinSC.class);
        }
        else {
            mapper.addMixIn(Annotation.class, AnnotationMixinCC.class);
            mapper.addMixIn(Media.class, MediaMixinCC.class);
        }

        mapper.registerModule(simpleModule);
    }
}
