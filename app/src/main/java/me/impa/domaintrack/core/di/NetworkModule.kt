/*
 * Copyright (c) 2026 Alexander Yaburov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.impa.domaintrack.core.di

import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Qualifier
import jakarta.inject.Singleton
import me.impa.domaintrack.core.data.remote.api.IanaBootstrapApi
import me.impa.domaintrack.core.data.remote.api.RdapApi
import me.impa.domaintrack.core.data.remote.dto.VCardArrayDto
import me.impa.domaintrack.core.data.remote.serialize.VCardArrayDeserializer
import me.impa.domaintrack.core.util.isOnline
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.util.concurrent.TimeUnit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BasicHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CachedHttpClient

private const val HTTP_CACHE_SIZE = 10L * 1024 * 1024
private const val MAX_AGE = 5 * 60 // 5 minutes
private const val MAX_STALE = 60 * 60 * 24 * 30 // 30 days

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    @BasicHttpClient
    fun provideBasicHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(10, TimeUnit.SECONDS)
        writeTimeout(10, TimeUnit.SECONDS)
        readTimeout(10, TimeUnit.SECONDS)

    }
        .build()

    @Singleton
    @Provides
    @CachedHttpClient
    fun provideCachedHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, HTTP_CACHE_SIZE)

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val onlineInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            response.newBuilder()
                .header("Cache-Control", "public, max-age=${MAX_AGE}")
                .removeHeader("Pragma")
                .build()
        }

        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!connectivityManager.isOnline()) {
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=${MAX_STALE}")
                    .removeHeader("Pragma")
                    .build()
            }
            chain.proceed(request)
        }

        return OkHttpClient.Builder().apply {
            cache(cache)
            connectTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            addNetworkInterceptor(onlineInterceptor)
            addInterceptor(offlineInterceptor)
        }.build()
    }

    @Singleton
    @Provides
    fun providesIanaBootstrapService(@CachedHttpClient client: OkHttpClient): IanaBootstrapApi =
        Retrofit.Builder()
            .baseUrl("https://data.iana.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create<IanaBootstrapApi>()

    @Singleton
    @Provides
    fun providesRdapService(@BasicHttpClient client: OkHttpClient): RdapApi =
        Retrofit.Builder()
            .baseUrl("https://dummy_RDAP/") // It's by design.
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(VCardArrayDto::class.java, VCardArrayDeserializer())
                        .create()
                )
            )
            .build()
            .create<RdapApi>()

}