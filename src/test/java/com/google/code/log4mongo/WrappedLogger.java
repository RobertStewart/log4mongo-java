package com.google.code.log4mongo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 *
 */
public class WrappedLogger {

    private final Logger delegate;

    public WrappedLogger(Logger logger) {
        this.delegate = logger;
    }

    /**
     * @see org.apache.log4j.Category#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal(Object message, Throwable t) {
        delegate.fatal(message, t);
    }

    /**
     * @see org.apache.log4j.Category#fatal(java.lang.Object)
     */
    public void fatal(Object message) {
        delegate.fatal(message);
    }

    public void fatal(String format, Object... params) {
        //fatal is always enabled
        if( params[params.length - 1] instanceof Throwable ) {
            delegate.fatal(generateFormattedMessage(format, params), (Throwable) params[params.length-1]);
        } else {
            delegate.fatal(generateFormattedMessage(format, params));
        }
    }

    public boolean isErrorEnabled() {
        return delegate.isEnabledFor(Level.ERROR);
    }

    /**
     * @see org.apache.log4j.Category#error(java.lang.Object, java.lang.Throwable)
     */
    public void error(Object message, Throwable t) {
        delegate.error(message, t);
    }

    /**
     * @see org.apache.log4j.Category#error(java.lang.Object)
     */
    public void error(Object message) {
        delegate.error(message);
    }

    public void error(String format, Object... params) {
        //skip isErrorEnabled() check here since more than likely it is.
        //Log4j checks anyway, so...
        if( params[params.length - 1] instanceof Throwable ) {
            final String message = generateFormattedMessage(format, params);
            delegate.error(message, (Throwable) params[params.length-1]);
        } else {
            delegate.error(generateFormattedMessage(format, params));
        }
    }

    public boolean isWarnEnabled() {
        return delegate.isEnabledFor(Level.WARN);
    }

    /**
     * @see org.apache.log4j.Category#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn(Object message, Throwable t) {
        delegate.warn(message, t);
    }

    /**
     * @see org.apache.log4j.Category#warn(java.lang.Object)
     */
    public void warn(Object message) {
        delegate.warn(message);
    }

    public void warn(String format, Object... params) {
        if( params[params.length - 1] instanceof Throwable ) {
            delegate.warn(generateFormattedMessage(format, params), (Throwable) params[params.length-1]);
        } else {
            delegate.warn(generateFormattedMessage(format, params));
        }
    }

    /**
     * @see org.apache.log4j.Category#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    /**
     * @see org.apache.log4j.Category#info(java.lang.Object, java.lang.Throwable)
     */
    public void info(Object message, Throwable t) {
        delegate.info(message, t);
    }

    /**
     * @see org.apache.log4j.Category#info(java.lang.Object)
     */
    public void info(Object message) {
        delegate.info(message);
    }

    public void info(String format, Object... params) {
        if (isInfoEnabled()) {
            if( params[params.length - 1] instanceof Throwable ) {
                delegate.info(generateFormattedMessage(format, params), (Throwable) params[params.length-1]);
            } else {
                delegate.info(generateFormattedMessage(format, params));
            }
        }
    }

    /**
     * @see org.apache.log4j.Category#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    /**
     * @see org.apache.log4j.Category#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug(Object message, Throwable t) {
        delegate.debug(message, t);
    }

    /**
     * @see org.apache.log4j.Category#debug(java.lang.Object)
     */
    public void debug(Object message) {
        delegate.debug(message);
    }

    public void debug(String format, Object... params) {
        if (isDebugEnabled()) {
            if( params[params.length - 1] instanceof Throwable ) {
                delegate.debug(generateFormattedMessage(format, params), (Throwable) params[params.length-1]);
            } else {
                delegate.debug(generateFormattedMessage(format, params));
            }
        }
    }

    private String generateFormattedMessage(String format, Object... params) {
        return MessageFormat.format(format, params);
    }

    /**
     * @see org.apache.log4j.Logger#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    /**
     * @see org.apache.log4j.Logger#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object message, Throwable t) {
        delegate.trace(message, t);
    }

    /**
     * @see org.apache.log4j.Logger#trace(java.lang.Object)
     */
    public void trace(Object message) {
        delegate.trace(message);
    }

    public void trace(String format, Object... params) {
        if (isTraceEnabled()) {
            if( params[params.length - 1] instanceof Throwable ) {
                delegate.trace(generateFormattedMessage(format, params), (Throwable) params[params.length-1]);
            } else {
                delegate.trace(generateFormattedMessage(format, params));
            }
        }
    }

}
