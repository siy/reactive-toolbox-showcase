package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.reactivetoolbox.core.lang.List;

import java.io.IOException;

public class ListSerializer<E> extends StdSerializer<List<E>> {
    public ListSerializer() {
        this(null);
    }

    protected ListSerializer(final Class<List<E>> t) {
        super(t);
    }

    @Override
    public void serialize(final List<E> value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        value.apply(element -> {
            try {
                gen.writeObject(element);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
        gen.writeEndArray();
    }
}
