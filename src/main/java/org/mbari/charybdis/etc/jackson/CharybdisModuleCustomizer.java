package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.jackson.JsonMapperBuilderCustomizer;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.time.Timecode;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Singleton
public class CharybdisModuleCustomizer implements JsonMapperBuilderCustomizer {

    @ConfigProperty(name = "charybdis.jackson.property-naming-strategy", defaultValue = "LOWER_CAMEL_CASE")
    String propertyNamingStrategy;

    @Override
    public void customize(JsonMapper.Builder builder) {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(Timecode.class, new TimecodeSerializer());
        simpleModule.addSerializer(byte[].class, new ByteArraySerializer());

        builder.changeDefaultPropertyInclusion(incl -> incl
                .withValueInclusion(JsonInclude.Include.NON_NULL)
                .withContentInclusion(JsonInclude.Include.NON_NULL));

        // Quarkus will set strategy for most of the JSON returns. However, the mixins are added
        // after the fact and explicitly set the names. Here we just change the mixin based on the
        // strategy.
        if (propertyNamingStrategy.equals("SNAKE_CASE")) {
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.addMixIn(Annotation.class, AnnotationMixinSC.class);
            builder.addMixIn(Media.class, MediaMixinSC.class);
        }
        else {
            builder.addMixIn(Annotation.class, AnnotationMixinCC.class);
            builder.addMixIn(Media.class, MediaMixinCC.class);
        }

        builder.addModule(simpleModule);
    }
}
