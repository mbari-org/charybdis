package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.mbari.vcr4j.time.Timecode;

import java.io.IOException;

public class TimecodeSerializer extends StdSerializer<Timecode> {

    public TimecodeSerializer() {
        this(null);
    }

    public TimecodeSerializer(Class<Timecode> t) {
        super(t);
    }

    @Override
    public void serialize(Timecode timecode, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(timecode.toString());
    }

}
