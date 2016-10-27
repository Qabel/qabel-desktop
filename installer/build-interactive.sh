#!/bin/bash
set -e
set -x
docker build -t qabel/desktop-client-installer .
docker run --rm -ti -v "`pwd`/..:/vagrant" -w /vagrant/installer --entrypoint /bin/bash qabel/desktop-client-installer

