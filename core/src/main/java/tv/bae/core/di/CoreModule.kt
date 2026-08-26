package tv.bae.core.di

import androidx.room.Room
import org.koin.dsl.module
import tv.bae.core.data.local.AppDatabase
import tv.bae.core.data.remote.CatApi
import tv.bae.core.data.remote.createKtorClient
import tv.bae.core.data.repository.BreedRepositoryImpl
import tv.bae.core.domain.repository.BreedRepository
import tv.bae.core.domain.usecase.GetAverageLifespanUseCase
import tv.bae.core.domain.usecase.GetBreedDetailUseCase
import tv.bae.core.domain.usecase.GetFavouriteBreedsUseCase
import tv.bae.core.domain.usecase.ToggleFavouriteUseCase

val coreModule = module {
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "cats_db")
        .fallbackToDestructiveMigration(true)
        .build()
    }
    single { get<AppDatabase>().favouriteDao() }
    single { get<AppDatabase>().breedDao() }

    single { createKtorClient() }
    single { CatApi(get()) }

    single<BreedRepository> {
        BreedRepositoryImpl(get(), get(), get())
    }

    factory { GetBreedDetailUseCase(get()) }
    factory { GetFavouriteBreedsUseCase(get()) }
    factory { ToggleFavouriteUseCase(get()) }
    factory { GetAverageLifespanUseCase(get()) }
}
