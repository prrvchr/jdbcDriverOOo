package io.github.prrvchr.driver.provider;

import java.util.HashMap;
import java.util.Map;

import com.sun.star.uno.XComponentContext;

public class DriverLoggerFinder extends System.LoggerFinder {

    private static Map<String, DriverLogger> sLOGGERS = new HashMap<String, DriverLogger>();
    private XComponentContext mContext = null;

    public DriverLoggerFinder(XComponentContext context) {
        mContext = context;
    }

    @Override
    public System.Logger getLogger(String name, Module module) {
        System.out.println("DriverLoggerFinder() 1 Name: " + name);
        DriverLogger logger = null;
        if (!sLOGGERS.containsKey(name)) {
            try {
                System.out.println("DriverLoggerFinder() 2");
                logger = new DriverLogger(mContext, name, module);
                System.out.println("DriverLoggerFinder() 3 Name: " + name);
                sLOGGERS.put(name, logger);
                System.out.println("DriverLoggerFinder() 4 Name: " + name);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new SecurityException(e);
            }
        } else {
            logger = sLOGGERS.get(name);
        }
        if (logger == null) {
            throw new SecurityException();
        }
        return logger;
    }

}