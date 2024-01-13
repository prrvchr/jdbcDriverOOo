#!/bin/bash

docker run -v ~/github/jdbcDriverOOo:/working-dir ghcr.io/fluidattacks/makes/amd64 m gitlab:fluidattacks/universe@trunk /skims scan ./_fascan.yml
