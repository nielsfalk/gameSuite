# Gamesuite

Gamesuite is a multi-module Kotlin game platform.

## Disclaimer

No one has ever read this code. It is completely vibecoded: written by AI agents, accepted when the tests passed, and only ever checked by clicking around in the app. Use it at your own risk. It comes with no warranty of any kind, express or implied, including but not limited to correctness, fitness for a particular purpose, or suitability for any purpose.

## Architecture & Modules

- **`shared`**: Game logic, data models, sealed game stage definitions (`de.nielsfalk.game.suite.$gamename` / `de.nielsfalk.game.suite.common`).
- **`server`**: Ktor backend with PostgreSQL database for score/highscore CRUD APIs:
  - Route: `/{username}/{gameName}/{stage}/{levelId}`
  - Scores can be posted without authentication
  - Scores contain time and signed points
- **`pwa`**: Kotlin Compose HTML Progressive Web App (hosted on GitHub Pages):
  - Deep links: `/{username}/{stage}/{gameName}/{levelId}`
  - If no deep link but username in `localStorage`, forwards to `/{username}`
  - If no deep link and no username in `localStorage`, prompts for username
  - Packages: `de.nielsfalk.game.suite.$gamename`, `de.nielsfalk.game.suite.common`, or `de.nielsfalk.game.suite.navigation`
  - Highscore summary display
  - Local storage offline caching and resilient background server synchronization (does not fail when server is unreachable)

## Server Hosting: Render + Neon

The server is deployed to **Render** (web service) and the database is **Neon** (serverless PostgreSQL). Both use free tiers, so hosting costs $0 for low traffic. The PWA is hosted separately on GitHub Pages, so the server only has to serve the JSON API with CORS.

### Neon (database)

- Project `my-first-project` (`cool-mode-61763762`), branch `production`, database `neondb`.
- Region: AWS Frankfurt (`aws-eu-central-1`) — must match the Render region (Frankfurt) to keep query latency low.
- The free plan gives 100 compute-hours/month, 1 GB storage and 5 GB egress, and never pauses or deletes the project. Compute scales to zero after 5 minutes idle and wakes on the next query.
- Locally, `neon init` wrote the connection strings to `.env.local` (git-ignored). `./gradlew :server:run` and `./gradlew :server:databaseManualTest` load that file; the regular `:server:test` task stays on in-memory H2.

### Render (server)

Create a Web Service from this repository:

- **Region:** Frankfurt (EU Central) — must match the Neon region.
- **Build command:** `./gradlew :server:installDist`
- **Start command:** `server/build/install/server/bin/server`
- **Environment:** `DATABASE_URL` = the Neon connection string (credentials embedded, `sslmode=require`). The server converts it to JDBC via `postgresUrlToJdbc`; alternatively set `DB_URL`, `DB_USER` and `DB_PASSWORD`.
- **Instance type:** Free. The service sleeps after ~15 minutes idle, and the first request afterwards takes 30–60 seconds (JVM startup). Background score sync in the PWA makes this acceptable.

The repository contains everything Render needs:

- `Dockerfile`: multi-stage build that runs `./gradlew :server:installDist` and starts the generated distribution (`/app/bin/server`).
- `render.yaml`: a Blueprint that creates the free `gamesuite-server` web service in Frankfurt with health check `/health`. The `DATABASE_URL` environment variable is marked `sync: false`, so Render asks for the Neon connection string on first setup.
- `Application.kt` reads the `PORT` environment variable (falling back to `8080`), as Render assigns the port at runtime.

To create the service, either open **Blueprints → New Blueprint Instance** in the Render dashboard and select this repository (Render reads `render.yaml`), or open **New → Web Service**, choose the Docker runtime and set `DATABASE_URL` manually.

## Testing Strategy

- Tests use **Kotest FreeSpec** and **data-driven testing** (`withData` / `table` / `forAll`).
- **Power Asserts** compiler plugin enabled for rich assertion diagnostics.
- Smoke test coverage across all modules.

## Setup Tasks

- [x] [T01] Project initialization: MIT License, agents.md mot guidelines, gitignore
- [x] [T02] Multi-module Gradle Kotlin DSL configuration & wrapper setup
- [x] [T03] Shared module: Game logic models, sealed stage classes, and Kotest FreeSpec data-driven smoketests
- [x] [T04] Server module: Ktor server with PostgreSQL support, score CRUD endpoints, and Kotest FreeSpec smoketests
- [x] [T05] PWA module: Compose HTML PWA with deep linking, local storage score caching/syncing, and Kotest FreeSpec smoketests
- [x] [T06] Smoke verification and end-to-end build test across all modules
- [x] [T07] Lollipop design: fat font, round edges, dark background, and popping colors for the PWA
- [x] [T08] Pre-push git hook: runs all tests across all modules and verifies that commit author and coauthors differ; installable via a Gradle task
- [x] [T09] Commit-msg git hook: fails a commit when the author appears as a coauthor or coauthors are not unique; a single Gradle task installs all hooks
- [x] [T10] Shared module: reusable `SquareGrid` with cells in the common package for grid-based games
- [x] [T11] Shared module: Minesweeper game with serializable stages — easy, beginner, and expert presets plus a custom stage with parameters (width, height, mine count)
- [x] [T12] Server module: always validate that gameName and stage exist before accepting score requests (unknown games/stages are rejected)
- [x] [T13] PWA module: Minesweeper gameplay with stage selection (easy, beginner, expert, custom) using the shared SquareGrid and stages
- [x] [T14] PWA module: UserHome logout button clears the stored username from localStorage (keeping local scores) and returns to the username prompt at `/`
- [x] [T15] Shared module: `Grid<T>` interface (width/height, bounds, get/set, neighbors) implemented by `SquareGrid` and a new `HexGrid` using odd-r offset coordinates (pointy-top hexes on a rectangular board)
- [x] [T16] PWA module: CSS confetti-burst winning animation (~2s, non-blocking, respects `prefers-reduced-motion`) when a Minesweeper game is cleared
- [x] [T17] PWA module: automatically flag all mines when a Minesweeper game is finished
- [x] [T18] Shared/PWA modules: `HexMine`, a Minesweeper variant on a hexagonal grid using the shared `HexGrid`
- [x] [T19] PWA module: navigation fixes (deployment base path, browser back/forward, preserved game/stage selection)
- [x] [T20] Shared/PWA/server: port hexlaser into `de.nielsfalk.game.suite.hexlaser`, rename levels to stages, seed stage generation with a deterministic default, add the HTML5 canvas PWA view and deep links, and register/score `hexlaser`
- [x] [T21] Hosting: document Render + Neon deployment, wire Neon connection strings into the server via `DATABASE_URL`, and add an opt-in `DatabaseManualTest` for the real database

## License

This project is licensed under the [MIT License](LICENSE).
