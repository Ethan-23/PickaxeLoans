# PickaxeLoans

A Paper plugin for Minecraft 1.21 that adds a player-driven **pickaxe lending market** to Cosmic Prisons. Players can list their pickaxes for loan, and other players can borrow them for a set duration — paying an upfront cost and ongoing taxes on what they earn while mining with the borrowed pickaxe. This was a suggestion I actually made a few weeks ago to help newer players get started. Players who have pickaxes sitting in their PVs collecting dust can loan them out for some reward, while newer players get a faster pickaxe to mine with.

## How it works

**Lenders** hold a pickaxe and run `/loan create` to open the listing menu, where they configure the deal:

- **Upfront cost** — a one-time fee paid in either money or energy
- **XP tax** — a percentage of the XP the borrower earns while mining that goes to the lender. (On cosmic this would be a physical xp bottle to drink tiered at the ores mined)
- **Energy tax** — a percentage of the energy the borrower earns that goes to the lender. (On cosmic this would account for extract fee)
- **Duration** — how long the borrower keeps the pickaxe before it's automatically returned

**Borrowers** run `/loan` to browse all listed pickaxes, preview the deal, and accept it. The pickaxe goes into their inventory tagged as a loaned item. While the loan is active:

- Mining rewards are split between borrower and lender according to the taxes on the deal
- The loaned pickaxe can't be dropped, stored in containers, or lost on death
- When the timer runs out, the pickaxe is returned to the lender automatically

Each loan moves through a simple lifecycle: `LISTED → BORROWED → RETURNED / EXPIRED / CANCELLED`.

## Commands

| Command | Description |
|---|---|
| `/loan` (alias `/loans`) | Open the loan listings menu |
| `/loan create` | List the pickaxe in your main hand for loan |

## Cosmic module

The plugin ships with a small self-contained prisons simulation (`cosmic` package) used to demo the loan system without needing a full prisons server:

- Mineable ores (coal through emerald) that grant **energy** and **experience** on break, then respawn on a timer
- Per-player stats tracked in memory and displayed on a live scoreboard
- Pickaxe tiers from wooden to diamond (Copper and Netherite were not included to match cosmic prisons so they are not loanable)

This module stands in for the host server's economy — in a real deployment the loan system would hook into the server's own energy/XP systems instead.

## Architecture

```
src/main/java/io/github/ethan23/pickaxeLoans/
├── commands/    # /loan command + tab completion
├── model/       # Loan, LoanDeal, ActiveLoan, LoanState, LoanResult
├── service/     # LoanService (business logic), LoanRepository (in-memory index)
├── database/    # LoanStorage interface + SQLite implementation
├── gui/         # Inventory GUI framework + loan menus
├── item/        # Pickaxe validation and persistent data keys
├── events/      # Loan-loss prevention (death, drop, container moves)
├── task/        # Loan expiry tick + dirty-loan write-behind flusher
├── util/        # Item/Component builders
└── cosmic/      # Demo prisons simulation (ores, energy, scoreboard)
```

Key design points:

- **Service layer is Bukkit-free where possible** — `LoanService` and `LoanRepository` contain the loan rules and are covered by JUnit tests
- **SQLite persistence with write-behind** — loans are held in memory for fast access; changes are marked dirty and flushed to `loans.db` by a background task and on shutdown
- **Loans survive restarts** — active loans and listings are loaded from storage on startup

## Planning & design process

The system was designed up front on an [Eraser planning board](https://app.eraser.io/workspace/mpDEqfLbYgSNDXivKuPq?origin=share) before any code was written. The board holds:

- **Data model sketch** — the `Loan`, `ActiveLoan`, `LoanState`, and `CostType` shapes, essentially as they exist in code today
- **In-memory index design** — the exact structures now in `LoanRepository`: a `lenderToLoans` map (lender → their loan ids), a `loans` map (id → loan), a `borrowerToLoan` map (one active loan per borrower), and two min-heaps ordered by listing expiration and loan end time, so the periodic update tick only ever inspects the top of each heap instead of scanning all loans
- **Flowcharts for every player flow** — create listing (GUI state machine with value editing and validation), browse/paginate listings, borrow preview + accept (availability and affordability checks), return/end loan, and the lender's active-loans menu (collect taxes, reclaim expired/cancelled pickaxes, shift-click to cancel)
- **Event flows** — block break (tax accrual), item drop blocking, death handling, join reconciliation, and the update tick
- **Repository operation flowcharts** — create / borrow / cancel / return / expire, each with its `LoanResult` outcomes (`NOT_FOUND`, `NOT_LISTED`, `ALREADY_BORROWING`, `DUPLICATE_LOAN`, …)

Notable places where the implementation evolved past the plan:

- The deal terms (cost type, upfront cost, taxes, duration) were extracted from `Loan` into a dedicated `LoanDeal` value object
- SQLite persistence was added — the board designed a purely in-memory system
- A `LENDERS_LOAN` result was added to stop lenders borrowing their own pickaxe
- The planned "pickaxe level 20+" and whitescroll listing requirements were simplified to a pickaxe-type check

## Building & running

Requires **Java 21**.

```bash
# Build the plugin jar (output in build/libs/)
./gradlew build

# Run the tests
./gradlew test

# Spin up a local Paper 1.21 test server with the plugin installed
./gradlew runServer
```

## Tech stack

- [Paper API](https://papermc.io/) 1.21.11
- SQLite (via `sqlite-jdbc`, loaded as a plugin library)
- Gradle with the [run-paper](https://github.com/jpenilla/run-task) plugin for local testing
- JUnit 5
