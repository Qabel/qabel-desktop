#!/bin/bash
set -x
cat drop.pid | xargs kill
rm drop.pid
cat accounting.pid | xargs kill
rm accounting.pid
cat block.pid | xargs kill
rm block.pid


