---
title: Anchor architectural redesign against 12 Forgejo review issues
date: "2026-10-03"
track: knowledge
category: decisions
applies_when: Planning architectural rebuild and selecting tasks
decision_status: accepted
---

## Context
The repository was reviewed via a dual-agent stoic pipeline and synchronized to self-hosted Forgejo (nicholas/qtr-qth on athena-forge) with 12 discrete backlog issues.

## Strategy
Rather than refactoring in a vacuum, use the concrete issues (#1 through #12) as practical filters to guide and stress-test the architectural redesign:
1. DAG modularity: Break serial <-> util cycle and extract CLI.
2. Security & boundaries: Enforce canonical path containment and parameterized ProcessBuilder.
3. Temporal precision: Fix midnight UTC day-boundary rollover and leap second handling.
4. Functional performance: Eliminate Map.of heap thrashing in hot byte loops; complete Vavr Option/Try migration.
5. Testing purity: Convert scattered assertions to strict Given-When-Then BDD Fixtures.
