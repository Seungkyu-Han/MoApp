package com.example.moapp.controller

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ApiRequestTask(private val callback: ApiCallback) {

    fun execute(urlString: String, headers: Map<String, String> = emptyMap()) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = makeApiCall(urlString, headers)
                withContext(Dispatchers.Main) {
                    callback.onSuccess(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Error occurred during API request.")
                }
            }
        }
    }

    private suspend fun makeApiCall(urlString: String, headers: Map<String, String>): String? {
        return try {
            val url = URL(urlString)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

            try {
                // 추가된 부분: 헤더 추가
                for ((key, value) in headers) {
                    urlConnection.setRequestProperty(key, value)
                }

                val bufferedReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                bufferedReader.use { it.readText() }
            } finally {
                urlConnection.disconnect()
            }
        } catch (e: Exception) {
            Log.e("ApiRequestTask", "Error", e)
            null
        }
    }
}

interface ApiCallback {
    fun onSuccess(response: String?)
    fun onError(error: String)
}

