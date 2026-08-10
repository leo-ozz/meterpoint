# meterpoint

**A charge point operator backend that answers a question people actually have: when is the cheapest and cleanest time to charge?**

The system records EV charging sessions, enriches each one with the wholesale electricity price and grid carbon intensity at the moment it happened, and serves that back as cost, emissions, and a recommended charging window.

Internally the project is called `meterpoint`. `gridweather` is the public-facing name.

> **Status:** in progress. Infrastructure, deployment pipeline and service skeleton are live. The domain model, data pipeline and warehouse are being built — see [Roadmap](#roadmap).

---

## Why this exists

Two things are true about charging an EV in Germany: the wholesale price varies by a factor of five across a day and regularly goes negative, and the carbon intensity of the grid varies by a factor of four depending on wind and solar output. Those two curves do not peak at the same time.

So "charge overnight" is not actually good advice. The interesting question is which quarter-hour windows are cheap, which are clean, and what it costs you to prefer one over the other.

Answering that requires a transactional system that records sessions reliably, a pipeline that ingests and corrects market data, and a warehouse that joins them at the right grain. That is what this repository is.

---

## Data

**Market data is real.** Day-ahead prices, actual generation per production type, and total load for the DE_LU bidding zone, from the [ENTSO-E Transparency Platform](https://transparency.entsoe.eu/). Quarter-hourly resolution since the SDAC 15-minute market time unit went live on 1 October 2025.

**Transactional data is simulated.** A generator drives the producer service as if real charge points and drivers were using it. It deliberately produces the mess a real fleet produces: out-of-order meter readings, duplicates, clock skew between charge points, sessions that never receive a stop event, hardware faults mid-session, and sessions crossing midnight and DST boundaries.

Carbon intensity is derived here from the generation mix and per-source emission factors, rather than taken from an API that supplies the figure.

---

## Architecture

Two independent ingestion paths that meet only in the warehouse.

```
Simulator                    ENTSO-E API
    │                             │
    ▼                             ▼
Producer Service            Ingestion Job
(Spring Boot,               (Python, Parquet)
 outbox pattern)                  │
    │                             │
    ▼                             │
Outbox Poller                     │
    │                             │
    └──────────┬──────────────────┘
               ▼
      Warehouse + dbt Core
               │
               ▼
         Serving API
```

The producer service knows nothing about electricity prices. The ingestion job knows nothing about charging sessions. Neither depends on the other being available.

**Why the join is deferred to the warehouse rather than done at write time:**

- *Availability* — enriching a session with a live price call would couple charging to a third-party API's uptime.
- *Correctability* — actual generation figures are revised days after publication. A price frozen into a transactional row cannot be corrected without mutating transactional data.
- *Timing* — a session finishes before the generation data describing it exists.

### The interesting modelling problem

A charging session spans multiple 15-minute market intervals, partially overlapping the first and last. Allocating its energy across those intervals — to compute true cost and emissions — is a real problem with defensible alternatives.

This project uses **metered allocation**: cumulative meter readings are interpolated to interval boundaries and differenced. The simpler flat-by-duration split is wrong whenever charging power varies, which is always, since EVs taper as the battery fills.

That decision produces the metric the project is built around: **price capture ratio**, the energy-weighted price actually paid divided by the period average. Below 1.0 means the session charged in cheaper-than-average intervals.

---

## Stack

| Layer | Choice |
|---|---|
| Producer service | Java 21, Spring Boot, transactional outbox |
| Pipeline | Python, Polars, Parquet |
| Warehouse | PostgreSQL, dbt Core (Kimball, SCD Type 2) |
| Object storage | Garage (S3-compatible) |
| Edge | Caddy — automatic HTTPS |
| Infrastructure | OpenTofu, Hetzner Cloud, cloud-init |
| CI/CD | GitHub Actions → GHCR → SSH |
| Observability | Prometheus, Grafana |

---

## Running costs

Single node, everything containerised.

| Item | € / month |
|---|---|
| Hetzner CX33 (4 vCPU, 8 GB) | 10.10 |
| Volume, 10 GB | ~0.50 |
| Primary IPv4 | ~0.60 |
| Storage Box (backups) | ~4.00 |
| Domain | ~1.00 |
| **Total** | **~16** |

Hetzner raised cloud prices twice in 2026 — the CPX line by roughly 2.5×, the CX and CAX lines far less. The choice of CX over the more commonly recommended CPX is documented in [ADR-0002](docs/decisions/).

---

## Repository layout

```
infra/              OpenTofu: server, firewall, volume, cloud-init
services/producer/  Spring Boot service
pipelines/          Outbox poller, ENTSO-E ingestion
warehouse/          dbt project
simulator/          Traffic generator (throwaway by design)
deploy/             Compose stack and Caddy config
docs/decisions/     Architecture decision records
```

---

## Operating

Deployment is push-based. A merge to `main` runs tests, builds an image tagged with the commit SHA, pushes it to GHCR, syncs `deploy/` to the server, and restarts the stack.

Images are tagged by commit SHA rather than a mutable tag, so the running version is always identifiable and rollback is a one-line change to `.env` followed by `docker compose up -d`.

Persistent state — Postgres data and the Parquet landing zone — lives on a Hetzner Volume with an independent lifecycle. The server can be replaced without touching it.

**Local development** requires Docker and JDK 21:

```bash
cd services/producer && ./gradlew build
docker compose -f deploy/compose.yaml up -d
```

---

## Design decisions

Recorded as ADRs in [`docs/decisions/`](docs/decisions/). Among them: single-node deployment with state externalised to a volume; Hetzner CX over CPX; Caddy over nginx plus certbot; deferring price enrichment to the warehouse; metered rather than flat energy allocation.

Each records what was chosen, what was rejected, what the consequences are, and the condition under which the decision should be revisited.

---

## Roadmap

- [x] Infrastructure as code, reproducible from scratch
- [x] Deployment pipeline, HTTPS, persistent volume
- [ ] Producer service: domain model, state machine, transactional outbox
- [ ] Outbox poller writing partitioned Parquet
- [ ] ENTSO-E ingestion: incremental, idempotent, restatement-aware
- [ ] dbt models: documented grain, SCD Type 2, incremental facts
- [ ] Monitoring, alerting, and a documented incident
- [ ] Serving API with the optimal-window endpoint

---

## Licence

MIT# meterpoint
