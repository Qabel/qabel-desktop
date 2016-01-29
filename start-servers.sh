#!/bin/bash
set -e
cd qabel-drop
if [ ! -d venv ]; then
  virtualenv --python=python3 venv
fi
source venv/bin/activate
pip install -r requirements.txt
if [ ! -d config.py ]; then
  cp config.py.example config.py
  sed --in-place "s/'qabel_drop'/'qabel_drop','host':'localhost','port':'5432','username':'qabel','password':'qabel_test'/" config.py
fi
python manage.py runserver > ../drop.log 2>&1 &
echo $! > ../drop.pid
deactivate
cd ..

cd qabel-accounting
if [ ! -d venv ]; then
  virtualenv --python=python3 venv
fi
source venv/bin/activate
pip install -r requirements.txt
python manage.py testserver testdata.json --addrport 9696 > ../accounting.log 2>&1 &
echo $! > ../accounting.pid
deactivate
cd ..

