#!
# -*- coding: utf_8 -*-

import uno
import unohelper

from com.sun.star.ucb.ConnectionMode import OFFLINE
from com.sun.star.ucb.ConnectionMode import ONLINE
from com.sun.star.auth.RestRequestTokenType import TOKEN_NONE
from com.sun.star.auth.RestRequestTokenType import TOKEN_URL
from com.sun.star.auth.RestRequestTokenType import TOKEN_REDIRECT
from com.sun.star.auth.RestRequestTokenType import TOKEN_QUERY
from com.sun.star.auth.RestRequestTokenType import TOKEN_JSON
from com.sun.star.auth.RestRequestTokenType import TOKEN_SYNC

from com.sun.star.sdbc import XRestProvider

from .configuration import g_host
from .configuration import g_url
from .configuration import g_timestamp
from .configuration import g_IdentifierRange
from .configuration import g_userfields
from .configuration import g_peoplefields
from .logger import getLogger

import json


class Provider(unohelper.Base,
               XRestProvider):
    def __init__(self, ctx):
        self.ctx = ctx
        self.SessionMode = OFFLINE
        self._Error = ''
        self.Logger = getLogger(self.ctx)

    @property
    def Host(self):
        return g_host
    @property
    def BaseUrl(self):
        return g_url
    @property
    def TimeStampPattern(self):
        return g_timestamp
    @property
    def IdentifierRange(self):
        return g_IdentifierRange
    @property
    def PeopleFields(self):
        return KeyMap(g_peoplefields)

    def isOnLine(self):
        return self.SessionMode != OFFLINE
    def isOffLine(self):
        return self.SessionMode != ONLINE

    def getRequestParameter(self, method, data=None):
        parameter = uno.createUnoStruct('com.sun.star.auth.RestRequestParameter')
        parameter.Name = method
        if method == 'getNewIdentifier':
            parameter.Method = 'GET'
            parameter.Url = '%s/files/generateIds' % self.BaseUrl
            parameter.Query = '{"count": "%s", "space": "drive"}' % max(g_IdentifierRange)
            token = uno.createUnoStruct('com.sun.star.auth.RestRequestToken')
            token.Type = TOKEN_NONE
            enumerator = uno.createUnoStruct('com.sun.star.auth.RestRequestEnumerator')
            enumerator.Field = 'ids'
            enumerator.Token = token
            parameter.Enumerator = enumerator
        elif method == 'getUser':
            parameter.Method = 'GET'
            parameter.Url = '%s/people/me' % self.BaseUrl
            parameter.Query = '{"personFields": "%s"}' % g_userfields
        elif method == 'getPeople':
            parameter.Method = 'GET'
            parameter.Url = '%s/people/me/connections' % self.BaseUrl
            sync = data.getValue('Token')
            if sync:
                token = '"syncToken": "%s"' % sync
            else:
                token = '"requestSyncToken": true'
            parameter.Query = '{%s, "personFields": "%s"}' % (token, g_peoplefields)
            token = uno.createUnoStruct('com.sun.star.auth.RestRequestToken')
            token.Type = TOKEN_QUERY | TOKEN_SYNC
            token.Field = 'nextPageToken'
            token.Value = 'pageToken'
            token.SyncField = 'nextSyncToken'
            enumerator = uno.createUnoStruct('com.sun.star.auth.RestRequestEnumerator')
            enumerator.Field = 'connections'
            enumerator.Token = token
            parameter.Enumerator = enumerator

        elif method == 'getItem':
            parameter.Method = 'GET'
            parameter.Url = '%s/files/%s' % (self.BaseUrl, data.getValue('Id'))
            parameter.Query = '{"fields": "%s"}' % g_itemfields
        elif method == 'getFolderContent':
            parameter.Method = 'GET'
            parameter.Url = '%s/files' % self.BaseUrl
            query = "'%s' in parents" % data.getValue('Id')
            parameter.Query = '{"fields": "%s", "pageSize": "%s", "q": "%s"}' % \
                (g_childfields, g_pages, query)
            token = uno.createUnoStruct('com.sun.star.auth.RestRequestToken')
            token.Type = TOKEN_QUERY
            token.Field = 'nextPageToken'
            token.Value = 'pageToken'
            enumerator = uno.createUnoStruct('com.sun.star.auth.RestRequestEnumerator')
            enumerator.Field = 'files'
            enumerator.Token = token
            parameter.Enumerator = enumerator
        elif method == 'getDocumentContent':
            parameter.Method = 'GET'
            parameter.Url = '%s/files/%s' % (self.BaseUrl, data.getValue('Id'))
            mediatype = data.getValue('MediaType')
            if mediatype in g_doc_map:
                parameter.Url += '/export'
                parameter.Query = '{"mimeType": "%s"}' % mediatype
            else:
                parameter.Query = '{"alt": "media"}'
        elif method == 'updateTitle':
            parameter.Method = 'PATCH'
            parameter.Url = '%s/files/%s' % (self.BaseUrl, data.getValue('Id'))
            parameter.Json = '{"name": "%s"}' % data.getValue('Title')
        elif method == 'updateTrashed':
            parameter.Method = 'PATCH'
            parameter.Url = '%s/files/%s' % (self.BaseUrl, data.getValue('Id'))
            parameter.Json = '{"trashed": true}'
        elif method == 'createNewFolder':
            parameter.Method = 'POST'
            parameter.Url = '%s/files' % self.BaseUrl
            parameter.Json = '{"id": "%s", "parents": "%s", "name": "%s", "mimeType": "%s"}' % \
                                (data.getValue('Id'), data.getValue('ParentId'),
                                 data.getValue('Title'), data.getValue('MediaType'))
        elif method == 'getUploadLocation':
            parameter.Method = 'PATCH'
            parameter.Url = '%s/%s' % (self.UploadUrl, data.getValue('Id'))
            parameter.Query = '{"uploadType": "resumable"}'
            parameter.Header = '{"X-Upload-Content-Type": "%s"}' % data.getValue('MediaType')
        elif method == 'getNewUploadLocation':
            mimetype = None if data.getValue('Size') else data.getValue('MediaType')
            parameter.Method = 'POST'
            parameter.Url = self.UploadUrl
            parameter.Query = '{"uploadType": "resumable"}'
            parameter.Json = '{"id": "%s", "parents": "%s", "name": "%s", "mimeType": "%s"}' % \
                                (data.getValue('Id'), data.getValue('ParentId'),
                                 data.getValue('Title'), data.getValue('MediaType'))
            parameter.Header = '{"X-Upload-Content-Type": "%s"}' % data.getValue('MediaType')
        elif method == 'getUploadStream':
            parameter.Method = 'PUT'
            parameter.Url = data.getValue('Location')
        return parameter

    def getUser(self, request, name):
        parameter = self.getRequestParameter('getUser')
        return request.execute(parameter)

    def transform(self, name, value):
        if name == 'Resource':
            value = value.split('/').pop()
        elif name == 'ParentId':
            value = [value]
        return value

    def getUserId(self, user):
        return user.getValue('resourceName').split('/').pop()
        #return user.getValue('resourceName')
    def getItemId(self, item):
        return item.getDefaultValue('resourceName', '').split('/').pop()

    def getItemParent(self, item, rootid):
        return item.getDefaultValue('parents', (rootid, ))
    def getItemTitle(self, item):
        return item.getDefaultValue('name', None)
    def getItemCreated(self, item, timestamp=None):
        created = item.getDefaultValue('createdTime', None)
        if created:
            return self.parseDateTime(created)
        return timestamp
    def getItemModified(self, item, timestamp=None):
        modified = item.getDefaultValue('modifiedTime', None)
        if modified:
            return self.parseDateTime(modified)
        return timestamp
    def getItemMediaType(self, item):
        return item.getValue('mimeType')
    def getItemSize(self, item):
        return item.getDefaultValue('size', 0)
    def getItemTrashed(self, item):
        return item.getDefaultValue('trashed', False)
    def getItemCanAddChild(self, item):
        return item.getValue('capabilities').getValue('canAddChildren')
    def getItemCanRename(self, item):
        return item.getValue('capabilities').getValue('canRename')
    def getItemIsReadOnly(self, item):
        return not item.getValue('capabilities').getValue('canEdit')
    def getItemIsVersionable(self, item):
        return item.getValue('capabilities').getValue('canReadRevisions')
