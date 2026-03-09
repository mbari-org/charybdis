package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.mbari.vars.annosaurus.sdk.r1.models.ImageReference;
import org.mbari.vcr4j.VideoIndex;

import java.time.Duration;
import java.util.List;

public abstract class AnnotationMixin {

    @JsonIgnore
    abstract VideoIndex getVideoIndex();

    @JsonIgnore
    abstract List<ImageReference> getImages();

    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("durationMillis")
    abstract Duration getDuration();

    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("elapsedTimeMillis")
    abstract Duration getElapsedTime();
}
