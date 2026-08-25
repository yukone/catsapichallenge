# Challenge Cats API

App for TheCatAPI to browse breeds, handle favourites and show breed details.

## Strategies

Given the 4–6 hour timebox and the emphasis on code quality over completeness, we will focus on the core required flows first. The exception is the modular structure, which we set up from the start since refactoring it later would be far more costly. Work goes bottom-up — domain, then UI, then data — delivering one screen at a time.

## Architectural decisions

- MVVM with Clean Architecture and a multi-module layout (`app`, `core`, `designsystem`, `feature-*`).
- Jetpack Compose for the UI.
- Coroutines and Flow for async work and state handling.
- Koin for dependency injection.
- Ktor with kotlinx.serialization for networking.

## Trade-offs

- Ktor over Retrofit: pure Kotlin and KMP-ready, with a smaller ecosystem.
- Koin over Hilt: no annotation processing, but requires extra care without compile-time validation.
- Modular from the start: more upfront setup, avoids a large refactor later.
