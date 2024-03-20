package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

        public DurationSerializer() {
            this(null);
        }

        public DurationSerializer(Class<Duration> t) {
            super(t);
        }

        @Override
        public void serialize(Duration duration, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws java.io.IOException {
            jsonGenerator.writeNumber(duration.toMillis());
        }
}
