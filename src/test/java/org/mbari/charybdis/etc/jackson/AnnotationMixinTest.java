package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.ImageReference;
import org.mbari.vcr4j.time.Timecode;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationMixinTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(Timecode.class, new TimecodeSerializer());
        objectMapper.addMixIn(Annotation.class, AnnotationMixin.class);
        objectMapper.registerModule(simpleModule);
    }

    @Test
    void imageReferencesSerializedAndImagesOmitted() throws Exception {
        var imageRef = new ImageReference();
        imageRef.setUrl(URI.create("http://example.com/image.jpg").toURL());

        var annotation = new Annotation();
        annotation.setImageReferences(List.of(imageRef));

        var json = objectMapper.writeValueAsString(annotation);
        var node = objectMapper.readTree(json);

        assertFalse(node.has("images"), "images field should be omitted");
        assertTrue(node.has("imageReferences") || node.has("image_references"),
                "imageReferences field should be present");
    }
}
