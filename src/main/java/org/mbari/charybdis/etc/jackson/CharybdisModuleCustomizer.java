package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.time.Timecode;

@Singleton
public class CharybdisModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(Timecode.class, new TimecodeSerializer());
        simpleModule.addSerializer(byte[].class, new ByteArraySerializer());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.addMixIn(Annotation.class, AnnotationMixin.class);
        mapper.addMixIn(Media.class, MediaMixin.class);
        mapper.registerModule(simpleModule);
    }
}
