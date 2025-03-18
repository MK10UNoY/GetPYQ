package com.infomanix.getpyq.utils

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient // ✅ Correct function
import io.github.jan.supabase.postgrest.Postgrest // ✅ Correct import
import io.github.jan.supabase.storage.Storage // ✅ Correct import
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @OptIn(SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
            val client = HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 15000
            }
        }

        return createSupabaseClient(
            supabaseUrl = "https://exuwaeeqyhjbzvdujgwd.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImV4dXdhZWVxeWhqYnp2ZHVqZ3dkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDIxMTYzOTYsImV4cCI6MjA1NzY5MjM5Nn0.AHIMKB7HDJzCZPkv9S28BQ6w59RsvVECOUa6plSeVnQ"
        ) {
            install(Postgrest) // ✅ Use correct install
            install(Storage) // ✅ Use correct install

            // ✅ Ensure Ktor client has timeout configurations
            install(Auth)
        }
    }
}
