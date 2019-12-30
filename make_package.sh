#!/bin/bash

cd ./CloudContactOOo/
./make_rdb.sh

cd ../jdbcDriverOOo/
zip -0 jdbcDriverOOo.zip mimetype
zip -r jdbcDriverOOo.zip *
cd ..

mv ./jdbcDriverOOo/jdbcDriverOOo.zip ./jdbcDriverOOo.oxt
