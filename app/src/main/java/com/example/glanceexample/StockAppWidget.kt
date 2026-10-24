package com.example.glance

import android.content.Context
import androidx.glance.text.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.example.glanceexample.PriceDataRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class StockAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        if (job == null) {
            job = startUpdateJob(20.seconds, context)
        }
        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    private fun startUpdateJob(timeInterval: Duration, context: Context): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(timeInterval)
                PriceDataRepo.update()
                StockAppWidget().updateAll(context)
            }
        }
    }

    companion object {
        private var job: Job? = null
    }

    @Composable
    fun GlanceContent() {
        Column(modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)
        ) {
            Text("Demo")
        }
    }

    @Composable
    private fun StockDisplay(stateCount: Float) {
        val color = if (PriceDataRepo.change > 0) {
            GlanceTheme.colors.primary
        } else {
            GlanceTheme.colors.error
        }
        val textStyle = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(PriceDataRepo.ticker, style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)
        )
        Text(text = String.format(Locale.getDefault(), "%.2f", stateCount),
            style = textStyle)
        Text("${PriceDataRepo.change} %", style = textStyle)
    }
}
