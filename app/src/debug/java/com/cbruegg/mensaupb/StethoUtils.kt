import android.app.Application
import com.facebook.stetho.Stetho.initializeWithDefaults
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient

/**
 * Install Stetho if this is a debug build.
 */
fun Application.installStetho() {
    initializeWithDefaults(this)
}

/**
 * Install Stetho if this is a debug build.
 */
fun OkHttpClient.Builder.installStetho(): OkHttpClient.Builder =
    addNetworkInterceptor(StethoInterceptor())