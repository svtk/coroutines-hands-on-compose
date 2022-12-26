package ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import contributors.ContributorsViewModel
import contributors.User

@Composable
fun ContributorsListView(
    contributorsViewModel: ContributorsViewModel,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(top = 20.dp, bottom = 20.dp, end = 20.dp)) {
        Column {
            Row(/*modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp)*/) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .background(MaterialTheme.colors.primary)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,

                    ) {
                    Text(
                        style = MaterialTheme.typography.subtitle1,
                        text = "Login",
                        color = MaterialTheme.colors.onPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primary)
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        style = MaterialTheme.typography.subtitle1,
                        text = "Contributions",
                        color = MaterialTheme.colors.onPrimary
                    )
                }
            }
            val usersUIState by contributorsViewModel.contributorsStateFlow.collectAsState()
            Box {
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                ) {

                    items(
                        items = usersUIState,
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        UserView(it)
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = state)
                )
            }
        }
    }
}

@Composable
fun UserView(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0, 0, 0, 20))
            .padding(5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.5f).padding(start = 20.dp),
        ) {
            Text(
                text = user.login,
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${user.contributions}",
            )
        }
    }
}

