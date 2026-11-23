package com.gows.animwatch.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.gows.animwatch.ui.components.GoldGradientButton
import com.gows.animwatch.ui.components.GoldOutlinedButton
import com.gows.animwatch.ui.components.TactileInputField
import com.gows.animwatch.ui.theme.AnimWatchTheme
import com.gows.animwatch.ui.theme.DividerSubtle
import com.gows.animwatch.ui.theme.GoldBright
import com.gows.animwatch.ui.theme.GoldGradientBrush
import com.gows.animwatch.ui.theme.GoldPrimary
import com.gows.animwatch.ui.theme.GoldSubtleBorder
import com.gows.animwatch.ui.theme.IsDarkTheme
import com.gows.animwatch.ui.theme.ObsidianCard
import com.gows.animwatch.ui.theme.ObsidianElevated
import com.gows.animwatch.ui.theme.ObsidianVoid
import com.gows.animwatch.ui.theme.StatusAmber
import com.gows.animwatch.ui.theme.StatusEmerald
import com.gows.animwatch.ui.theme.StatusLive
import com.gows.animwatch.ui.theme.TextOnGold
import com.gows.animwatch.ui.theme.TextPrimary
import com.gows.animwatch.ui.theme.TextSecondary
import com.gows.animwatch.ui.theme.TextTertiary

private enum class AuthMode(val label: String) {
    LOGIN("Masuk"),
    REGISTER("Daftar")
}

@Composable
fun AuthScreen(
    authError: String?,
    isGoogleLoading: Boolean = false,
    onClearAuthError: () -> Unit,
    onLogin: (identifier: String, password: String, rememberMe: Boolean) -> Unit,
    onRegister: (displayName: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit = {},
    onContinueAsGuest: () -> Unit,
    onDemoVipLogin: () -> Unit,
    onResetPasswordRequested: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }

    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var loginIdentifierError by remember { mutableStateOf<String?>(null) }
    var loginPasswordError by remember { mutableStateOf<String?>(null) }

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
        loginIdentifierError = if (idTrim.length < 3) "Masukkan email atau username minimal 3 karakter" else null
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
        regTermsError = if (!regTermsAccepted) "Setujui ketentuan layanan untuk melanjutkan" else null

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
        // Gentle organic ambient glow behind header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    Brush.verticalGradient(
                        0.0f to GoldPrimary.copy(alpha = if (IsDarkTheme) 0.16f else 0.12f),
                        0.65f to GoldPrimary.copy(alpha = 0.03f),
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
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Organic Brand Header
            AnimWatchCrestLogo(size = 64.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                        append("Anim")
                    }
                    withStyle(SpanStyle(color = GoldPrimary, fontWeight = FontWeight.Bold)) {
                        append("Watch")
                    }
                },
                fontSize = 30.sp,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (mode == AuthMode.LOGIN) {
                    "Selamat datang kembali. Masuk untuk melanjutkan tontonan anime favoritmu."
                } else {
                    "Buat akun baru dan atur tampilan tema sesuai seleramu."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Smooth Pill Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(ObsidianElevated)
                    .border(0.75.dp, GoldSubtleBorder, CircleShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AuthMode.entries.forEach { tab ->
                    val selected = mode == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
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
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) TextOnGold else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Soft Rounded Form Surface
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = ObsidianCard,
                shadowElevation = if (IsDarkTheme) 0.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .border(0.75.dp, GoldSubtleBorder, RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
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
                                color = StatusLive
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            (fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 18 }) togetherWith
                                fadeOut(tween(180))
                        },
                        label = "auth_mode_form"
                    ) { currentMode ->
                        when (currentMode) {
                            AuthMode.LOGIN -> {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                                modifier = Modifier.size(19.dp)
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
                                                modifier = Modifier.size(19.dp)
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
                                                    modifier = Modifier.size(19.dp)
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
                                                    checkedColor = GoldPrimary,
                                                    uncheckedColor = TextTertiary,
                                                    checkmarkColor = TextOnGold
                                                )
                                            )
                                            Text(
                                                text = "Ingat sesi saya",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }

                                        Text(
                                            text = "Lupa sandi?",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GoldPrimary,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { showForgotDialog = true }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    GoldGradientButton(
                                        text = "Masuk Sekarang",
                                        icon = Icons.AutoMirrored.Rounded.Login,
                                        onClick = ::submitLogin,
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 50.dp
                                    )

                                    Text(
                                        text = "Akun demo: adrian@animwatch.id • Sandi: animwatch123",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.5.sp,
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
                                    regPassword.length < 9 -> 0.65f
                                    else -> 1.0f
                                }
                                val (strengthLabel, strengthColor) = when {
                                    regPassword.isEmpty() -> "Belum diisi" to TextTertiary
                                    regPassword.length < 6 -> "Terlalu pendek" to StatusLive
                                    regPassword.length < 9 -> "Cukup kuat" to StatusAmber
                                    else -> "Sangat kuat" to StatusEmerald
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
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
                                                modifier = Modifier.size(19.dp)
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
                                                modifier = Modifier.size(19.dp)
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
                                                    modifier = Modifier.size(19.dp)
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
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password,
                                                imeAction = ImeAction.Next
                                            )
                                        )
                                        if (regPassword.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(7.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Kekuatan sandi",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 11.5.sp,
                                                    color = TextTertiary
                                                )
                                                Text(
                                                    text = strengthLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = strengthColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(5.dp))
                                            AnalogMeterGauge(
                                                progress = strengthFraction,
                                                height = 5.dp,
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
                                                modifier = Modifier.size(19.dp)
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
                                                    checkedColor = GoldPrimary,
                                                    uncheckedColor = TextTertiary,
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
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusLive,
                                                modifier = Modifier.padding(start = 12.dp)
                                            )
                                        }
                                    }

                                    GoldGradientButton(
                                        text = "Buat Akun Baru",
                                        icon = Icons.Rounded.PersonAdd,
                                        onClick = ::submitRegister,
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 50.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Access Divider & Instant Demo / Guest Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(color = DividerSubtle, modifier = Modifier.weight(1f))
                Text(
                    text = "  Atau masuk cepat  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                HorizontalDivider(color = DividerSubtle, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Official Google Sign-In Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isGoogleLoading) {
                        focusManager.clearFocus()
                        onClearAuthError()
                        onGoogleSignIn()
                    },
                shape = RoundedCornerShape(16.dp),
                color = if (IsDarkTheme) ObsidianElevated else Color.White,
                shadowElevation = if (IsDarkTheme) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.9.dp, GoldSubtleBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Menghubungkan Google...",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    } else {
                        GoogleLogoMark(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (mode == AuthMode.LOGIN) "Masuk dengan Google" else "Daftar dengan Google",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoldOutlinedButton(
                    text = "Akun Demo",
                    icon = Icons.Rounded.AutoAwesome,
                    isActive = true,
                    onClick = onDemoVipLogin,
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
                GoldOutlinedButton(
                    text = "Mode Tamu",
                    icon = Icons.Rounded.PersonOutline,
                    isActive = false,
                    onClick = onContinueAsGuest,
                    modifier = Modifier.weight(1f),
                    height = 46.dp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

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
                    text = if (mode == AuthMode.LOGIN) "Daftar sekarang" else "Masuk di sini",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldPrimary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                            onClearAuthError()
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
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
        shape = RoundedCornerShape(24.dp),
        titleContentColor = TextPrimary,
        title = {
            Text(
                text = "Pemulihan Kata Sandi",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
private fun GoogleLogoMark(modifier: Modifier = Modifier) {
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
            onLogin = { _, _, _ -> },
            onRegister = { _, _, _ -> },
            onGoogleSignIn = {},
            onContinueAsGuest = {},
            onDemoVipLogin = {},
            onResetPasswordRequested = {}
        )
    }
}
