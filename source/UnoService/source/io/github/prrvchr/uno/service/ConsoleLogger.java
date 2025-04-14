package io.github.prrvchr.uno.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.sun.star.logging.XLogger;
import com.sun.star.logging.XLoggerPool;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

@SuppressWarnings("unused")
public class ConsoleLogger implements System.Logger {

    private static final String SERVICE = "io.github.prrvchr.jdbcDriverOOo.LoggerPool";
    private static final String ERROR_MSG = "UNO service <" + SERVICE + "> cant be loaded!!!";
    private static XComponentContext CONTEXT = null;
    private XLogger mLogger;
    private String mName;

    public ConsoleLogger(String name) {
        System.out.println("ConsoleLogger() 1 Name: " + name);
        mName = name;
        System.out.println("ConsoleLogger() 2 Name: " + name);
    }

    public static final void setContext(XComponentContext context) {
        System.out.println("ConsoleLogger.setContext() 1");
        CONTEXT = context;
        System.out.println("ConsoleLogger.setContext() 2");
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public boolean isLoggable(Level level) {
        boolean loggable = false;
        if (getLogger() != null) {
            System.out.println("ConsoleLogger.isLoggable() 1 : " + (level.getSeverity() >= mLogger.getLevel()));
            loggable = level.getSeverity() >= mLogger.getLevel();
        }
        return loggable;
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String key, Throwable thrown) {
        if (isLoggable(level)) {
            System.out.println("ConsoleLogger.log() 1 Msg: " + key);
            StringWriter sw = new StringWriter();
            thrown.printStackTrace(new PrintWriter(sw));
            String msg = getMessage(bundle, key, sw.toString());
            logMessage(level, msg);
            System.out.println(msg);
        }
    }

    @Override
    public void log(Level level, ResourceBundle bundle, String key, Object... parameters) {
        if (isLoggable(level)) {
            System.out.println("ConsoleLogger.log() 2 Msg: " + key);
            String msg = getMessage(bundle, key, parameters);
            logMessage(level, msg);
            System.out.println(msg);
        }
    }

    private XLogger getLogger() {
        XLogger logger = null;
        if (mLogger == null && CONTEXT != null) {
            mLogger = getUnoLoggerPool().getNamedLogger(mName);
        }
        return mLogger;
    }
    private XLoggerPool getUnoLoggerPool() {
        Object service = null;
        try {
            service = CONTEXT.getServiceManager().createInstanceWithContext(SERVICE, CONTEXT);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException(ERROR_MSG, e);
        }
        if (service != null) {
            return UnoRuntime.queryInterface(XLoggerPool.class, service);
        }
        throw new SecurityException(ERROR_MSG);
    }

    private String getMessage(ResourceBundle bundle, String key, Object... parameters) {
        String template = key;
        if (bundle != null) {
            template = bundle.getString(key);
        }
        String msg = template;
        if (parameters != null) {
            try {
                msg = MessageFormat.format(template, parameters);
            } catch (IllegalArgumentException e) { }
        }
        return msg;
    }

    private void logMessage(Level level, String msg) {
        StackTraceElement st = StackHelper.getCallerStackTrace();
        if (st != null) {
            mLogger.logp(level.getSeverity(), st.getClassName(), st.getMethodName(), msg);
        } else {
            mLogger.log(level.getSeverity(), msg);
        }
    }

    private static class StackHelper {
        private static StackTraceElement getCallerStackTrace() { 
            StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
            StackTraceElement stackTrace = null;
            for (StackTraceElement st : stackTraces) {
                if (!st.getClassName().equals(StackHelper.class.getName()) && st.getClassName().indexOf("java.lang.Thread") != 0) {
                    stackTrace = st;
                    break;
                }
            }
            return stackTrace;
        }
    }

}