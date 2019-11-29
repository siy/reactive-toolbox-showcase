package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.reactivetoolbox.domain.BaseId;

import java.io.IOException;

public class BaseIdSerializer extends StdSerializer<BaseId> {
    public BaseIdSerializer() {
        this(null);
    }

    protected BaseIdSerializer(final Class<BaseId> t) {
        super(t);
    }

    @Override
    public void serialize(final BaseId value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeString(value.id().encoded());
    }
}
