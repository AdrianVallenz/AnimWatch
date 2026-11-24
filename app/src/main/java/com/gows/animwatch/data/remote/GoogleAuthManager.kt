package com.gows.animwatch.data.remote

import android.accounts.Account
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
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume

sealed interface GoogleSignInOutcome {
    data class IdToken(val idToken: String) : GoogleSignInOutcome
    data class AccessToken(val accessToken: String) : GoogleSignInOutcome
    data class NeedsUserConsent(val consentIntent: Intent, val account: Account) : GoogleSignInOutcome
    data object LaunchedBrowserOAuth : GoogleSignInOutcome
    data object Cancelled : GoogleSignInOutcome
    data class NeedsFallback(val reason: String) : GoogleSignInOutcome
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
        val path = uri.path.orEmpty()
        if (path.isNotEmpty() && !path.startsWith("/google", ignoreCase = true)) return
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

    private const val GOOGLE_OAUTH_SCOPE =
        "oauth2:https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile openid"

    /**
     * Prepares the classic Google Play Services "Choose an account" dialog intent
     * (shows all accounts on device + "Use another account").
     * Works natively with Android OAuth Client ID (package + SHA-1) without requiring
     * a separate Web Client ID in requestIdToken(), and signs out cached state first
     * so the account chooser dialog always appears.
     */
    @Suppress("DEPRECATION")
    suspend fun prepareAccountChooserIntent(
        activity: Activity,
        clientId: String
    ): Intent? {
        if (clientId.trim().isEmpty()) return null
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        suspendCancellableCoroutine { cont ->
            client.signOut().addOnCompleteListener {
                if (cont.isActive) cont.resume(Unit)
            }
        }
        return client.signInIntent
    }

    @Suppress("DEPRECATION")
    suspend fun completeAccountChooserResult(
        context: Context,
        resultCode: Int,
        data: Intent?
    ): GoogleSignInOutcome {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val signedIn = task.getResult(ApiException::class.java)
            val idToken = signedIn?.idToken.orEmpty().trim()
            if (idToken.isNotEmpty()) {
                return GoogleSignInOutcome.IdToken(idToken)
            }
            val androidAccount = signedIn?.account
            if (androidAccount != null) {
                fetchAccessTokenForAccount(context, androidAccount)
            } else {
                GoogleSignInOutcome.NeedsFallback("Akun Google tidak mengembalikan kredensial OAuth.")
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED,
                CommonStatusCodes.CANCELED,
                GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS -> GoogleSignInOutcome.Cancelled
                else -> {
                    if (resultCode == Activity.RESULT_CANCELED && data == null) {
                        GoogleSignInOutcome.Cancelled
                    } else {
                        GoogleSignInOutcome.NeedsFallback(
                            "Google Sign-In gagal (kode ${e.statusCode}). Pastikan SHA-1 APK sesuai di Google Cloud Console."
                        )
                    }
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            if (resultCode == Activity.RESULT_CANCELED) {
                GoogleSignInOutcome.Cancelled
            } else {
                GoogleSignInOutcome.NeedsFallback(e.localizedMessage ?: "Gagal membaca hasil pemilih akun Google.")
            }
        }
    }

    suspend fun fetchAccessTokenForAccount(
        context: Context,
        account: Account
    ): GoogleSignInOutcome = withContext(Dispatchers.IO) {
        try {
            val token = GoogleAuthUtil.getToken(
                context.applicationContext,
                account,
                GOOGLE_OAUTH_SCOPE
            ).trim()
            if (token.isNotEmpty()) {
                GoogleSignInOutcome.AccessToken(token)
            } else {
                GoogleSignInOutcome.Failure("Token OAuth Google kosong.")
            }
        } catch (recoverable: UserRecoverableAuthException) {
            val intent = recoverable.intent
            if (intent != null) {
                GoogleSignInOutcome.NeedsUserConsent(intent, account)
            } else {
                GoogleSignInOutcome.Failure("Izin akses profil Google diperlukan.")
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            GoogleSignInOutcome.Failure(
                "Gagal mengambil token Google: ${e.localizedMessage ?: "Periksa koneksi internet"}"
            )
        }
    }

    suspend fun signInFallback(
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
                GoogleSignInOutcome.Failure(
                    "Google Client ID belum dikonfigurasi. Tambahkan Google Web Client ID agar menu 'Choose an account' aktif."
                )
            }
        }

        val credentialManager = CredentialManager.create(activity)
        val nonce = UUID.randomUUID().toString()

        // 1. Secondary native Credential Manager Sign-In With Google sheet
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(cleanClientId)
            .setNonce(nonce)
            .build()
        val primaryResult = attemptCredentialRequest(
            credentialManager = credentialManager,
            activity = activity,
            request = GetCredentialRequest.Builder().addCredentialOption(signInWithGoogleOption).build()
        )
        if (primaryResult is GoogleSignInOutcome.IdToken || primaryResult is GoogleSignInOutcome.Cancelled) {
            return primaryResult
        }

        // 2. Fallback Google ID option (shows all accounts on device)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(cleanClientId)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()
        val secondaryResult = attemptCredentialRequest(
            credentialManager = credentialManager,
            activity = activity,
            request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        )
        if (secondaryResult is GoogleSignInOutcome.IdToken || secondaryResult is GoogleSignInOutcome.Cancelled) {
            return secondaryResult
        }

        // 3. Final secure fallback: Browser OAuth (prompt=select_account renders Google "Choose an account" web page)
        if (webOAuthEnabled) {
            return launchBrowserOAuth(activity, apiBaseUrl)
        }

        return (secondaryResult as? GoogleSignInOutcome.Failure)
            ?: (primaryResult as? GoogleSignInOutcome.Failure)
            ?: GoogleSignInOutcome.Failure("Gagal menampilkan menu pemilihan akun Google. Pastikan SHA-1 Android & Web Client ID sesuai.")
    }

    private suspend fun attemptCredentialRequest(
        credentialManager: CredentialManager,
        activity: Activity,
        request: GetCredentialRequest
    ): GoogleSignInOutcome {
        return try {
            val response = credentialManager.getCredential(request = request, context = activity)
            val credential = response.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCred = GoogleIdTokenCredential.createFrom(credential.data)
                if (googleCred.idToken.isNotBlank()) {
                    GoogleSignInOutcome.IdToken(googleCred.idToken)
                } else {
                    GoogleSignInOutcome.Failure("Token Google kosong.")
                }
            } else {
                GoogleSignInOutcome.Failure("Kredensial bukan akun Google.")
            }
        } catch (_: GetCredentialCancellationException) {
            GoogleSignInOutcome.Cancelled
        } catch (e: GetCredentialException) {
            GoogleSignInOutcome.Failure(e.errorMessage?.toString() ?: "Autentikasi Google tidak tersedia.")
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            GoogleSignInOutcome.Failure(e.localizedMessage ?: "Gagal membuka pemilihan akun Google.")
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
