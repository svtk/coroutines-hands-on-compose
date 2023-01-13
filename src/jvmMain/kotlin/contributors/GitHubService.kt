package contributors

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*

interface GitHubService {
    @GET("orgs/{org}/repos?per_page=100")
    fun getOrgReposCall(
        @Path("org") org: String
    ): Call<List<Repo>>

    @GET("repos/{owner}/{repo}/contributors?per_page=100")
    fun getRepoContributorsCall(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Call<List<User>>

    @GET("orgs/{org}/repos?per_page=100")
    suspend fun getOrgRepos(
        @Path("org") org: String,
    ): Response<List<Repo>>

    @GET("repos/{owner}/{repo}/contributors?per_page=100")
    suspend fun getRepoContributors(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
    ): Response<List<User>>
}


// TODO: A better example would have something here where this data is refreshed regularly and sent back.
// Then it really makes sense to have this be a flow, because it's a stream of values over time.
// Right now it's just a single value, but still serves to demonstrate how you can get rid of the `suspend`
// Modifier in your function signature.
// Can't have this one inside the interface, otherwise retrofit gets mad, so extension function it is
fun GitHubService.getOrgReposFlow(org: String): Flow<Response<List<Repo>>> {
    return flow {
        val repos = getOrgRepos(org)
        emit(repos)
    }
}

@Serializable
data class Repo(
    val id: Long,
    val name: String,
)

@Serializable
data class User(
    val login: String,
    val contributions: Int,
)

@Serializable
data class RequestData(
    val username: String,
    val password: String,
    val org: String
)

@OptIn(ExperimentalSerializationApi::class)
fun createGitHubService(username: String, password: String): GitHubService {
    val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)
    val httpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", authToken)
            val request = builder.build()
            chain.proceed(request)
        }
        .build()

    val contentType = "application/json".toMediaType()
    val json = Json { ignoreUnknownKeys = true }
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com")
        .addConverterFactory(json.asConverterFactory(contentType))
        .client(httpClient)
        .build()
    return retrofit.create(GitHubService::class.java)
}
