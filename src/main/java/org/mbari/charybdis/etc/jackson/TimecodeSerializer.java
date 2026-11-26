package org.mbari.charybdis.etc.jackson;

import tools.jackson.databind.ser.std.StdSerializer;
import org.mbari.vcr4j.time.Timecode;
import tools.jackson.databind.SerializationContext;

public class TimecodeSerializer extends StdSerializer<Timecode> {

    public TimecodeSerializer() {
        this(null);
    }

    public TimecodeSerializer(Class<Timecode> t) {
        super(t);
    }

    @Override
    public void serialize(Timecode timecode, tools.jackson.core.JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
        jsonGenerator.writeString(timecode.toString());
    }

}
