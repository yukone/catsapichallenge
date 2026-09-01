# Challenge Cats API

App for TheCatAPI to browse breeds, handle favourites and show breed details.

## Strategies

I focused on the core required flows first. The exception was the modular structure, which I've set up from the start since refactoring it later would be far more costly. 
The work goes bottom-up: domain, then data, then UI, delivering one feature at a time.
I also knew that I would have to do a major refactor to use Paging3 + RemoteMediator for offline, but I wanted to ensure that, having limited time, I delivered what was required.
For testing (aside from unit/integration tests), I first used the emulator and then a personal phone to double-check the features.

## Architectural decisions

- MVVM with Clean Architecture and a multi-module layout (`app`, `core`, `designsystem`, `feature-*`).
- Jetpack Compose for the UI.
- Coroutines and Flow for async work and state handling.
- Koin for dependency injection, having each feature module declare its own Koin module, making it easier for testing and for adding/removing features from the app.
- Ktor with kotlinx.serialization for networking.
- Local DB (Room) as the single source of truth
- Local-only search with Material 3 `SearchBar` and 300ms debounce on breed name.
- Paging 3 + RemoteMediator for offline functionality.
- For the data summary (average lifespan of the all the favourite breeds), the higher value will be used to calculate the average.
- UI state handling: sealed interfaces (Loading/Success/Error/Empty) for favourites and breed detail screens. The breed list screen uses independent flows — PagingData for the list, reactive StateFlow<Set<String>> for favourite IDs, and a flatMapLatest for search. Shared design-system components render loading, error, and empty states.

## Trade-offs

- Ktor over Retrofit: pure Kotlin and KMP-ready, with a smaller ecosystem.
- Koin over Hilt: no annotation processing, but requires extra care without compile-time validation.
- Modular from the start: more upfront setup, avoids a large refactor later.
- Paging 3 over custom pagination: had to do a larger initial refactor (replaced manual getBreeds(page) + BreedRepository.getBreeds()), but provides infinite scroll, cache management, and background refresh.
- Turbine over manual Flow testing: adds a dependency, but it is better for testing flows. Sequential awaitItem() is less error-prone than manual collection.
- Local search over network search: works offline, no API cost, instant results. This limits the results to cached breeds.
- DisposableEffect over lifecycle observer for search cleanup: lifecycle observer caused visible flicker on back navigation (is triggered after RESUMED). DisposableEffect fires on composition exit, clearing state before the user sees it.
- Storing the apikey on a properties file: this keeps the api safe from being committed to version control, but does not protect it from being extracted through reverse engineering.

## Next steps
- Search can be improved with online fallback (when local results are empty, query the API and cache results).
- E2E tests.
- UX polish with loading skeleton (shimmer effect on cards), pull to refresh the breed list, offline indicator in the UI.

## Required project setup
- On the root of the project folder create a file named 'apikey.properties' (this should be excluded from version control)
- Inside this file write your apikey like "CAT_API_KEY=live_XXXXXX" (can be obtained for free on https://thecatapi.com/)

## Post-submission critical fixes (Applied after the initial submission on a new branch)
- Clean Architecture fixes:
  - BreedRepository was returning Flow<PagingData<BreedEntity>> instead of Flow<PagingData<Breed>>.
  - BreedListViewModel was using favouriteDao instead of a dedicated use case.
- Missing tests for GetAverageLifespanUseCase.
- Removed the unnecessary page calculation on the RemoteMediator. The key is embedded in the entity, so the extra getById was just a leftover defensive query. 