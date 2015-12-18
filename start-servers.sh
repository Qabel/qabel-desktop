#!/bin/bash
cd qabel-drop
if [ ! -d venv ]; then
  virtualenv --python=python3.4 venv
fi
source venv/bin/activate
pip install -r requirements.txt
if [ ! -d config.py ]; then
  cp config.py.example config.py
fi
python manage.py runserver > ../drop.log 2>&1 &
echo $! > ../drop.pid
deactivate
cd ..

cd qabel-accounting
if [ ! -d venv ]; then
  virtualenv --python=python3.4 venv
fi
source venv/bin/activate
pip install -r requirements.txt
python manage.py testserver testdata.json --addrport 9696 > ../accounting.log 2>&1 &
echo $! > ../accounting.pid
deactivate
cd ..

