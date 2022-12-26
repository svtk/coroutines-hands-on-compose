package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import contributors.ContributorsViewModel
import kotlinx.coroutines.cancel

@Composable
@Preview
fun App(contributorsViewModel: ContributorsViewModel) {
    MaterialTheme {
        Row {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LoadingDetailsView(
                    contributorsViewModel,
                )
                LoadingStatus(contributorsViewModel)
            }
            ContributorsListView(
                contributorsViewModel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

fun main() = application {
    val scope = rememberCoroutineScope()
    val contributorsViewModel = ContributorsViewModel(scope)
    Window(
        onCloseRequest = {
            contributorsViewModel.saveParams()
            scope.cancel()
            exitApplication()
        },
        title = "GitHub Contributors",
        state = rememberWindowState(width = 900.dp, height = 600.dp)
    ) {
        App(contributorsViewModel)
    }
}
