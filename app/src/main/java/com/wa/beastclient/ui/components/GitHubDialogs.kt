package com.wa.beastclient.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wa.beastclient.BuildConfig
import com.wa.beastclient.core.update.AppReleaseInfo
import com.wa.beastclient.core.update.UpdateUiState
import com.wa.beastclient.core.update.WuppUpdater
import com.wa.beastclient.ui.theme.LocalWaColors
import java.io.File
import java.net.URLEncoder

enum class GitHubFeedbackType(
    val label: String,
    val templateFile: String,
    val githubLabel: String,
    val defaultTitlePrefix: String,
    val placeholder: String
) {
    BUG(
        label = "Bug",
        templateFile = "bug_report.yml",
        githubLabel = "bug",
        defaultTitlePrefix = "[Bug] ",
        placeholder = "What broke, and what did you expect to happen? Steps to reproduce help a lot."
    ),
    FEATURE(
        label = "Feature",
        templateFile = "feature_request.yml",
        githubLabel = "enhancement",
        defaultTitlePrefix = "[Feature] ",
        placeholder = "Describe the feature, how it should behave, or which WhatsApp feature it improves."
    ),
    FEEDBACK(
        label = "Feedback",
        templateFile = "feedback.yml",
        githubLabel = "feedback",
        defaultTitlePrefix = "[Feedback] ",
        placeholder = "Share UI/UX thoughts, performance feedback, or suggestions for Wupp."
    )
}

@Composable
fun GitHubFeedbackDialog(
    initialType: GitHubFeedbackType,
    onDismiss: () -> Unit
) {
    val colors = LocalWaColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var selectedType by remember(initialType) { mutableStateOf(initialType) }
    var titleText by remember { mutableStateOf("") }
    var detailsText by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }

    val diagnosticsBlock = remember {
        buildString {
            appendLine("---")
            appendLine("**Environment**")
            appendLine("- App: Wupp v${BuildConfig.VERSION_NAME} (code ${BuildConfig.VERSION_CODE})")
            appendLine("- Repo: `${BuildConfig.GITHUB_REPO}`")
            appendLine("- Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            append("- Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        }
    }

    fun buildFullBody(): String {
        val core = detailsText.trim().ifBlank { "_Describe details here..._" }
        return if (includeDiagnostics) "$core\n\n$diagnosticsBlock" else core
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.elevatedBar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (selectedType) {
                        GitHubFeedbackType.BUG -> Icons.Rounded.BugReport
                        GitHubFeedbackType.FEATURE -> Icons.Rounded.Lightbulb
                        GitHubFeedbackType.FEEDBACK -> Icons.Rounded.ChatBubbleOutline
                    },
                    contentDescription = null,
                    tint = colors.accentGreen,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Send to GitHub",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GitHubFeedbackType.entries.forEach { type ->
                        val selected = selectedType == type
                        FilterChip(
                            selected = selected,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accentGreen.copy(alpha = 0.22f),
                                selectedLabelColor = colors.accentGreen
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Summary") },
                    placeholder = { Text("Short title for ${selectedType.label.lowercase()}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    label = { Text("Details") },
                    placeholder = { Text(selectedType.placeholder, fontSize = 12.sp) },
                    minLines = 4,
                    maxLines = 7,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { includeDiagnostics = !includeDiagnostics }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeDiagnostics,
                        onCheckedChange = { includeDiagnostics = it },
                        colors = CheckboxDefaults.colors(checkedColor = colors.accentGreen)
                    )
                    Column {
                        Text(
                            text = "Attach Wupp & device info",
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "v${BuildConfig.VERSION_NAME} • Android ${Build.VERSION.RELEASE} • ${Build.MODEL}",
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanTitle = if (titleText.isBlank()) {
                        selectedType.defaultTitlePrefix.trim()
                    } else if (titleText.trim().startsWith("[")) {
                        titleText.trim()
                    } else {
                        "${selectedType.defaultTitlePrefix}${titleText.trim()}"
                    }
                    val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8")
                    val encodedBody = URLEncoder.encode(buildFullBody(), "UTF-8")
                    val repo = BuildConfig.GITHUB_REPO.trim().trim('/')
                    val issueUrl = "https://github.com/$repo/issues/new" +
                        "?template=${selectedType.templateFile}" +
                        "&labels=${selectedType.githubLabel}" +
                        "&title=$encodedTitle" +
                        "&body=$encodedBody"

                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl)))
                        onDismiss()
                    }.onFailure {
                        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentGreen,
                    contentColor = Color(0xFF0B141A)
                )
            ) {
                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open GitHub", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        val markdown = buildString {
                            appendLine("## ${selectedType.defaultTitlePrefix}${titleText.trim()}")
                            appendLine()
                            appendLine(buildFullBody())
                        }
                        clipboard.setText(AnnotatedString(markdown))
                        Toast.makeText(context, "Copied report to clipboard", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        }
    )
}

@Composable
fun AppUpdateDialog(
    state: UpdateUiState,
    onDownload: (AppReleaseInfo) -> Unit,
    onInstall: (File) -> Unit,
    onSkipVersion: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalWaColors.current
    val context = LocalContext.current

    val release: AppReleaseInfo = when (state) {
        is UpdateUiState.UpdateAvailable -> state.release
        is UpdateUiState.Downloading -> state.release
        is UpdateUiState.ReadyToInstall -> state.release
        else -> return
    }

    val isDownloading = state is UpdateUiState.Downloading
    val isReady = state is UpdateUiState.ReadyToInstall
    val apkSizeLabel = remember(release.apkSizeBytes) {
        WuppUpdater.formatByteSize(release.apkSizeBytes)
    }

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        containerColor = colors.elevatedBar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accentGreen.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = colors.accentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isReady) "Update Ready to Install" else "Wupp Update Available",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                    val meta = buildString {
                        append("v${BuildConfig.VERSION_NAME} → ${release.tagName}")
                        if (apkSizeLabel.isNotBlank()) append(" ($apkSizeLabel)")
                    }
                    Text(
                        text = meta,
                        color = colors.accentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.appSurface.copy(alpha = 0.65f))
                        .border(0.5.dp, colors.textSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = release.changelog,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                if (state is UpdateUiState.Downloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (state.progressPercent >= 0) {
                            LinearProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.accentGreen
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = colors.accentGreen
                            )
                        }
                        val dlText = buildString {
                            append("Downloading APK")
                            if (state.progressPercent >= 0) append(" ${state.progressPercent}%")
                            val done = WuppUpdater.formatByteSize(state.downloadedBytes)
                            val tot = WuppUpdater.formatByteSize(state.totalBytes)
                            if (done.isNotBlank() && tot.isNotBlank()) append(" ($done / $tot)")
                        }
                        Text(
                            text = dlText,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (state) {
                is UpdateUiState.ReadyToInstall -> {
                    Button(
                        onClick = { onInstall(File(state.apkPath)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentGreen,
                            contentColor = Color(0xFF0B141A)
                        )
                    ) {
                        Text("Install APK", fontWeight = FontWeight.SemiBold)
                    }
                }
                is UpdateUiState.Downloading -> {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Download in Background", color = colors.textPrimary)
                    }
                }
                else -> {
                    if (!release.apkDownloadUrl.isNullOrBlank()) {
                        Button(
                            onClick = { onDownload(release) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGreen,
                                contentColor = Color(0xFF0B141A)
                            )
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Update Now", fontWeight = FontWeight.SemiBold)
                        }
                    } else if (release.htmlUrl.isNotBlank()) {
                        Button(
                            onClick = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)))
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGreen,
                                contentColor = Color(0xFF0B141A)
                            )
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Release", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onSkipVersion) {
                        Text("Skip", color = colors.textSecondary)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Later", color = colors.textSecondary)
                    }
                }
            }
        }
    )
}
