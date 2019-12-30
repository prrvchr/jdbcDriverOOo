#!/bin/bash

OOoProgram=/usr/lib/libreoffice/program
WorkBench=$(dirname "${0}")

${OOoProgram}/regview ${WorkBench}/CloudContactOOo/types.rdb
