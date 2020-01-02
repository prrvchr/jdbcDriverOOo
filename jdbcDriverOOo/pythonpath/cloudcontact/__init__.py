#!
# -*- coding: utf-8 -*-

import traceback
try:

    from .connection import Connection

    from .configuration import g_extension
    from .configuration import g_identifier
    from .configuration import g_host

    from .dbinit import getDataSourceUrl

    from .dbconfig import g_path

    from .dbtools import getDataSourceConnection
    from .dbtools import getDataBaseInfo
    from .dbtools import getDataSourceLocation
    from .dbtools import getDataSourceJavaInfo

    from .logger import getLogger
    from .logger import getLoggerSetting
    from .logger import getLoggerUrl
    from .logger import setLoggerSetting
    from .logger import clearLogger
    from .logger import logMessage

except Exception as e:
    print("cloudcontact.__init__() ERROR: %s - %s" % (e, traceback.print_exc()))

