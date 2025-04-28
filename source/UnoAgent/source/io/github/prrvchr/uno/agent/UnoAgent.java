/*
╔════════════════════════════════════════════════════════════════════════════════════╗
║                                                                                    ║
║   Copyright (c) 2020-25 https://prrvchr.github.io                                  ║
║                                                                                    ║
║   Permission is hereby granted, free of charge, to any person obtaining            ║
║   a copy of this software and associated documentation files (the "Software"),     ║
║   to deal in the Software without restriction, including without limitation        ║
║   the rights to use, copy, modify, merge, publish, distribute, sublicense,         ║
║   and/or sell copies of the Software, and to permit persons to whom the Software   ║
║   is furnished to do so, subject to the following conditions:                      ║
║                                                                                    ║
║   The above copyright notice and this permission notice shall be included in       ║
║   all copies or substantial portions of the Software.                              ║
║                                                                                    ║
║   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,                  ║
║   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES                  ║
║   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.        ║
║   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY             ║
║   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,             ║
║   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE       ║
║   OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                    ║
║                                                                                    ║
╚════════════════════════════════════════════════════════════════════════════════════╝
*/
package io.github.prrvchr.uno.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;


public final class UnoAgent {

    private static Instrumentation sInstrumentation;
    private static ClassLoader sAddUrlThis;
    private static Method sAddUrlMethod;

    private UnoAgent() {
    }

    /**
     * Called by the JRE. <em>Do not call this method from user code.</em>
     *
     * <p>
     * This method is automatically invoked when the JRE loads this class as an
     * agent using the option {@code -javaagent:jarPathOfThisClass}.
     *
     * <p>
     * For this to work the {@code MANIFEST.MF} file <strong>must</strong>
     * include the line {@code Premain-Class: org.libreoffice.uno.agent.UnoAgent}.
     *
     * @param agentArgs agent arguments; currently ignored
     * @param instrumentation provided by the JRE
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("UnoAgent.premain()");
        if (instrumentation == null) {
            throw new NullPointerException("UnoAgent instrumentation");
        }
        if (sInstrumentation == null) {
            sInstrumentation = instrumentation;
        }
    }

    /**
     * Get the java.lang.instrument.Instrumentation.
     *
     */
    public static synchronized Instrumentation getInstrumentation() {
        return sInstrumentation;
    }

    /**
     * Adds a JAR file to the list of JAR files searched by the system class
     * loader. This effectively adds a new JAR to the class path.
     *
     * @param uri the JAR uri as string (ie: file://)
     * @throws URISyntaxException if there is an error parsing uri string
     * @throws IOException if there is an error accessing the JAR file
     */
    public static synchronized void addToClassPath(String uri) throws IOException, URISyntaxException {
        if (uri == null) {
            throw new NullPointerException();
        }
        addToClassPath(new File(new URI(uri)));
    }

    /**
     * Adds a JAR file to the list of JAR files searched by the system class
     * loader. This effectively adds a new JAR to the class path.
     *
     * @param jarFile the JAR file to add
     * @throws IOException if there is an error accessing the JAR file
     */
    public static synchronized void addToClassPath(File jarFile) throws IOException {
        if (jarFile == null) {
            throw new NullPointerException();
        }
        // do our best to ensure consistent behaviour across methods
        if (!jarFile.exists()) {
            throw new FileNotFoundException(jarFile.getAbsolutePath());
        }
        if (!jarFile.canRead()) {
            throw new IOException("can't read jar: " + jarFile.getAbsolutePath());
        }
        if (jarFile.isDirectory()) {
            throw new IOException("not a jar: " + jarFile.getAbsolutePath());
        }

        // add the jar using instrumentation, or fall back to reflection
        if (sInstrumentation != null) {
            sInstrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            return;
        }
        try {
            getAddUrlMethod().invoke(sAddUrlThis, jarFile.toURI().toURL());
        } catch (SecurityException iae) {
            throw new RuntimeException("security model prevents access to method", iae);
        } catch (Throwable t) {
            // IllegalAccessException
            // IllegalArgumentException
            // InvocationTargetException
            // MalformedURLException
            // (or a runtime error)
            throw new AssertionError("internal error", t);
        }
    }

    /**
     * Returns whether the extending the class path is supported on the host
     * JRE. If this returns false, the most likely causes are:
     * <ul>
     * <li> the manifest is not configured to load the agent or the
     * {@code -javaagent:jarpath} argument was not specified (Java 9+);
     * <li> security restrictions are preventing reflective access to the class
     * loader (Java &le; 8);
     * <li> the underlying VM neither supports agents nor uses URLClassLoader as
     * its system class loader (extremely unlikely from Java 1.6+).
     * </ul>
     *
     * @return true if the Jar loader is supported on the Java runtime
     */
    public static synchronized boolean isSupported() {
        boolean supported = false;
        try {
            supported = sInstrumentation != null || getAddUrlMethod() != null;
        } catch (Throwable t) { }
        return supported;
    }

    /**
     * Returns a string that describes the strategy being used to add JAR files
     * to the class path. This is meant mainly to assist with debugging and
     * diagnosing client issues.
     *
     * @return returns {@code "none"} if no strategy was found, otherwise a
     *     short describing the method used; the value {@code "reflection"}
     *     indicates that a fallback not compatible with Java 9+ is being used
     */
    public static synchronized String getStrategy() {
        String strat = "none";
        if (sInstrumentation != null) {
            strat = "agent";
        } else {
            try {
                if (isSupported()) {
                    strat = "reflection";
                }
            } catch (Throwable t) { }
        }
        return strat;
    }

    private static Method getAddUrlMethod() {
        if (sAddUrlMethod == null) {
            sAddUrlThis = ClassLoader.getSystemClassLoader();
            if (sAddUrlThis instanceof URLClassLoader) {
                try {
                    final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    sAddUrlMethod = method;
                } catch (NoSuchMethodException nsm) {
                    // violates URLClassLoader API!
                    throw new AssertionError();
                }
            } else {
                throw new UnsupportedOperationException("did you forget -javaagent:<jarpath>?");
            }
        }
        return sAddUrlMethod;
    }

}
