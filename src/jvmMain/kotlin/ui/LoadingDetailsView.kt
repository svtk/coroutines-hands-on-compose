package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import contributors.ContributorsViewModel
import variant.contributors.Variant

@Composable
fun LoadingDetailsView(
    contributorsViewModel: ContributorsViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Card(
            modifier = modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
        ) {
            Column(
                modifier = modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LabeledElement("GitHub Username") {
                    OutlinedTextField(
                        value = contributorsViewModel.username,
                        onValueChange = { contributorsViewModel.username = it }
                    )
                }
                LabeledElement("Password/Token") {
                    OutlinedTextField(
                        value = contributorsViewModel.password,
                        onValueChange = { contributorsViewModel.password = it },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                }
                LabeledElement("Organization") {
                    OutlinedTextField(
                        value = contributorsViewModel.org,
                        onValueChange = { contributorsViewModel.org = it },
                    )
                }

                LabeledElement("Variant") {
                    VariantDropdown(contributorsViewModel)
                }
            }
        }
        Row {
            Box(
                modifier = Modifier.fillMaxWidth(0.5f),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = contributorsViewModel::startLoading,
                    enabled = contributorsViewModel.newLoadingEnabled,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 20.dp, end = 10.dp)
                ) {
                    Text("Load contributors")
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = contributorsViewModel::cancelLoading,
                    enabled = contributorsViewModel.cancellationEnabled,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 10.dp, end = 20.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun VariantDropdown(contributorsViewModel: ContributorsViewModel) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = contributorsViewModel.variant.name,
            onValueChange = {},
            readOnly = true,
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Variant.values().forEachIndexed { index, variant ->
                DropdownMenuItem(onClick = {
                    contributorsViewModel.variant = variant
                    expanded = false
                }) {
                    Text(text = variant.name)
                }
            }
        }
    }
}

@Composable
fun LabeledElement(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(top = 15.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                style = MaterialTheme.typography.subtitle1,
                text = label
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            content()
        }
    }
}
