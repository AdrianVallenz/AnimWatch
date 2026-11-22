package com.gows.animwatch.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gows.animwatch.ui.components.AnalogMeterGauge
import com.gows.animwatch.ui.components.AnimWatchCrestLogo
import com.gows.animwatch.ui.components.CassetteTelemetryStrip
import com.gows.animwatch.ui.components.CrtScanlineOverlay
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.components.TapeReelSpinner
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBorder
import com.gows.animwatch.ui.theme.GoldBorderBrush
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.MonoFont
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianGoldCardBrush
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusAmber
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private enum class AuthMode(val label: String) {
    LOGIN("MASUK"),
    REGISTER("DAFTAR")
}

@Composable
fun AuthScreen(
    authError: String?,
    onClearAuthError: () -> Unit,
    onLogin: (identifier: String, password: String, rememberMe: Boolean) -> Unit,
    onRegister: (displayName: String, email: String, password: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onDemoVipLogin: () -> Unit,
    onResetPasswordRequested: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }

    // Login fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var loginIdentifierError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regTermsAccepted by remember { mutableStateOf(true) }
    var regNameError by remember { mutableStateOf<String?>(null) }
    var regEmailError by remember { mutableStateOf<String?>(null) }
    var regPasswordError by remember { mutableStateOf<String?>(null) }
    var regConfirmError by remember { mutableStateOf<String?>(null) }
    var regTermsError by remember { mutableStateOf<String?>(null) }

    var showForgotDialog by remember { mutableStateOf(false) }

    fun submitLogin() {
        focusManager.clearFocus()
        onClearAuthError()
        val idTrim = loginIdentifier.trim()
        loginIdentifierError = if (idTrim.length < 3) "Masukkan email atau username (min. 3 karakter)" else null
        loginPasswordError = if (loginPassword.length < 6) "Kata sandi minimal 6 karakter" else null
        if (loginIdentifierError == null && loginPasswordError == null) {
            onLogin(idTrim, loginPassword, rememberSession)
        }
    }

    fun submitRegister() {
        focusManager.clearFocus()
        onClearAuthError()
        val cleanName = regName.trim()
        val cleanEmail = regEmail.trim()
        regNameError = if (cleanName.length < 2) "Nama tampilan minimal 2 karakter" else null
        regEmailError = if (!cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            "Format email tidak valid (contoh: nama@email.com)"
        } else null
        regPasswordError = if (regPassword.length < 6) "Kata sandi minimal 6 karakter" else null
        regConfirmError = if (regConfirmPassword != regPassword) "Konfirmasi kata sandi tidak cocok" else null
        regTermsError = if (!regTermsAccepted) "Setujui ketentuan layanan untuk mendaftar" else null

        if (regNameError == null &&
            regEmailError == null &&
            regPasswordError == null &&
            regConfirmError == null &&
            regTermsError == null
        ) {
            onRegister(cleanName, cleanEmail, regPassword)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        // Subtle atmospheric accent glow at top + CRT scanline overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to GoldPrimary.copy(alpha = 0.14f),
                        0.32f to ObsidianVoid,
                        1.0f to ObsidianVoid
                    )
                )
        )
        CrtScanlineOverlay(
            modifier = Modifier.fillMaxSize(),
            lineAlpha = 0.07f,
            stepPx = 6f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hardware Telemetry Header
            CassetteTelemetryStrip(
                code = "SYS.AUTH // TERMINAL AKSES",
                statusText = "DECK-OS v1.0",
                ledColor = GoldBright,
                pulsing = true
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 2. Brand Crest + Twin Cassette Tape Deck Visual
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianGoldCardBrush)
                    .border(1.2.dp, GoldBorderBrush, RoundedCornerShape(8.dp))
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    TapeReelSpinner(isSpinning = true, size = 28.dp, tint = GoldPrimary)
                    AnimWatchCrestLogo(size = 54.dp)
                    TapeReelSpinner(isSpinning = true, size = 28.dp, tint = GoldBright)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.ExtraBold)) {
                            append("ANIM")
                        }
                        withStyle(SpanStyle(color = GoldBright, fontWeight = FontWeight.ExtraBold)) {
                            append("WATCH")
                        }
                    },
                    fontFamily = MonoFont,
                    fontSize = 26.sp,
                    letterSpacing = 1.6.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ANIME STREAMING // SUB INDO DECK",
                    fontFamily = MonoFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.1.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Segmented Mode Switcher: MASUK / DAFTAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianElevated)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AuthMode.entries.forEach { tab ->
                    val selected = mode == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selected) GoldGradientBrush
                                else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable {
                                mode = tab
                                onClearAuthError()
                            }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            fontFamily = MonoFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) TextOnGold else TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Auth Form Console Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianCard)
                    .border(1.dp, GoldBorder, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                if (authError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusLive.copy(alpha = 0.14f))
                            .border(1.dp, StatusLive, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "ERR // $authError",
                            fontFamily = MonoFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusLive
                        )
                    }
                }

                AnimatedContent(
                    targetState = mode,
                    transitionSpec = {
                        (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 16 }) togetherWith
                            fadeOut(tween(180))
                    },
                    label = "auth_mode_form"
                ) { currentMode ->
                    when (currentMode) {
                        AuthMode.LOGIN -> {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TactileInputField(
                                    label = "Email atau Username",
                                    value = loginIdentifier,
                                    onValueChange = {
                                        loginIdentifier = it
                                        loginIdentifierError = null
                                        onClearAuthError()
                                    },
                                    placeholder = "adrian@animwatch.id",
                                    errorText = loginIdentifierError,
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
                                    label = "Kata Sandi",
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        loginPasswordError = null
                                        onClearAuthError()
                                    },
                                    placeholder = "Minimal 6 karakter",
                                    errorText = loginPasswordError,
                                    visualTransformation = if (loginPasswordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { loginPasswordVisible = !loginPasswordVisible },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) {
                                                    Icons.Rounded.VisibilityOff
                                                } else {
                                                    Icons.Rounded.Visibility
                                                },
                                                contentDescription = "Tampilkan sandi",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { submitLogin() })
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { rememberSession = !rememberSession }
                                    ) {
                                        Checkbox(
                                            checked = rememberSession,
                                            onCheckedChange = { rememberSession = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = GoldBright,
                                                uncheckedColor = GoldBorder,
                                                checkmarkColor = TextOnGold
                                            )
                                        )
                                        Text(
                                            text = "Ingat sesi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }

                                    Text(
                                        text = "LUPA SANDI?",
                                        fontFamily = MonoFont,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldBright,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .clickable { showForgotDialog = true }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }

                                GoldGradientButton(
                                    text = "Masuk ke AnimWatch",
                                    icon = Icons.AutoMirrored.Rounded.Login,
                                    onClick = ::submitLogin,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 48.dp
                                )

                                Text(
                                    text = "INFO DEMO // adrian@animwatch.id • sandi: 123456",
                                    fontFamily = MonoFont,
                                    fontSize = 9.5.sp,
                                    color = TextTertiary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        AuthMode.REGISTER -> {
                            val strengthFraction = when {
                                regPassword.isEmpty() -> 0f
                                regPassword.length < 6 -> 0.28f
                                regPassword.length < 9 -> 0.62f
                                else -> 1.0f
                            }
                            val (strengthLabel, strengthColor) = when {
                                regPassword.isEmpty() -> "BELUM DIISI" to TextTertiary
                                regPassword.length < 6 -> "LEMAH" to StatusLive
                                regPassword.length < 9 -> "CUKUP KUAT" to StatusAmber
                                else -> "AMAN & KUAT" to StatusEmerald
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TactileInputField(
                                    label = "Nama Tampilan",
                                    value = regName,
                                    onValueChange = {
                                        regName = it
                                        regNameError = null
                                        onClearAuthError()
                                    },
                                    placeholder = "Contoh: Adrian Vallenz",
                                    errorText = regNameError,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Badge,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                                )

                                TactileInputField(
                                    label = "Alamat Email",
                                    value = regEmail,
                                    onValueChange = {
                                        regEmail = it
                                        regEmailError = null
                                        onClearAuthError()
                                    },
                                    placeholder = "nama@email.com",
                                    errorText = regEmailError,
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

                                Column {
                                    TactileInputField(
                                        label = "Kata Sandi",
                                        value = regPassword,
                                        onValueChange = {
                                            regPassword = it
                                            regPasswordError = null
                                            onClearAuthError()
                                        },
                                        placeholder = "Minimal 6 karakter",
                                        errorText = regPasswordError,
                                        visualTransformation = if (regPasswordVisible) {
                                            VisualTransformation.None
                                        } else {
                                            PasswordVisualTransformation()
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Rounded.Lock,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { regPasswordVisible = !regPasswordVisible },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (regPasswordVisible) {
                                                        Icons.Rounded.VisibilityOff
                                                    } else {
                                                        Icons.Rounded.Visibility
                                                    },
                                                    contentDescription = "Tampilkan sandi",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Next
                                        )
                                    )
                                    if (regPassword.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "KEKUATAN SANDI",
                                                fontFamily = MonoFont,
                                                fontSize = 9.5.sp,
                                                color = TextTertiary
                                            )
                                            Text(
                                                text = strengthLabel,
                                                fontFamily = MonoFont,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = strengthColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        AnalogMeterGauge(
                                            progress = strengthFraction,
                                            segments = 12,
                                            height = 6.dp,
                                            activeColor = strengthColor
                                        )
                                    }
                                }

                                TactileInputField(
                                    label = "Konfirmasi Kata Sandi",
                                    value = regConfirmPassword,
                                    onValueChange = {
                                        regConfirmPassword = it
                                        regConfirmError = null
                                    },
                                    placeholder = "Ulangi kata sandi",
                                    errorText = regConfirmError,
                                    visualTransformation = if (regPasswordVisible) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { submitRegister() })
                                )

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable {
                                            regTermsAccepted = !regTermsAccepted
                                            regTermsError = null
                                        }
                                    ) {
                                        Checkbox(
                                            checked = regTermsAccepted,
                                            onCheckedChange = {
                                                regTermsAccepted = it
                                                regTermsError = null
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = GoldBright,
                                                uncheckedColor = GoldBorder,
                                                checkmarkColor = TextOnGold
                                            )
                                        )
                                        Text(
                                            text = "Saya menyetujui Ketentuan Layanan & Privasi AnimWatch",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    if (regTermsError != null) {
                                        Text(
                                            text = regTermsError.orEmpty(),
                                            fontFamily = MonoFont,
                                            fontSize = 10.sp,
                                            color = StatusLive,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    }
                                }

                                GoldGradientButton(
                                    text = "Daftar & Mulai Nonton",
                                    icon = Icons.Rounded.PersonAdd,
                                    onClick = ::submitRegister,
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 48.dp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Quick Access Divider & Instant Demo / Guest Keys
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(color = DividerSubtle, modifier = Modifier.weight(1f))
                Text(
                    text = "  AKSES CEPAT  ",
                    fontFamily = MonoFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.8.sp
                )
                HorizontalDivider(color = DividerSubtle, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GoldOutlinedButton(
                    text = "Demo VIP",
                    icon = Icons.Rounded.AutoAwesome,
                    isActive = true,
                    onClick = onDemoVipLogin,
                    modifier = Modifier.weight(1f),
                    height = 44.dp
                )
                GoldOutlinedButton(
                    text = "Tamu",
                    icon = Icons.Rounded.PersonOutline,
                    isActive = false,
                    onClick = onContinueAsGuest,
                    modifier = Modifier.weight(1f),
                    height = 44.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Switch Prompt
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) "Belum punya akun?" else "Sudah punya akun?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (mode == AuthMode.LOGIN) "DAFTAR SEKARANG" else "MASUK DI SINI",
                    fontFamily = MonoFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldBright,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                            onClearAuthError()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showForgotDialog) {
        ForgotPasswordDialog(
            initialEmail = if (loginIdentifier.contains("@")) loginIdentifier else "",
            onSendReset = { email ->
                onResetPasswordRequested(email)
                showForgotDialog = false
            },
            onDismiss = { showForgotDialog = false }
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    onSendReset: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        titleContentColor = GoldBright,
        title = {
            Text(
                text = "PEMULIHAN KATA SANDI",
                fontFamily = MonoFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Masukkan alamat email terdaftar untuk menerima tautan pemulihan kata sandi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                TactileInputField(
                    label = "Email Terdaftar",
                    value = email,
                    onValueChange = {
                        email = it
                        error = null
                    },
                    placeholder = "nama@email.com",
                    errorText = error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            GoldGradientButton(
                text = "Kirim Tautan",
                onClick = {
                    val trimmed = email.trim()
                    if (!trimmed.contains("@") || !trimmed.contains(".")) {
                        error = "Masukkan format email yang valid"
                    } else {
                        onSendReset(trimmed)
                    }
                },
                height = 40.dp
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("BATAL", fontFamily = MonoFont, color = TextSecondary)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AnimWatchTheme {
        AuthScreen(
            authError = null,
            onClearAuthError = {},
            onLogin = { _, _, _ -> },
            onRegister = { _, _, _ -> },
            onContinueAsGuest = {},
            onDemoVipLogin = {},
            onResetPasswordRequested = {}
        )
    }
}
