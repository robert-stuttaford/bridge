# ADR 002 --> Code Organisation

## Context



## Decision

    bridge.* - top level ns
    bridge.ENTITY.* - everything about ENTITY

these could be directly in bridge, in which case it's the engine, or in an ENTITY, in which case it's the actual implementation for that ENTITY.

from 'bottom' to 'top':

    .schema - datomic schema
    .spec - specs
    .data - all data transformations, all Datomic interactions
    .api - controller layer for client
    .ui - all client code

## Status

Accepted.

## Consequences

