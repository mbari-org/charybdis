package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;

public abstract class MediaMixin {

    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("durationMillis")
    abstract Duration getDuration();
}
