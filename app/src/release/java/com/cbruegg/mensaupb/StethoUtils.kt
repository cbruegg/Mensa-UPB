import android.app.Application
import okhttp3.OkHttpClient

/**
 * Install Stetho if this is a debug build.
 */
fun installStetho() {}

/**
 * Install Stetho if this is a debug build.
 */
fun OkHttpClient.Builder.installStetho() = this