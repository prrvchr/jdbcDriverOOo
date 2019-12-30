#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.auth import XRestDataParser

from unolib import getNamedValue
from unolib import KeyMap


class DataParser(unohelper.Base,
                 XRestDataParser):
    def __init__(self, datasource):
        self.datasource = datasource
        self.map = datasource.getFieldsMap(True)
        self.keys = self.map.getKeys()
        print("dbpaser.DataParser(): %s" % (self.keys, ))

    def jsonParser(self, pairs):
        data = KeyMap()
        for key, value in pairs:
            if value is None:
                continue
            if key in self.keys:
                map = self.map.getValue(key)
                k = map.getValue('Map')
                v = self.datasource.Provider.transform(k, value)
                data.setValue(k, v)
        return data if data.Count else None
