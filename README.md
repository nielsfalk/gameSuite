# Gamesuite

Gamesuite is a multi-module Kotlin game platform.

Play it at https://nielsfalk.github.io/gameSuite/

## Disclaimer

No one has ever read this code. It is completely vibecoded: written by AI agents, accepted when the tests passed, and only ever checked by clicking around in the app. Use it at your own risk. It comes with no warranty of any kind, express or implied, including but not limited to correctness, fitness for a particular purpose, or suitability for any purpose.

## Architecture & Modules

- **`shared`**: Game logic, data models, sealed game stage definitions (`de.nielsfalk.game.suite.$gamename` / `de.nielsfalk.game.suite.common`):
  - A common game interface describing every game: name, display name, ordered stages, next stage and last stage
  - Deterministic level seeds derived from game name, stage and level number
- **`server`**: Ktor backend with PostgreSQL database for score/highscore CRUD APIs:
  - Score route: `/{username}/{gameName}/{stage}/{level}`
  - Level highscores: `/highscores/{gameName}/{stage}/{level}`
  - User summary: `/summary/{username}`
  - Scores can be posted without authentication
  - Scores contain time and signed points
- **`pwa`**: Kotlin Compose HTML Progressive Web App (hosted on GitHub Pages):
  - Deep links: `/{username}/{gameName}/{stage}/{level}` (same segment order as the server)
  - Game route: `/{username}/{gameName}` shows the stage selection for one game
  - If no deep link but username in `localStorage`, forwards to `/{username}`
  - If no deep link and no username in `localStorage`, prompts for username
  - Packages: `de.nielsfalk.game.suite.$gamename`, `de.nielsfalk.game.suite.common`, or `de.nielsfalk.game.suite.navigation`
  - Game logo grid, per game+stage highscore summary and per-level highscore dialog
  - Local storage offline caching and resilient background server synchronization (does not fail when server is unreachable)

## Game Shell, Levels & Navigation

All games share one shell and one level model:

- **Levels**: every stage has levels numbered `1..∞`. The level number is hashed together with game name and stage into the seed of the random number generator, so a level number always reproduces the same board.
- **Deep links** carry the level number, which makes every level shareable and comparable.
- **Toolbar** (same for every game): `🏠 ‖ << < ↻ > >> ‖ 🏆` — home, 10 levels back, 1 level back, restart the current level, 1 level forward, 10 levels forward, and the highscore of the current level. `<<` and `<` clamp to level 1 and are disabled there; `↻` replays the identical board with a fresh timer. Game-specific actions (minesweeper flag mode, hexlaser hint) sit in a second row as icon buttons.
- **Home navigation** shows the game logos as inline SVG. The stage selection only appears after a logo was clicked. Under the logos a per game+stage summary for the user shows levels won / played, best points and total points with a link to continue at the highest level played.
- **Winning**: the winning animation plays first, then the highscore dialog for that level opens automatically with a "next level" action. The dialog lists the top 10 (points descending, time ascending), highlights the player's own best entry and shows total plays. When the server does not respond, the dialog still opens with locally stored scores and an offline hint — being offline is never an error.
- **Stage up**: three consecutive wins in a session open a stage-up dialog, once per stage and never on the last stage. The streak resets on a loss, on `↻` and on a stage change.
- **Server base URL** defaults to `https://gamesuite-server.onrender.com` and can be overridden with the `gamesuite_server_url` key in `localStorage` for local development.

## PWA Hosting: GitHub Pages

The PWA is deployed to GitHub Pages by `.github/workflows/pages.yml` on every push to `master`:

- The workflow runs `./gradlew :pwa:jsBrowserDistribution` and publishes `pwa/build/dist/js/productionExecutable`.
- It copies `index.html` to `404.html` (with absolute `/gameSuite/...` asset URLs), so deep links such as `/gameSuite/{username}/{stage}/{gameName}/{levelId}` load the app instead of a plain GitHub 404.
- The app derives its base path from the `gameSuite-pwa.js` script URL, so it works under the `/gameSuite/` subpath.

One-time setup: in the repository settings, open **Pages → Build and deployment** and set **Source** to **GitHub Actions**. The site is then available at https://nielsfalk.github.io/gameSuite/.

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

The deployed service is live at **https://gamesuite-server.onrender.com** (health check: `/health`).

To recreate it, either open **Blueprints → New Blueprint Instance** in the Render dashboard and select this repository (Render reads `render.yaml`), or open **New → Web Service**, choose the Docker runtime and set `DATABASE_URL` manually.

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
- [ ] [T23] Shared module: deterministic level seeds — `levelSeed(gameName, stage, level)` hashes game name, stage and level number (`1..∞`) into a stable seed, with data-driven reproducibility tests
- [ ] [T24] Shared module: common game interface (`name`, display name, ordered stages, `nextStage`, `isLastStage`) implemented for minesweeper, hexmine and hexlaser; `GameCatalog` validation built on top of it
- [ ] [T25] Shared/PWA/server: drop the minesweeper custom stage — only the presets easy, beginner and expert remain, custom parsing and the custom input fields are removed
- [ ] [T26] Navigation: unify deep links to `/{username}/{gameName}/{stage}/{level}` with numeric level numbers and add the game route `/{username}/{gameName}`; non-numeric legacy level ids fall back to level 1
- [ ] [T27] PWA module: inline SVG icon set in the common package (`<<`, `<`, `↻`, `>`, `>>`, home, trophy, flag, hint) rendered as reusable icon buttons
- [ ] [T28] PWA module: shared game shell with the icon toolbar (`🏠 ‖ << < ↻ > >> ‖ 🏆`), clamped and disabled level navigation at level 1, and a second row for game-specific icon buttons; the text buttons "New Game", "Next Grid", "Next Stage", "Retry" and "Back to Home" are removed
- [ ] [T29] PWA module: minesweeper, hexmine and hexlaser views use the shared game shell, display the level number, and `↻` restarts the identical board with a fresh timer, move count and points
- [ ] [T30] PWA module: inline SVG game logos and a logo grid on the user home; the stage selection becomes visible only after a game logo was clicked and lives on `/{username}/{gameName}`
- [ ] [T31] Server module: level highscore endpoint returns the top 10 ordered by points descending then time ascending, plus total plays for that level
- [ ] [T32] PWA module: per-level highscore dialog (rank, username, points, time, own best highlighted, total plays) opened by the trophy button, with local scores and an offline hint when the server does not respond
- [ ] [T33] PWA module: after a win the winning animation plays and the highscore dialog opens automatically with a "next level" action
- [ ] [T34] Shared/PWA modules: stage up after three consecutive wins in a session for every game — dialog shown once per stage, never on the last stage, streak reset on loss, on `↻` and on stage change
- [ ] [T35] Server module: `GET /summary/{username}` aggregates per game and stage the levels won, levels played, best points, total points and the highest level played
- [ ] [T36] PWA module: highscore summary for the user under the game logos from the summary endpoint, with a local-score fallback and a "continue at level N" link
- [ ] [T37] PWA module: server base URL defaults to `https://gamesuite-server.onrender.com` with a `gamesuite_server_url` localStorage override, and legacy local scores with non-numeric level ids are ignored in all summaries

## License

This project is licensed under the [MIT License](LICENSE).
