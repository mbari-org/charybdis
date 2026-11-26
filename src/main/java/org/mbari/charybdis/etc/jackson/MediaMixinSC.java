package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;

public abstract class MediaMixinSC {

    @JsonSerialize(using = DurationSerializer.class)
    @JsonProperty("duration_millis")
    abstract Duration getDuration();
}
