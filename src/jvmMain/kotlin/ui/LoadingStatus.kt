package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import contributors.ContributorsViewModel

@Composable
fun LoadingStatus(
    contributorsViewModel: ContributorsViewModel
) {
    val loadingStatus = contributorsViewModel.loadingStatus
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loadingStatus == ContributorsViewModel.LoadingStatus.NOT_STARTED ||
            loadingStatus == ContributorsViewModel.LoadingStatus.IN_PROGRESS
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(15.dp))
        }

        Card(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.padding(15.dp),
                contentAlignment = Alignment.Center,
            ) {
                val time = contributorsViewModel.currentLoadingTimeMillis
                val timeText = if (time == 0L) "" else "${(time / 1000)}.${time % 1000 / 100} sec"

                val text = "Loading status: " + when (loadingStatus) {
                    ContributorsViewModel.LoadingStatus.NOT_STARTED -> "not started"
                    ContributorsViewModel.LoadingStatus.COMPLETED -> "completed in $timeText"
                    ContributorsViewModel.LoadingStatus.IN_PROGRESS -> "in progress $timeText"
                    ContributorsViewModel.LoadingStatus.CANCELED -> "canceled"
                }
                Text(
                    style = MaterialTheme.typography.subtitle1,
                    text = text
                )
            }
        }
    }
}

@Composable
fun LoadingStatus1(
    contributorsViewModel: ContributorsViewModel
) {
    Card(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val time = contributorsViewModel.currentLoadingTimeMillis
            val timeText = if (time == 0L) "" else "${(time / 1000)}.${time % 1000 / 100} sec"

            val loadingStatus = contributorsViewModel.loadingStatus
            val text = when (loadingStatus) {
                ContributorsViewModel.LoadingStatus.NOT_STARTED -> "Start new loading"
                ContributorsViewModel.LoadingStatus.COMPLETED -> "Loading status: completed in $timeText"
                ContributorsViewModel.LoadingStatus.IN_PROGRESS -> "Loading status: in progress $timeText"
                ContributorsViewModel.LoadingStatus.CANCELED -> "Loading status: canceled"
            }
            Text(
                style = MaterialTheme.typography.subtitle1,
                text = text
            )
            if (loadingStatus == ContributorsViewModel.LoadingStatus.NOT_STARTED ||
                loadingStatus == ContributorsViewModel.LoadingStatus.IN_PROGRESS
            ) {
                Spacer(modifier = Modifier.height(15.dp))
                CircularProgressIndicator()
            }
        }
    }
}