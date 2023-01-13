package tasks

import contributors.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserResults(
    val users: List<User> = listOf(),
    val completed: Boolean = false,
)

suspend fun loadContributorsSequentialFlow(
    service: GitHubService,
    req: RequestData,
): Flow<List<User>> = coroutineScope {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    flow {
        for (repo in repos) {
            val users = service.getRepoContributors(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            emit(users)
        }
    }.runningReduce { accumulator, value ->
        (accumulator + value).aggregate()
    }
}

suspend fun loadContributorsChannelFlow(
    service: GitHubService,
    req: RequestData,
): Flow<List<User>> {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()
    return channelFlow {
        for (repo in repos) {
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                send(users)
            }
        }
    }.runningReduce { accumulator, value ->
        (accumulator + value).aggregate()
    }
}

// Version without scope parameter
// Potential traps:
// - Channel needs to be UNLIMITED, otherwise this implementation will hang
suspend fun loadContributorsFlow(
    service: GitHubService,
    req: RequestData,
): Flow<UserResults> =
    coroutineScope {
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()

        val channel = Channel<List<User>>(UNLIMITED)
        for (repo in repos) {
            launch {
                val users = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(users)
            }
        }
        flow {
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
    scope: CoroutineScope,
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

fun loadContributorsChannelFlowNonSuspend(
    service: GitHubService,
    req: RequestData,
): Flow<List<User>> {
    val repoResponses = service.getOrgReposFlow(req.org)
    // onEach may seem spooky for an API that only returns a single value,
    // but actually we're doing the legwork to build a function that also works if the API returned
    // a stream of these values over time (i.e. if there is an interval at which the information
    // inside `repoResponses` is refreshed.)
    // This whole function can now react to changes emitted from the service!
    val repos = repoResponses.onEach {
        logRepos(req, it)
    }.map {
        it.bodyList()
    }

    return channelFlow {
        repos.onEach { repos ->
            for (repo in repos) {
                launch {
                    val users = service.getRepoContributors(req.org, repo.name)
                        .also { logUsers(repo, it) }
                        .bodyList()
                    send(users)
                }
            }
        }.collect()
    }.runningReduce { accumulator, value ->
        (accumulator + value).aggregate()
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

