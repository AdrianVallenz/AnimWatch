package com.gows.animwatch.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.ui.components.AnimWatchCrestLogo
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.GoldTint
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGoldCardBrush
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

@Composable
fun AuthScreen(
    authError: String?,
    isGoogleLoading: Boolean = false,
    hasNativeGoogleOAuth: Boolean = false,
    forceOpenManualDialog: Boolean = false,
    onConsumeForceOpenManualDialog: () -> Unit = {},
    onClearAuthError: () -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onGoogleDirectSignIn: (email: String, displayName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showDirectGoogleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(forceOpenManualDialog) {
        if (forceOpenManualDialog) {
            showDirectGoogleDialog = true
            onConsumeForceOpenManualDialog()
        }
    }

    // Native Android System Google Account Picker ("Pilih akun untuk AnimWatch")
    val googleAccountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data
                ?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                ?.trim()
                .orEmpty()
            if (selectedEmail.isNotBlank()) {
                val derivedName = selectedEmail
                    .substringBefore("@")
                    .replace(Regex("[._-]+"), " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
                    .ifEmpty { "Google User" }
                onGoogleDirectSignIn(selectedEmail, derivedName)
            } else {
                showDirectGoogleDialog = true
            }
        }
    }

    val launchSystemGoogleAccountPicker = {
        if (hasNativeGoogleOAuth) {
            onGoogleSignIn()
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                val pickerIntent: Intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    false,
                    null,
                    null,
                    null,
                    null
                )
                googleAccountPickerLauncher.launch(pickerIntent)
            }.onFailure {
                showDirectGoogleDialog = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Ambient header glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to GoldPrimary.copy(alpha = if (IsDarkTheme) 0.18f else 0.12f),
                        0.65f to GoldPrimary.copy(alpha = 0.04f),
                        1.0f to Color.Transparent
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimWatchCrestLogo(size = 76.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                        append("Anim")
                    }
                    withStyle(SpanStyle(color = GoldPrimary, fontWeight = FontWeight.Bold)) {
                        append("Watch")
                    }
                },
                fontSize = 34.sp,
                letterSpacing = (-0.6).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Streaming ratusan anime Sub Indo & Dub Indo resmi dalam satu aplikasi modern.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Google Auth Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = ObsidianCard,
                shadowElevation = if (IsDarkTheme) 0.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(ObsidianGoldCardBrush)
                        .border(0.85.dp, GoldSubtleBorder, RoundedCornerShape(26.dp))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Masuk dengan Akun Google",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Gunakan akun Google kamu untuk menyinkronkan profil kustom, daftar Disimpan, riwayat tonton, dan komentar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (authError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(StatusLive.copy(alpha = 0.12f))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = authError,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = StatusLive,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Primary Google Sign-In Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !isGoogleLoading) {
                                onClearAuthError()
                                launchSystemGoogleAccountPicker()
                            },
                        shape = RoundedCornerShape(18.dp),
                        color = if (IsDarkTheme) Color.White else ObsidianElevated,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.2.dp,
                                    color = Color(0xFF4285F4)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Menghubungkan Akun Google...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (IsDarkTheme) Color(0xFF111827) else TextPrimary
                                )
                            } else {
                                GoogleLogoMark(modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Lanjutkan dengan Google",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (IsDarkTheme) Color(0xFF111827) else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Instant in-app Google Account entry option
                    Text(
                        text = "Masuk cepat dengan alamat Gmail",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(enabled = !isGoogleLoading) {
                                onClearAuthError()
                                showDirectGoogleDialog = true
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )

                    HorizontalDivider(
                        color = DividerSubtle,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    // Feature highlights synced with Google account
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GoogleBenefitRow(
                            icon = Icons.Rounded.CloudDone,
                            title = "Sinkronisasi Otomatis",
                            subtitle = "Daftar Disimpan & riwayat tonton tersimpan di akun Google"
                        )
                        GoogleBenefitRow(
                            icon = Icons.Rounded.Palette,
                            title = "Custom Profile & Avatar",
                            subtitle = "Ubah foto profil, inisial, username @handle, gelar & bio"
                        )
                        GoogleBenefitRow(
                            icon = Icons.Rounded.ChatBubbleOutline,
                            title = "Komunitas & Komentar Anime",
                            subtitle = "Diskusi per episode, balas komentar, suka, dan filter spoiler"
                        )
                    }
                }
            }
        }
    }

    if (showDirectGoogleDialog) {
        GoogleAccountQuickInputDialog(
            onConfirm = { email, name ->
                showDirectGoogleDialog = false
                onGoogleDirectSignIn(email, name)
            },
            onDismiss = { showDirectGoogleDialog = false }
        )
    }
}

@Composable
private fun GoogleBenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GoldTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary,
                fontSize = 11.5.sp
            )
        }
    }
}

@Composable
private fun GoogleAccountQuickInputDialog(
    onConfirm: (email: String, displayName: String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GoogleLogoMark(modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Akun Google",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Masukkan alamat Gmail dan nama tampilan Google kamu untuk masuk langsung ke AnimWatch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                TactileInputField(
                    label = "Email Google (Gmail)",
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    placeholder = "nama@gmail.com",
                    errorText = emailError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Email,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                TactileInputField(
                    label = "Nama Tampilan Google",
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        nameError = null
                    },
                    placeholder = "Nama profil kamu",
                    errorText = nameError,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Badge,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Masuk dengan Google",
                icon = Icons.Rounded.AutoAwesome,
                onClick = {
                    val cleanEmail = email.trim().lowercase()
                    val cleanName = displayName.trim().ifEmpty {
                        cleanEmail.substringBefore("@")
                            .replace(Regex("[._-]+"), " ")
                            .replaceFirstChar { it.uppercase() }
                    }
                    emailError = if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
                        "Masukkan alamat email Google yang valid"
                    } else null
                    nameError = if (cleanName.length < 2) "Nama minimal 2 karakter" else null
                    if (emailError == null && nameError == null) {
                        onConfirm(cleanEmail, cleanName)
                    }
                },
                height = 42.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}

@Composable
fun GoogleLogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.19f
        val inset = strokeWidth / 2f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        // Red (top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Yellow (left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Green (bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 35f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue (right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -5f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )
        // Blue horizontal bar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.50f, size.height * 0.50f),
            end = Offset(size.width - inset, size.height * 0.50f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Butt
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AnimWatchTheme {
        AuthScreen(
            authError = null,
            isGoogleLoading = false,
            onClearAuthError = {},
            onGoogleSignIn = {},
            onGoogleDirectSignIn = { _, _ -> }
        )
    }
}
