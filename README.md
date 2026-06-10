# angelNCore

Core infrastructure plugin for the AngelNetwork ecosystem. Provides the shared event bus, service layer, database access, and economy backbone that all other Angel plugins depend on.

---

## Architecture

angelNCore is the **single required dependency** for every Angel plugin. All cross-plugin communication flows through its typed event bus — plugins publish events; other plugins subscribe. No direct plugin-to-plugin calls.

```
angelNCore (no deps)
├── EventBus           — typed Bukkit event system (publish/subscribe)
├── ServiceRegistry    — static service locator for 9 domain services
├── DatabaseManager    — centralized SQLite/MySQL data layer
├── EconomyManager     — player balances, deposits, withdrawals
├── MarketManager      — supply/demand price engine
├── BankService        — loans, interest, debt tracking
├── RegionService      — land claims and territory control
├── CompanyService     — company formation and governance
├── StockExchangeService — stock trading + HTTP API
├── NutritionService   — player diet tracking
├── MilitaryService    — war declarations, faction modifiers
├── LogisticsService   — trade route tracking
└── CrossListingService — cross-plugin item lookups
```

---

## Events (Cross-Plugin Communication)

All events extend `AngelNetworkEvent`. Published via `EventBus.publish()` — consumed by subscribing plugins with standard `@EventHandler`.

| Event | Publisher | Consumer(s) |
|---|---|---|
| `AuctionSaleEvent` | actualAuction | MarketService |
| `BountyCompletedEvent` | angelBounty | ReputationService, AuditService |
| `CompanyIPOEvent` | CompanyService | Stock Exchange API |
| `ContractBreachedEvent` | angelTrade | ReputationService, angelBounty |
| `DuelCompletedEvent` | angelDuel | ReputationService |
| `FactoryDamagedEvent` | angelCreate | MilitaryService, CompanyService |
| `FactoryRepairedEvent` | angelCreate | MilitaryService, CompanyService |
| `ItemProducedEvent` | angelCreate | MarketService, CompanyService |
| `LandClaimChangedEvent` | RegionService | CompanyService, MarketService |
| `PlayerDietChangedEvent` | angelSustenance | NutritionService, MilitaryService |
| `SeasonChangedEvent` | angelSeason | angelSustenance, MarketService, RegionService |
| `ShipmentInterceptedEvent` | angelTrade | MarketService, angelBounty |
| `TradeCompletedEvent` | angelTrade | MarketService, ReputationService |
| `WarDeclaredEvent` | angelNCore | MilitaryService, MarketService, Stock Exchange |
| `WarEndedEvent` | angelNCore | MilitaryService, MarketService |

---

## Commands

| Command | Description |
|---|---|
| `/shop` | Dynamic shop with supply/demand pricing |
| `/balance` | Check your balance |
| `/eco give|take|set` | Admin economy management |
| `/bank` | Banking interface (loans, deposits) |
| `/market` | Market price listings |
| `/stock` | Stock exchange trading |
| `/claim` | Land claiming |
| `/region` | Region management |
| `/war` | War declarations |
| `/menu` | Angular Hub GUI |
| `/tutorial` | New player onboarding |
| `/backup` | Admin database backup |

---

## Economy

Dynamic supply-and-demand pricing:
- Buying drives prices up, selling drives them down
- Prices decay toward base value over time
- Capped between configurable floor and ceiling
- All prices configured in `config.yml`

---

## Installation

1. Build with `./gradlew jar`, drop into `plugins/`
2. Start server — `config.yml` generates on first run
3. All other Angel plugins soft-depend on this plugin; build it first

---

## Building

Requires Java 21 and Gradle.

```bash
./gradlew jar
```

---

## Dependencies

- Paper API 1.21.11 — provided at runtime
- Vault API — compile-only, for economy bridge
- SQLite — bundled with Paper
