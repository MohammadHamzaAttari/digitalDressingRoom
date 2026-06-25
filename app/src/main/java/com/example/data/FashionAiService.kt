package com.example.data

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// Model requests and responses
@JsonClass(generateAdapter = true)
data class VirtualTryOnRequest(
    val image: String, // base64, data-URL, or https:// URL of the source image
    val prompt: String,
    val garment_type: String = "outfit", // "upper_body" | "lower_body" | "overall" | "outfit"
    val strength: Float = 0.65f,
    val steps: Int = 25,
    val guidance_scale: Float = 9.0f,
    val response_format: String = "json"
)

@JsonClass(generateAdapter = true)
data class AccessoryOverlayRequest(
    val image: String, // base64 / url
    val mask: String, // B&W mask
    val prompt: String,
    val accessory_type: String = "accessory", // "watch" | "shoes" | "bag" | "hair" | "makeup" | "jewelry" | "accessory"
    val strength: Float = 0.75f,
    val steps: Int = 25,
    val guidance_scale: Float = 8.5f,
    val response_format: String = "json"
)

@JsonClass(generateAdapter = true)
data class Img2ImgRequest(
    val image: String,
    val prompt: String,
    val strength: Float = 0.75f,
    val steps: Int = 20,
    val guidance_scale: Float = 7.5f,
    val response_format: String = "json"
)

@JsonClass(generateAdapter = true)
data class GenerateRequest(
    val prompt: String,
    val width: Int = 1024,
    val height: Int = 1536,
    val steps: Int = 8,
    val response_format: String = "json"
)

@JsonClass(generateAdapter = true)
data class FashionAiResponse(
    val ok: Boolean,
    val endpoint: String? = null,
    val model: String? = null,
    val mimeType: String? = null,
    val filename: String? = null,
    val base64: String? = null,
    val dataUrl: String? = null,
    val accessoryType: String? = null,
    val garmentType: String? = null,
    val strength: Float? = null,
    val steps: Int? = null,
    val guidanceScale: Float? = null,
    val prompt: String? = null,
    val error: String? = null
)

interface FashionAiApi {
    @POST("api/v1/virtual-tryon")
    @Headers("Content-Type: application/json")
    suspend fun virtualTryOn(@Body request: VirtualTryOnRequest): FashionAiResponse

    @POST("api/v1/accessory-overlay")
    @Headers("Content-Type: application/json")
    suspend fun accessoryOverlay(@Body request: AccessoryOverlayRequest): FashionAiResponse

    @POST("api/v1/img2img")
    @Headers("Content-Type: application/json")
    suspend fun imageToImage(@Body request: Img2ImgRequest): FashionAiResponse

    @POST("api/v1/generate")
    @Headers("Content-Type: application/json")
    suspend fun textToImage(@Body request: GenerateRequest): FashionAiResponse
}

object FashionAiClient {
    var baseUrl: String = "https://fastapi-jqyv.vercel.app/"
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            recreateService()
        }

    var apiKey: String = "9f4c2a7e8d1b3f6a5c0e7d2a9b4f1c8e"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("X-API-Key", apiKey)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = com.squareup.moshi.Moshi.Builder()
        .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
        .build()

    private var _apiService: FashionAiApi? = null

    val apiService: FashionAiApi
        get() {
            if (_apiService == null) {
                recreateService()
            }
            return _apiService!!
        }

    private fun recreateService() {
        _apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FashionAiApi::class.java)
    }
}
