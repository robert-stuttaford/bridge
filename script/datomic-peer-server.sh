#!/bin/bash

# requires Datomic Pro

$DATOMIC_PATH/runtime/bin/run -m datomic.peer-server -h localhost -p 8998 -a $BRIDGE_DATOMIC_CLIENT_KEY,$BRIDGE_DATOMIC_CLIENT_SECRET -d $BRIDGE_DATOMIC_DB,datomic:mem://localhost:4334/$BRIDGE_DATOMIC_DB