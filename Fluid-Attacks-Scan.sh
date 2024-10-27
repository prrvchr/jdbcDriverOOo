#!/bin/bash
docker run -v ~/github/jdbcDriverOOo:/src -v ./_fascan.yml:/fascan.yml fluidattacks/cli:latest skims scan /fascan.yml
# docker system prune -f
