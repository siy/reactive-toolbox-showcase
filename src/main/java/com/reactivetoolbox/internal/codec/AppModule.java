package com.reactivetoolbox.internal.codec;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.reactivetoolbox.api.response.Status;
import com.reactivetoolbox.domain.BaseId;
import org.reactivetoolbox.core.lang.Option;

public class AppModule extends SimpleModule {
    public AppModule() {
        addSerializer(Status.class, new StatusSerializer());
        addSerializer(Option.class, new OptionSerializer());
        addSerializer(BaseId.class, new BaseIdSerializer());
    }
}
