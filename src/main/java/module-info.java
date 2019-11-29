module com.reactivetoolbox {
    requires logback.classic;
    requires org.slf4j;
    requires jooby;
    requires jooby.jackson;
    requires com.fasterxml.jackson.databind;
    requires org.reactivetoolbox.core;
    exports com.reactivetoolbox.api.response;
    exports com.reactivetoolbox.api.request;
    exports com.reactivetoolbox.api.service;
    exports com.reactivetoolbox.domain;
//    exports org.reactivetoolbox.core.async;
//    exports org.reactivetoolbox.core.lang;
//    exports org.reactivetoolbox.core.lang.support;
}