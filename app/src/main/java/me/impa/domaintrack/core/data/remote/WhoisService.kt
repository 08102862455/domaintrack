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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import me.impa.domaintrack.core.data.remote.model.DatePattern
import me.impa.domaintrack.core.data.remote.model.DomainData
import me.impa.domaintrack.core.data.remote.model.toMs
import me.impa.domaintrack.core.di.IoDispatcher
import me.impa.domaintrack.core.util.extractTld
import me.impa.domaintrack.core.util.isIpAddress
import timber.log.Timber
import java.io.IOException
import java.net.IDN
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

private const val WHOIS_ROOT = "whois.iana.org"
private const val WHOIS_PORT = 43
private const val REQUEST_TIMEOUT = 10000

@Singleton
class WhoisService @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val regexRegistrar = listOf(
        "registrar:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registrar:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Sponsoring Registrar Organization:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registered through:\\s?(P<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registrar Name[.]*:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Record maintained by:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registration Service Provided By:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registrar of Record:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain Registrar :\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registration Service Provider: (?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "\\tName:\\t\\s(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registrar\\n\\s+Organization:\\s+(?<val>[^\\n]+)\\n".toRegex()
    )

    private val regexCreation = listOf(
        "\\[Created on]\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Created on[.]*: [a-zA-Z]+, (?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Creation Date:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Creation date\\s*:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registration Date:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Created Date:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Created on:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Created on\\s?[.]*:\\s?(?<val>.+)\\.$".toRegex(RegexOption.MULTILINE),
        "Date Registered\\s?[.]*:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain Created\\s?[.]*:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain registered\\s?[.]*:\\s?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain record activated\\s?[.]*:\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Record created on\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Record created\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Created\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registered on\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Registered\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain Create Date\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain Registration Date\\s?[.]*:?\\s*?(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "created:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "\\[Registered Date]\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "created-date:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "Domain Name Commencement Date: (?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "registered:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE),
        "registration:\\s*(?<val>.+)$".toRegex(RegexOption.MULTILINE)
    )

    private val regexExpiration = listOf(
        "\\[Expires on]\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Registrar Registration Expiration Date: *(?<val>.+)-[0-9]{4}".toRegex(RegexOption.MULTILINE),
        "Expires on[.]*: [a-zA-Z]+, (?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expiration Date:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expiration date\\s*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expires on:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expires on\\s?[.]*:\\s?(?<val>.+)\\.".toRegex(RegexOption.MULTILINE),
        "Exp(?:iry)? Date\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expiry\\s*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Domain Currently Expires\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record will expire on\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Domain expires\\s?[.]*:\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record expires on\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record expires\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expires\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expire Date\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Expired\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Domain Expiration Date\\s?[.]*:?\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "paid-till:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "expiration_date:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "expire-date:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "renewal:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "expire:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE)
    )

    private val regexUpdated = listOf(
        "\\[Last Updated]\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record modified on[.]*: (?<val>.+) [a-zA-Z]+".toRegex(RegexOption.MULTILINE),
        "Record last updated on[.]*: [a-zA-Z]+, (?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Updated Date:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Updated date\\s*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record last updated on\\s?[.]*:?\\s?(?<val>.+)\\.".toRegex(RegexOption.MULTILINE),
        "Domain record last updated\\s?[.]*:\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Domain Last Updated\\s?[.]*:\\s*?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Last updated on:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Date Modified\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Last Modified\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Domain Last Updated Date\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Record last updated\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Modified\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "[Cc]hanged:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "last_update:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Last Update\\s?[.]*:\\s?(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "Last updated on (?<val>.+) [a-z]{3,4}".toRegex(RegexOption.MULTILINE),
        "Last updated:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "last-updated:\\s*(?<val>.+)".toRegex(RegexOption.MULTILINE),
        "\\[Last Update]\\s*(?<val>.+) \\([A-Z]+\\)".toRegex(RegexOption.MULTILINE),
        "Last update of whois database:\\s?[a-z]{3}, (?<val>.+) [a-z]{3,4}".toRegex(RegexOption.MULTILINE)
    )

    private fun matchMultiple(text: String, regexList: List<Regex>): MatchResult? {
        regexList.forEach { regex ->
            regex.find(text)?.let {
                return it
            }
        }
        return null
    }

    private fun getRegistrar(whoisText: String): String {
        return matchMultiple(whoisText, regexRegistrar)?.groupValues[1] ?: ""
    }

    private fun getCreationDate(whoisText: String): String {
        return matchMultiple(whoisText, regexCreation)?.groupValues[1] ?: ""
    }

    private fun getExpirationDate(whoisText: String): String {
        return matchMultiple(whoisText, regexExpiration)?.groupValues[1] ?: ""
    }

    private fun getUpdatedDate(whoisText: String): String {
        return matchMultiple(whoisText, regexUpdated)?.groupValues[1] ?: ""
    }

    private fun findReferralServer(whoisText: String): String? {
        val patterns = listOf(
            "whois:[ \\t]*([\\w.-]+)".toRegex(RegexOption.IGNORE_CASE),
            "refer:[ \\t]*([\\w.-]+)".toRegex(RegexOption.IGNORE_CASE)
        )

        patterns.forEach { pattern ->
            pattern.find(whoisText)?.let {
                return it.groupValues[1]
            }
        }
        return null
    }

    @Throws(IOException::class)
    private fun queryWhoisServer(target: String, server: String): String {

        return Socket(server, WHOIS_PORT).use { socket ->
            socket.soTimeout = REQUEST_TIMEOUT
            socket.getOutputStream().bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                Timber.d("Query: $target")
                writer.write("$target\r\n")
                writer.flush()
                socket.getInputStream().bufferedReader(StandardCharsets.UTF_8).use { reader ->
                    val response = reader.readText()
                    return@use response
                }
            }
        }
    }

    private suspend fun getWhoisText(domain: String): String {

        require(!withContext(ioDispatcher) { isIpAddress(domain) }) {
            "Invalid domain name"
        }

        val tld = extractTld(domain)

        // Cache in Room?

        var server = findReferralServer(queryWhoisServer(tld, WHOIS_ROOT))
            ?: throw IOException("No referral server found")

        var whoisText: String

        while (true) {
            whoisText = queryWhoisServer(domain, server)
            findReferralServer(whoisText)?.also { server = it } ?: break
        }

        return whoisText
    }

    suspend fun query(domain: String): Result<DomainData> {
        val idnDomain = IDN.toASCII(domain, IDN.ALLOW_UNASSIGNED)

        val whoisText = try {
            withContext(ioDispatcher) { getWhoisText(idnDomain) }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        val result = DomainData(
            domain = domain,
            creationDate = DatePattern.parse(getCreationDate(whoisText)).toMs(),
            expirationDate = DatePattern.parse(getExpirationDate(whoisText)).toMs(),
            updatedDate = DatePattern.parse(getUpdatedDate(whoisText)).toMs(),
            registrar = getRegistrar(whoisText)
        )

        return if (result.registrar.isBlank()) Result.failure(Exception("No data found"))
        else Result.success(result)
    }
}