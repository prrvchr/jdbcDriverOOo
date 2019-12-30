
cd .\CloudContactOOo

..\zip.exe -0 CloudContactOOo.zip mimetype

..\zip.exe -r CloudContactOOo.zip *

cd ..

move /Y .\CloudContactOOo\CloudContactOOo.zip .\CloudContactOOo.oxt
