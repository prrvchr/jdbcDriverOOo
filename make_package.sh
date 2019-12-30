#!/bin/bash

cd ./CloudContactOOo/
./make_rdb.sh

cd ../gContactOOo/
zip -0 gContactOOo.zip mimetype
zip -r gContactOOo.zip *
cd ..

mv ./gContactOOo/gContactOOo.zip ./gContactOOo.oxt
