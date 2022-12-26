package contributors

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import contributors.ContributorsViewModel.LoadingStatus.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tasks.*
import variant.contributors.Variant.*

class ContributorsViewModel(
    private val scope: CoroutineScope
) {
    var username by mutableStateOf("")

    var password by mutableStateOf("")

    var org by mutableStateOf("")

    var variant by mutableStateOf(BLOCKING)

    var newLoadingEnabled by mutableStateOf(true)
        private set
    var cancellationEnabled by mutableStateOf(false)
        private set

    enum class LoadingStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELED }
    var loadingStatus by mutableStateOf(NOT_STARTED)
        private set

    var currentLoadingTimeMillis by mutableStateOf(0L)
        private set

    private var loadingJob by mutableStateOf<Job?>(null)

    private val _contributorsStateFlow: MutableStateFlow<List<User>> = MutableStateFlow(listOf())
    val contributorsStateFlow: StateFlow<List<User>> get() = _contributorsStateFlow

    init {
        loadInitialParams()
    }

    // TODO 1. Loading icon + statuses
    // TODO 2. variants in the dropbox + tasks
    // TODO 3. Read the hands-on :)
    // TODO Next. Flow tutorial

    fun startLoading() {
        saveParams()

        val req = RequestData(username, password, org)
        clearResults()
        val service = createGitHubService(req.username, req.password)

        newLoadingEnabled = false
        if (variant.ordinal >= SUSPEND.ordinal) {
            cancellationEnabled = true
        }

        val startTime = System.currentTimeMillis()
        loadingJob = scope.launch {
            when (variant) {
                BLOCKING -> { // Blocking UI thread
                    val users = loadContributorsBlocking(service, req)
                    updateResults(users, startTime)
                }
                BACKGROUND -> { // Blocking a background thread
                    loadContributorsBackground(service, req) {
                        updateResults(it, startTime)
                    }
                }
                CALLBACKS -> { // Using callbacks
                    loadContributorsCallbacks(service, req) { users ->
                        updateResults(users, startTime)
                    }
                }
                SUSPEND -> { // Using coroutines
                    val users = loadContributorsSuspend(service, req)
                    updateResults(users, startTime)
                }
                CONCURRENT -> { // Performing requests concurrently
                    val users = loadContributorsConcurrent(service, req)
                    updateResults(users, startTime)
                }
                NOT_CANCELLABLE -> { // Performing requests in a non-cancellable way
                    val users = loadContributorsNotCancellable(service, req)
                    updateResults(users, startTime)
                }
                PROGRESS -> { // Showing progress
                    loadContributorsProgress(service, req) { users, completed ->
                        updateResults(users, startTime, completed)
                    }
                }
                CHANNELS -> {  // Performing requests concurrently and showing progress
                    loadContributorsChannels(service, req) { users, completed ->
                        updateResults(users, startTime, completed)
                    }
                }
                FLOW -> { // Returning results as Flow
                    // TODO leave one version
//                    val contributorsFlow = loadContributorsFlow(service, req, scope = this)
                    val contributorsFlow = loadContributorsFlow(service, req)
                    contributorsFlow.collect {
                        updateResults(it.users, startTime, it.completed)
                    }
                }
            }
        }
    }

    fun cancelLoading() {
        loadingJob?.cancel()
        loadingStatus = CANCELED
        newLoadingEnabled = true
        cancellationEnabled = false
        currentLoadingTimeMillis = 0
    }

    private fun clearResults() {
        _contributorsStateFlow.value = listOf()
        loadingStatus = IN_PROGRESS
        newLoadingEnabled = false
        cancellationEnabled = false
        currentLoadingTimeMillis = 0
    }

    private fun loadInitialParams() {
        val params = loadStoredParams()
        username = params.username
        password = params.password
        org = params.org
        variant = params.variant
    }

    fun saveParams() {
        if (username.isEmpty() && password.isEmpty()) {
            removeStoredParams()
        } else {
            saveParams(LoadingParams(username = username, password = password, org = org, variant = variant))
        }
    }

    private fun updateResults(
        users: List<User>,
        startTime: Long,
        completed: Boolean = true
    ) {
        loadingStatus = if (completed) COMPLETED else IN_PROGRESS
        currentLoadingTimeMillis = System.currentTimeMillis() - startTime
        _contributorsStateFlow.value = users
        if (completed) {
            newLoadingEnabled = true
            cancellationEnabled = false
        }
    }
}
