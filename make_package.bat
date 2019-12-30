
cd .\jdbcDriverOOo

..\zip.exe -0 jdbcDriverOOo.zip mimetype

..\zip.exe -r jdbcDriverOOo.zip *

cd ..

move /Y .\jdbcDriverOOo\jdbcDriverOOo.zip .\jdbcDriverOOo.oxt
