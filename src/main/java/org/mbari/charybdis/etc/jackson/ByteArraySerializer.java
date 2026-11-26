package org.mbari.charybdis.etc.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.SerializationContext;

import java.util.HexFormat;

public class ByteArraySerializer extends StdSerializer<byte[]> {

    HexFormat hexFormat = HexFormat.of();

    public ByteArraySerializer() {
        this(null);
    }

    public ByteArraySerializer(Class<byte[]> t) {
        super(t);
    }

    @Override
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
        jsonGenerator.writeString(hexFormat.formatHex(bytes));
    }


}
