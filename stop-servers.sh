#!/bin/bash
set -x
cd drop-server
bash drop-server.sh stop
cd ..
cat accounting.pid | xargs kill
rm accounting.pid
cat block.pid | xargs kill
rm block.pid


