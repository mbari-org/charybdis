package org.mbari.charybdis.etc.jackson;

import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.SerializationContext;

import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

        public DurationSerializer() {
            this(null);
        }

        public DurationSerializer(Class<Duration> t) {
            super(t);
        }

        @Override
        public void serialize(Duration duration, tools.jackson.core.JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeNumber(duration.toMillis());
        }
}
