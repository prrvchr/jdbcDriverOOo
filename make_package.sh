#!/bin/bash

./rdb/make_rdb.sh

cd ./HsqlDBDriverOOo/
zip -0 HsqlDBDriverOOo.zip mimetype
zip -r HsqlDBDriverOOo.zip *
cd ..

mv ./HsqlDBDriverOOo/HsqlDBDriverOOo.zip ./HsqlDBDriverOOo.oxt
