# Keycloak Development Realm

This directory contains:

- realm import (`wms-realm.json`)
- custom login theme (`themes/`)

These resources are used only by the local Docker Compose development environment.

## Demo Accounts

`wms-realm.json` seeds the `wms` realm with the following accounts, each
with a **hardcoded password** defined in that file:

| Username | Role |
|---|---|
| `admin` | ADMIN |
| `jan.kowalski`, `anna.nowak`, `piotr.wisniewski`, `karolina.wojcik`, `michal.kaminski`, `zuzanna.lewandowska`, `tomasz.zielinski`, `natalia.szymanska` | EMPLOYEE |

These credentials are intended **only** for the local Docker Compose
development environment. They are not configurable via `.env` - they are
baked directly into the realm import.

**Before any non-local deployment** (staging, production, or any environment
reachable outside `localhost`):

- do not reuse this realm import as-is, or
- log in and change every account's password immediately after the realm is
  imported, or
- replace the `credentials` block for each user in `wms-realm.json` with your
  own values before import.

Note: this is separate from `KC_BOOTSTRAP_ADMIN_USERNAME` /
`KC_BOOTSTRAP_ADMIN_PASSWORD` (the Keycloak *master realm* admin console
credentials), which are already supplied via `.env` and are not hardcoded.

## Production

`docker-compose.prod.yml` starts Keycloak with `start` only (no
`--import-realm`), so `wms-realm.json` and its demo users are **never**
imported in production, even though the directory is still mounted.

For production:

- create the realm, clients and users manually via the Keycloak admin
  console, or
- provision them from a private, non-committed realm export kept outside
  this repository.

Never commit a production realm export or production user credentials to
this repository.
