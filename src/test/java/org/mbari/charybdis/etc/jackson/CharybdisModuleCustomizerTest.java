package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CharybdisModuleCustomizerTest {

    private ObjectMapper buildMapper(String strategy) throws Exception {
        var customizer = new CharybdisModuleCustomizer();
        Field f = CharybdisModuleCustomizer.class.getDeclaredField("propertyNamingStrategy");
        f.setAccessible(true);
        f.set(customizer, strategy);
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        customizer.customize(mapper);
        return mapper;
    }

    private Annotation buildAnnotation() {
        var a = new Annotation();
        a.setObservationUuid(UUID.randomUUID());
        a.setConcept("Nanomia bijuga");
        a.setObserver("brian");
        a.setObservationTimestamp(Instant.now());
        a.setVideoReferenceUuid(UUID.randomUUID());
        a.setElapsedTime(Duration.ofSeconds(30));
        a.setDuration(Duration.ofSeconds(5));
        return a;
    }

    private Media buildMedia() {
        var m = new Media();
        m.setVideoSequenceName("Dive 1234");
        m.setStartTimestamp(Instant.now());
        m.setDuration(Duration.ofMinutes(60));
        return m;
    }

    @Test
    void annotationCamelCase() throws Exception {
        var mapper = buildMapper("CAMEL_CASE");
        JsonNode json = mapper.readTree(mapper.writeValueAsString(buildAnnotation()));

        // Standard camelCase getter-derived keys
        assertTrue(json.has("observationUuid"),    "should have observationUuid");
        assertTrue(json.has("videoReferenceUuid"), "should have videoReferenceUuid");
        assertTrue(json.has("observationTimestamp"), "should have observationTimestamp");

        // Mixin-renamed duration fields
        assertTrue(json.has("elapsedTimeMillis"), "mixin should rename elapsedTime → elapsedTimeMillis");
        assertTrue(json.has("durationMillis"),    "mixin should rename duration → durationMillis");

        // No snake_case leakage
        assertFalse(json.has("observation_uuid"),    "should NOT have snake_case observationUuid");
        assertFalse(json.has("elapsed_time_millis"), "should NOT have snake_case elapsedTimeMillis");
        assertFalse(json.has("duration_millis"),     "should NOT have snake_case durationMillis");

        // videoIndex must be suppressed by @JsonIgnore in mixin
        assertFalse(json.has("videoIndex"), "videoIndex should be @JsonIgnore'd");
    }

    @Test
    void annotationSnakeCase() throws Exception {
        var mapper = buildMapper("SNAKE_CASE");
        JsonNode json = mapper.readTree(mapper.writeValueAsString(buildAnnotation()));

        // Strategy-converted keys
        assertTrue(json.has("observation_uuid"),    "should have observation_uuid");
        assertTrue(json.has("video_reference_uuid"), "should have video_reference_uuid");
        assertTrue(json.has("observation_timestamp"), "should have observation_timestamp");

        // Mixin-renamed duration fields (explicit @JsonProperty wins over strategy)
        assertTrue(json.has("elapsed_time_millis"), "mixin should rename elapsedTime → elapsed_time_millis");
        assertTrue(json.has("duration_millis"),     "mixin should rename duration → duration_millis");

        // No camelCase leakage
        assertFalse(json.has("observationUuid"),  "should NOT have camelCase observationUuid");
        assertFalse(json.has("elapsedTimeMillis"), "should NOT have camelCase elapsedTimeMillis");
        assertFalse(json.has("durationMillis"),   "should NOT have camelCase durationMillis");

        // videoIndex must be suppressed by @JsonIgnore in mixin
        assertFalse(json.has("videoIndex"), "videoIndex should be @JsonIgnore'd");
    }

    @Test
    void mediaCamelCase() throws Exception {
        var mapper = buildMapper("CAMEL_CASE");
        JsonNode json = mapper.readTree(mapper.writeValueAsString(buildMedia()));

        assertTrue(json.has("videoSequenceName"), "should have videoSequenceName");
        assertTrue(json.has("startTimestamp"),    "should have startTimestamp");
        assertTrue(json.has("durationMillis"),    "mixin should rename getDuration() → durationMillis");

        assertFalse(json.has("video_sequence_name"), "should NOT have snake_case key");
        assertFalse(json.has("duration_millis"),     "should NOT have snake_case duration key");
    }

    @Test
    void mediaSnakeCase() throws Exception {
        var mapper = buildMapper("SNAKE_CASE");
        JsonNode json = mapper.readTree(mapper.writeValueAsString(buildMedia()));

        assertTrue(json.has("video_sequence_name"), "should have video_sequence_name");
        assertTrue(json.has("start_timestamp"),     "should have start_timestamp");
        assertTrue(json.has("duration_millis"),     "mixin should rename getDuration() → duration_millis");

        assertFalse(json.has("videoSequenceName"), "should NOT have camelCase key");
        assertFalse(json.has("durationMillis"),    "should NOT have camelCase duration key");
    }
}
