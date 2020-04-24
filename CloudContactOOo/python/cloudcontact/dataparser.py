#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.auth import XRestDataParser

from unolib import getNamedValue
from unolib import KeyMap


class DataParser(unohelper.Base,
                 XRestDataParser):
    def __init__(self, datasource, method):
        self.provider = datasource.Provider
        self.map = datasource.getFieldsMap(method, True)
        self.keys = self.map.getKeys()
        #map = {}
        #for key in self.keys:
        #    map[key] = {}
        #    km = self.map.getValue(key)
        #    for k in km.getKeys():
        #        map[key][k]= km.getValue(k)
        #print("dbpaser.DataParser(): %s\n%s" % (self.keys, map))

    def jsonParser(self, pairs):
        data = KeyMap()
        for key, value in pairs:
            if value is None:
                continue
            if key in self.keys:
                map = self.map.getValue(key)
                k = map.getValue('Map')
                v = self.provider.transform(k, value)
                data.setValue(k, v)
        return data if data.Count else None
