package tasks

import contributors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

data class UserResults(
    val users: List<User> = listOf(),
    val completed: Boolean = false,
)

// Version without scope parameter
// Looks fragile:
// - putting `coroutineScope` around `for (repo in repos)` makes it hang
// - putting `flow` around the last section `repeat(repos.size)` makes it hang
suspend fun loadContributorsFlow(
    service: GitHubService,
    req: RequestData,
): Flow<UserResults> = flow {
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        val channel = Channel<List<User>>()
        for (repo in repos) {
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }
        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            emit(UserResults(allUsers, it == repos.lastIndex))
        }
    }
}


// Version with scope parameter
suspend fun loadContributorsFlow(
    service: GitHubService,
    req: RequestData,
    scope: CoroutineScope
): Flow<UserResults> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val channel = Channel<List<User>>()
    for (repo in repos) {
        scope.launch {
            val users = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            channel.send(users)
        }
    }
    return flow {
        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            emit(UserResults(allUsers, it == repos.lastIndex))
        }
    }
}

// Experiments with state flow: it doesn't fit here,
// it's not infinite and should stop emitting values
suspend fun loadContributorsStateFlow(
    service: GitHubService,
    req: RequestData,
): StateFlow<UserResults> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val channel = Channel<List<User>>()
    for (repo in repos) {
        launch {
            val users = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            channel.send(users)
        }
    }
    val usersFlow = MutableStateFlow(UserResults())
    launch {
        var allUsers = emptyList<User>()
        repeat(repos.size) {
            val users = channel.receive()
            allUsers = (allUsers + users).aggregate()
            usersFlow.emit(UserResults(allUsers, it == repos.lastIndex))
        }
    }
    usersFlow.asStateFlow()
}

