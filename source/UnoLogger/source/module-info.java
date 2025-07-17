module io.github.prrvchr.unologger {
    provides java.lang.System.LoggerFinder with io.github.prrvchr.uno.logger.UnoLoggerFinder;

    exports io.github.prrvchr.uno.logger;
}