package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.reactivetoolbox.api.response.Status;

import java.io.IOException;

public class StatusSerializer extends StdSerializer<Status> {
    public StatusSerializer() {
        this(null);
    }

    protected StatusSerializer(final Class<Status> t) {
        super(t);
    }

    @Override
    public void serialize(final Status value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("code", value.code());
        gen.writeStringField("message", value.message());
        gen.writeEndObject();
    }
}
