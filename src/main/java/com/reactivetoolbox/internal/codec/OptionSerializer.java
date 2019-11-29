package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.reactivetoolbox.core.lang.Option;
import org.reactivetoolbox.core.lang.ThrowingFunctions;
import org.reactivetoolbox.core.lang.ThrowingFunctions.TFN1;

import java.io.IOException;
import java.util.function.Consumer;

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
        final var nullWriter = wrap((v) -> { gen.writeNull(); return null;});
        final var objWriter = wrap((v) -> { gen.writeObject(v); return null;});

        value.whenEmpty(() -> nullWriter.accept(null))
             .whenPresent(objWriter);
    }

    private <T> Consumer<T> wrap(final TFN1<Void, T> fn) {
        return (val) -> {
            try {
                fn.apply(val);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
