package com.example.glance

import android.content.Context
import androidx.glance.text.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.example.glanceexample.PriceDataRepo
import com.example.glanceexample.R
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

    private fun refreshPrice() {
        PriceDataRepo.update()
    }

    companion object {
        private var job: Job? = null
        private val smallMode = DpSize(100.dp, 80.dp)
        private val mediumMode = DpSize(120.dp, 120.dp)
    }
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )

    @Composable
    fun GlanceContent() {
        val stateCount by PriceDataRepo.currentPrice.collectAsState()
        val size = LocalSize.current
        when (size) {
            smallMode -> Small(stateCount)
            mediumMode -> Medium(stateCount)
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

    @Composable
    private fun Small(stateCount: Float) {
        Column(modifier = GlanceModifier
            .clickable { refreshPrice() }
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(8.dp)) {
            StockDisplay(stateCount)
        }
    }

    @Composable
    private fun Medium(stateCount: Float) {
        Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier
                .clickable { refreshPrice() }
                .fillMaxSize()
                .cornerRadius(15.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)) {
            StockDisplay(stateCount)
            Image(
                provider = ImageProvider(if (PriceDataRepo.change > 0)
                    R.drawable.up_arrow else R.drawable.down_arrow),
                contentDescription = "Arrow Image",
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(20.dp)
            )
        }
    }
}
