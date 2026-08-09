package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SearchSource(val title: String, val url: String)

data class GroundedDevotionalResult(
    val devotionalText: String,
    val searchSources: List<SearchSource> = emptyList()
)

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isNull_OR_blank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Exception) {
            ""
        }
    }

    private fun String?.isNull_OR_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    /**
     * Complex Reasoning / Theological Exegesis using gemini-3.1-pro-preview
     */
    suspend fun getDeepReflection(verseText: String, reference: String, language: String = "Cebuano"): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is missing. Please add your key in AI Studio Secrets panel."))
        }

        val prompt = """
            Analyze the following Bible verse: "$verseText" ($reference).
            Please provide an in-depth spiritual reflection containing:
            1. Theological Meaning & Original Context
            2. Life Application for Today
            3. 3 Reflection Questions for personal meditation or group study.
            Format cleanly with clear headings and emojis.
            LANGUAGE REQUIREMENT: Respond entirely in fluent, natural $language.
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", "You are an expert biblical scholar and compassionate spiritual counselor. Always respond in fluent, natural $language."))
                })
            })
        }

        makeApiCall("gemini-3.1-pro-preview", jsonBody)
    }

    /**
     * General tasks - Prayer & Scripture Suggestion using gemini-3.5-flash
     */
    suspend fun getPrayerAndScripture(situation: String, language: String = "Cebuano"): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is missing."))
        }

        val prompt = """
            The user is experiencing or asking about: "$situation".
            Provide in fluent, natural $language:
            1. A warm, personal 3-sentence prayer in $language.
            2. 2-3 relevant Bible Verses with full text and verse reference.
            Return in format:
            PRAYER: <prayer text in $language>
            VERSES: <verses>
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
        }

        val result = makeApiCall("gemini-3.5-flash", jsonBody)
        result.map { raw ->
            val prayerPart = raw.substringAfter("PRAYER:", "").substringBefore("VERSES:", raw).trim()
            val versePart = raw.substringAfter("VERSES:", raw).trim()
            Pair(if (prayerPart.isEmpty()) raw else prayerPart, versePart)
        }
    }

    /**
     * Fast tasks - Quick Verse Summary using gemini-3.1-flash-lite-preview
     */
    suspend fun getQuickSummary(verseText: String, reference: String): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is missing."))
        }

        val prompt = "Provide a 2-sentence summary and core takeaway of $reference: \"$verseText\"."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
        }

        makeApiCall("gemini-3.1-flash-lite-preview", jsonBody)
    }

    /**
     * Search Grounding using gemini-3.5-flash with Google Search tool
     */
    suspend fun getSearchGroundedDevotional(topic: String, language: String = "Cebuano"): Result<GroundedDevotionalResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is missing."))
        }

        val prompt = """
            Search for positive, uplifting current world news or inspirational stories related to "$topic".
            Create a "Scripture & Today's World" daily devotional that connects these current events with relevant Bible verses.
            Provide:
            - Overview of recent inspirational context
            - Connecting Bible Verses
            - Prayer for today
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            })
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Call failed (${response.code}): $responseString"))
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No candidate returned from Gemini."))
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: "No text generated."

            val sources = mutableListOf<SearchSource>()
            val groundingMetadata = candidate.optJSONObject("groundingMetadata")
            if (groundingMetadata != null) {
                val searchChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (searchChunks != null) {
                    for (i in 0 until searchChunks.length()) {
                        val chunk = searchChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val title = web.optString("title", "Web Source")
                            val uri = web.optString("uri", "")
                            if (uri.isNotEmpty()) {
                                sources.add(SearchSource(title, uri))
                            }
                        }
                    }
                }
            }

            Result.success(GroundedDevotionalResult(text, sources.distinctBy { it.url }))
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error in search grounded devotional", e)
            Result.failure(e)
        }
    }

    /**
     * Voice / Live Conversation using gemini-3.5-flash
     */
    suspend fun sendLiveVoiceMessage(
        userVoiceText: String,
        history: List<Pair<String, String>>,
        language: String = "Cebuano"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(Exception("Gemini API key is missing."))
        }

        val contentsArray = JSONArray()
        // Append conversation history
        for (turn in history) {
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply { put(JSONObject().put("text", turn.first)) })
            })
            contentsArray.put(JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().apply { put(JSONObject().put("text", turn.second)) })
            })
        }
        // Append current prompt
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply { put(JSONObject().put("text", userVoiceText)) })
        })

        val jsonBody = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", "You are an empathetic, soft-spoken voice spiritual companion fluent in Philippine Cebuano (Bisaya), Tagalog/Filipino, and English. Respond in fluent, natural Philippine Cebuano (Bisaya) when spoken to in Cebuano or if Cebuano language is selected. Keep responses concise (2-4 sentences max), spoken style, encouraging, and anchored in Christian grace."))
                })
            })
        }

        makeApiCall("gemini-3.5-flash", jsonBody)
    }

    private fun makeApiCall(modelName: String, jsonBody: JSONObject): Result<String> {
        val apiKey = getApiKey()
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Result.failure(Exception("API error $modelName (${response.code}): $responseString"))
            } else {
                val jsonResponse = JSONObject(responseString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    Result.failure(Exception("Empty candidate array returned by $modelName."))
                } else {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                    if (text.isNotEmpty()) {
                        Result.success(text)
                    } else {
                        Result.failure(Exception("No text in candidate response."))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiRepository", "Error calling $modelName", e)
            Result.failure(e)
        }
    }
}
