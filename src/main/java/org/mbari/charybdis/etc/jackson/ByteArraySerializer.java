package org.mbari.charybdis.etc.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
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
    public void serialize(byte[] bytes, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(hexFormat.formatHex(bytes));
    }


}
