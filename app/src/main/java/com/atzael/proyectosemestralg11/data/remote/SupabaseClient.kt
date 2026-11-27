package com.atzael.proyectosemestralg11.data.remote

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale

object SupabaseClient {

    // Adaptador de Gson tolerante para Long/Milisegundos: admite tanto números como cadenas de texto "DD-MM-YYYY HH:mm"
    private val flexibleLongAdapter = object : TypeAdapter<Long>() {
        override fun write(out: JsonWriter, value: Long?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value)
            }
        }

        override fun read(reader: JsonReader): Long? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            if (reader.peek() == JsonToken.STRING) {
                val str = reader.nextString()
                return str.toLongOrNull() ?: runCatching {
                    val pattern = if (str.contains(":")) "dd-MM-yyyy HH:mm" else "dd-MM-yyyy"
                    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                    sdf.parse(str)?.time
                }.getOrNull() ?: 0L
            }
            if (reader.peek() == JsonToken.NUMBER) {
                return reader.nextLong()
            }
            reader.skipValue()
            return null
        }
    }

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Long::class.java, flexibleLongAdapter)
        .registerTypeAdapter(Long::class.javaObjectType, flexibleLongAdapter)
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestOriginal = chain.request()
            val requestConHeaders = requestOriginal.newBuilder()
                .header("apikey", SupabaseConfig.ANON_KEY)
                .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
                .header("Content-Type", "application/json")
                .build()
            chain.proceed(requestConHeaders)
        }
        .build()

    val api: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(SupabaseConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SupabaseApi::class.java)
    }
}
