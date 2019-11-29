package com.reactivetoolbox.internal;

import org.reactivetoolbox.core.log.CoreLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements CoreLogger {
    private final Logger logger = LoggerFactory.getLogger("reactive-toolbox-logger");

    @Override
    public CoreLogger trace(final String msg) {
        logger.trace(msg);
        return this;
    }

    @Override
    public CoreLogger trace(final String msg, final Throwable throwable) {
        logger.trace(msg, throwable);
        return this;
    }

    @Override
    public CoreLogger trace(final String msg, final Object... params) {
        logger.trace(msg, params);
        return this;
    }

    @Override
    public CoreLogger debug(final String msg) {
        logger.debug(msg);
        return this;
    }

    @Override
    public CoreLogger debug(final String msg, final Throwable throwable) {
        logger.debug(msg, throwable);
        return this;
    }

    @Override
    public CoreLogger debug(final String msg, final Object... params) {
        logger.debug(msg, params);
        return this;
    }

    @Override
    public CoreLogger info(final String msg) {
        logger.info(msg);
        return this;
    }

    @Override
    public CoreLogger info(final String msg, final Throwable throwable) {
        logger.info(msg, throwable);
        return this;
    }

    @Override
    public CoreLogger info(final String msg, final Object... params) {
        logger.info(msg, params);
        return this;
    }

    @Override
    public CoreLogger warn(final String msg) {
        logger.warn(msg);
        return this;
    }

    @Override
    public CoreLogger warn(final String msg, final Throwable throwable) {
        logger.warn(msg, throwable);
        return this;
    }

    @Override
    public CoreLogger warn(final String msg, final Object... params) {
        logger.warn(msg, params);
        return this;
    }

    @Override
    public CoreLogger error(final String msg) {
        logger.error(msg);
        return this;
    }

    @Override
    public CoreLogger error(final String msg, final Throwable throwable) {
        logger.error(msg, throwable);
        return this;
    }

    @Override
    public CoreLogger error(final String msg, final Object... params) {
        logger.error(msg, params);
        return this;
    }
}
