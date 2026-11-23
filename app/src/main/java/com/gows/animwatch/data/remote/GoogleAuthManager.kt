package com.gows.animwatch.data.remote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed interface GoogleSignInOutcome {
    data class IdToken(val idToken: String) : GoogleSignInOutcome
    data object LaunchedBrowserOAuth : GoogleSignInOutcome
    data object OpenInAppGooglePrompt : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
    data class Failure(val message: String) : GoogleSignInOutcome
}

data class GoogleOAuthCallbackPayload(
    val token: String? = null,
    val error: String? = null
)

object GoogleAuthDeepLinkBus {
    private val _pendingCallback = MutableStateFlow<GoogleOAuthCallbackPayload?>(null)
    val pendingCallback: StateFlow<GoogleOAuthCallbackPayload?> = _pendingCallback.asStateFlow()

    fun dispatchFromIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (!uri.scheme.equals("animwatch", ignoreCase = true)) return
        if (!uri.host.equals("auth", ignoreCase = true)) return
        val token = uri.getQueryParameter("token")?.takeIf { it.isNotBlank() }
        val error = uri.getQueryParameter("error")?.takeIf { it.isNotBlank() }
        if (token != null || error != null) {
            _pendingCallback.value = GoogleOAuthCallbackPayload(token = token, error = error)
        }
    }

    fun consume() {
        _pendingCallback.value = null
    }
}

object GoogleAuthManager {

    suspend fun signIn(
        activity: Activity,
        webClientId: String,
        webOAuthEnabled: Boolean,
        apiBaseUrl: String
    ): GoogleSignInOutcome {
        val cleanClientId = webClientId.trim()
        if (cleanClientId.isEmpty()) {
            return if (webOAuthEnabled) {
                launchBrowserOAuth(activity, apiBaseUrl)
            } else {
                GoogleSignInOutcome.OpenInAppGooglePrompt
            }
        }

        val credentialManager = CredentialManager.create(activity)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(cleanClientId)
            .setNonce(UUID.randomUUID().toString())
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        return try {
            val response = credentialManager.getCredential(
                request = request,
                context = activity
            )
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                if (googleIdTokenCredential.idToken.isNotBlank()) {
                    GoogleSignInOutcome.IdToken(googleIdTokenCredential.idToken)
                } else if (webOAuthEnabled) {
                    launchBrowserOAuth(activity, apiBaseUrl)
                } else {
                    GoogleSignInOutcome.OpenInAppGooglePrompt
                }
            } else if (webOAuthEnabled) {
                launchBrowserOAuth(activity, apiBaseUrl)
            } else {
                GoogleSignInOutcome.OpenInAppGooglePrompt
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (_: GetCredentialException) {
            if (webOAuthEnabled) launchBrowserOAuth(activity, apiBaseUrl)
            else GoogleSignInOutcome.OpenInAppGooglePrompt
        } catch (_: Exception) {
            if (webOAuthEnabled) launchBrowserOAuth(activity, apiBaseUrl)
            else GoogleSignInOutcome.OpenInAppGooglePrompt
        }
    }

    fun launchBrowserOAuth(context: Context, apiBaseUrl: String): GoogleSignInOutcome {
        val normalizedBase = KumanimeApiClient.normalizeBaseUrl(apiBaseUrl)
        val startUrl = "${normalizedBase}auth/google/start"
        val uri = Uri.parse(startUrl)
        return runCatching {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, uri)
            GoogleSignInOutcome.LaunchedBrowserOAuth
        }.recoverCatching {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            GoogleSignInOutcome.LaunchedBrowserOAuth
        }.getOrElse { err ->
            GoogleSignInOutcome.Failure("Tidak dapat membuka halaman login Google: ${err.localizedMessage ?: "Browser tidak ditemukan"}")
        }
    }
}
