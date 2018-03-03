#!/bin/bash

if [[ -z $(which wget) ]]; then
  echo "Please install wget first."
  exit 1
fi

# DATOMIC_PATH=""

if [[ -z ${DATOMIC_PATH} ]]; then
  echo "Please set DATOMIC_PATH to the location you want to run your Datomic transactor from."
  exit 1
fi

DEFAULT_DATOMIC_VERSION=0.9.5661

[[ -z ${DATOMIC_VERSION} ]] && DATOMIC_VERSION=${DEFAULT_DATOMIC_VERSION} && echo "Installing version $DATOMIC_VERSION. To use a different version, cancel and set DATOMIC_VERSION var."

mkdir -p $DATOMIC_PATH
rm -f $DATOMIC_PATH/transactor.properties
touch $DATOMIC_PATH/transactor.properties

cat <<EOT >> $DATOMIC_PATH/transactor.properties
protocol=free
host=localhost
port=4334

memory-index-threshold=32m
memory-index-max=256m
object-cache-max=128m

data-dir=../data
log-dir=../logs
EOT

cd $DATOMIC_PATH

wget -O datomic-free-$DATOMIC_VERSION.zip https://my.datomic.com/downloads/free/$DATOMIC_VERSION
unzip -o datomic-free-$DATOMIC_VERSION.zip
rm -f datomic-free-$DATOMIC_VERSION.zip
rm -f runtime
ln -s datomic-free-$DATOMIC_VERSION runtime
