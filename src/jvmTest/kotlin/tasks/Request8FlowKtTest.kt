package tasks

import contributors.MockGithubService
import contributors.concurrentProgressResults
import contributors.testRequestData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Request8FlowKtTest {
    @Test
    fun testChannels() = runTest {
        val startTime = currentTime
        var index = 0
        val flow = loadContributorsFlow(MockGithubService, testRequestData)
        flow.collect { userResults ->
            val expected = concurrentProgressResults[index++]
            val time = currentTime - startTime
            Assert.assertEquals("Expected intermediate result after virtual ${expected.timeFromStart} ms:",
                expected.timeFromStart, time)
            Assert.assertEquals("Wrong intermediate result after $time:", expected.users, userResults.users)

        }
    }
}
