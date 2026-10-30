package uz.finansflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import uz.finansflow.app.ui.FinanceApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FinansFlowTheme { FinanceApp() } }
    }
}

@Composable
private fun FinansFlowTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val lightScheme = lightColorScheme(
        primary = Color(0xFF6455D8), secondary = Color(0xFF3B8D7B),
        background = Color(0xFFF7F7FB), surface = Color(0xFFFFFFFF),
        error = Color(0xFFBA1A1A)
    )
    val darkScheme = darkColorScheme(
        primary = Color(0xFFBDB0FF), secondary = Color(0xFF80D8C2),
        background = Color(0xFF111119), surface = Color(0xFF1B1B25),
        error = Color(0xFFFFB4AB)
    )
    MaterialTheme(colorScheme = if (dark) darkScheme else lightScheme, content = content)
}
