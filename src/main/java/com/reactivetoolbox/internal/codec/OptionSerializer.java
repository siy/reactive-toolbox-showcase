package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.reactivetoolbox.core.lang.Option;

import java.io.IOException;

public class OptionSerializer extends StdSerializer<Option> {
    public OptionSerializer() {
        this(null);
    }

    protected OptionSerializer(final Class<Option> t) {
        super(t);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serialize(final Option value, final JsonGenerator gen, final SerializerProvider provider) throws IOException {
        value.whenEmpty(() -> writeNull(gen))
             .whenPresent(gen::setCurrentValue);
    }

    private void writeNull(final JsonGenerator gen) {
        try {
            gen.writeNull();
        } catch (IOException e) {
            // ignore
        }
    }
}
