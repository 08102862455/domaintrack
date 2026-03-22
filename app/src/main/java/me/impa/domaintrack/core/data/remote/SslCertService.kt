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

package me.impa.domaintrack.core.data.remote

import android.annotation.SuppressLint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.impa.domaintrack.core.data.remote.model.CertificateData
import me.impa.domaintrack.core.di.IoDispatcher
import timber.log.Timber
import java.net.IDN
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TIMEOUT = 3_000

@Singleton
class SslCertService @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
    object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val allHostsValid = HostnameVerifier { _, _ -> true }

    private val sslContext by lazy {
        SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }

    private fun extractField(data: String, field: String) =
        data.split(",")
            .find { it.trim().startsWith("$field=") }
            ?.substringAfter("$field=")
            ?.trim() ?: ""

    suspend fun query(domain: String, subdomain: String = ""): Result<CertificateData> {

        val asciiDomain = IDN.toASCII(
            (subdomain.takeIf { it.isNotEmpty() }?.plus(".") ?: "").plus(domain),
            IDN.ALLOW_UNASSIGNED
        )

        val url = URL("https://$asciiDomain/")
        return try {
            withContext(ioDispatcher) {
                val connection = (url.openConnection() as HttpsURLConnection).apply {
                    sslSocketFactory = sslContext.socketFactory
                    hostnameVerifier = allHostsValid
                    connectTimeout = TIMEOUT
                    readTimeout = TIMEOUT
                    connect()
                }
                try {
                    val certificates = connection.serverCertificates
                    require(certificates.isNotEmpty()) { "No certificates found" }
                    val cert = certificates.first() as X509Certificate
                    Result.success(
                        CertificateData(
                        id = 0,
                        domain = domain,
                        subdomain = subdomain,
                        issuer = extractField(cert.issuerDN.name, "O"),
                        country = extractField(cert.issuerDN.name, "C"),
                        notBefore = cert.notBefore.time,
                        notAfter = cert.notAfter.time
                    ))
                } finally {
                    connection.disconnect()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure(e)
        }

    }
}