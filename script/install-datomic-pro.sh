#!/bin/bash

if [[ -z $(which wget) ]]; then
  echo "Please install wget first."
  exit 1
fi

# DATOMIC_PATH=""
# DATOMIC_USERNAME=""
# DATOMIC_PASSWORD=""
# DATOMIC_LICENSE=""

if [[ -z ${DATOMIC_PATH} ]]; then
  echo "Please set DATOMIC_PATH to the location you want to run your Datomic transactor from."
  exit 1
fi

if [[ -z ${DATOMIC_USERNAME} ]]; then
  echo "Please set DATOMIC_USERNAME to the email you registered with."
  exit 1
fi

if [[ -z ${DATOMIC_PASSWORD} ]]; then
  echo "Please set DATOMIC_PASSWORD to the download key on your https://my.datomic.com/account page."
  exit 1
fi

if [[ -z ${DATOMIC_LICENSE} ]]; then
  echo "Please set DATOMIC_LICENSE to the license you were provided when you registered."
  exit 1
fi

DEFAULT_DATOMIC_VERSION=0.9.5661

[[ -z ${DATOMIC_VERSION} ]] && DATOMIC_VERSION=${DEFAULT_DATOMIC_VERSION} && echo "Installing version $DATOMIC_VERSION. To use a different version, cancel and set DATOMIC_VERSION var."

mkdir -p $DATOMIC_PATH
rm -f $DATOMIC_PATH/transactor.properties
touch $DATOMIC_PATH/transactor.properties

cat <<EOT >> $DATOMIC_PATH/transactor.properties
protocol=dev
host=localhost
port=4334

license=$DATOMIC_LICENSE

memory-index-threshold=32m
memory-index-max=256m
object-cache-max=128m

data-dir=../data
log-dir=../logs
EOT

cd $DATOMIC_PATH

wget -O datomic-pro-$DATOMIC_VERSION.zip --http-user=$DATOMIC_USERNAME --http-password=$DATOMIC_PASSWORD https://my.datomic.com/downloads/pro/$DATOMIC_VERSION
unzip -o datomic-pro-$DATOMIC_VERSION.zip
rm -f datomic-pro-$DATOMIC_VERSION.zip
rm -f runtime
ln -s datomic-pro-$DATOMIC_VERSION runtime
