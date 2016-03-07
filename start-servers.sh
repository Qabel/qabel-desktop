#!/bin/bash
set -e
set -x

function waitForPort {
    started=false
    for i in `seq 0 9`; do
        if [ $(curl -I "http://localhost:"$1 | grep "HTTP/1" | cut -d' ' -f2)"" == "404" ]; then
            started=true
            break
        fi
        sleep 1
    done
    if [ ${started} != true ]; then
        echo "server on port "$1" did not start"
        exit -1
    fi
}

DIRHASH=$(pwd | shasum | cut -d" " -f1)

# qabel-drop
cd qabel-drop
if [ -f drop.pid ]; then
    echo "stopping old drop instance"
    cat drop.pid | xargs kill || echo "already gone"
fi
if [ ! -d "venv_"${DIRHASH} ]; then
  virtualenv --always-copy --python=python3.4 "venv_"${DIRHASH}
fi
source "venv_"${DIRHASH}"/bin/activate"
pip install -r requirements.txt
if [ ! -d config.py ]; then
  cp config.py.example config.py
  sed --in-place "s/'qabel_drop'/'qabel_drop','host':'localhost','port':'5432','username':'qabel','password':'qabel_test'/" config.py
fi
python manage.py runserver --host 0.0.0.0 --port 5000 > ../drop.log 2>&1 &
echo $! > ../accounting.pid
deactivate
cd ..
waitForPort 5000

# qabel-accounting
cd qabel-accounting
if [ -f accounting.pid ]; then
    echo "stopping old accounting instance"
    cat accounting.pid | xargs kill || echo "already gone"
fi
if [ ! -d "venv_"${DIRHASH} ]; then
  virtualenv --always-copy --python=python3.4 "venv_"${DIRHASH}
fi
source "venv_"${DIRHASH}"/bin/activate"
pip install -r requirements.txt
python manage.py migrate
python manage.py testserver testdata.json --addrport 0.0.0.0:9696 > ../accounting.log 2>&1 &
echo $! > ../accounting.pid
deactivate
cd ..
waitForPort 9696

# qabel-block
cd qabel-block
if [ -f block.pid ]; then
    echo "stopping old block instance"
    cat block.pid | xargs kill || echo "already gone"
fi
if [ ! -d "venv_"${DIRHASH} ]; then
  virtualenv --always-copy --python=python3.5 "venv_"${DIRHASH}
fi
if [ ! -f config.ini ]; then
  cp config.ini.example config.ini
fi
source "venv_"${DIRHASH}"/bin/activate"
pip install -r requirements.txt
cd src
python run.py --debug --dummy --dummy-log --dummy-cache --apisecret=Changeme --accounting-host=http://localhost:9696 --address=0.0.0.0 --port=9697 > ../../block.log 2>&1 &
echo $! > ../../block.pid
deactivate
cd ../..
waitForPort 9697
