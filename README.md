# Gamesuite

Gamesuite is a multi-module Kotlin game platform.

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

## Testing Strategy

- Tests use **Kotest FreeSpec** and **data-driven testing** (`withData` / `table` / `forAll`).
- **Power Asserts** compiler plugin enabled for rich assertion diagnostics.
- Smoke test coverage across all modules.

## Setup Tasks

- [x] [T01] Project initialization: MIT License, agents.md commit guidelines, gitignore
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

## License

This project is licensed under the [MIT License](LICENSE).
