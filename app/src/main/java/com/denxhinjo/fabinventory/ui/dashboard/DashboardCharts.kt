package com.denxhinjo.fabinventory.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class DayPoint(val label: String, val stockIn: Double, val stockOut: Double)

data class DonutSlice(val label: String, val value: Int, val color: Color)

data class BarEntry(val label: String, val value: Double)

data class GaugeEntry(val label: String, val pct: Int, val color: Color)

/**
 * Compact dual-line trend chart (Stock In vs Stock Out) drawn directly with
 * Canvas rather than pulling in a charting library, matching the "Revenue &
 * Expenses" style line chart on the web dashboard at a scale that fits a
 * phone screen.
 */
@Composable
fun TrendChart(points: List<DayPoint>, modifier: Modifier = Modifier) {
    val inColor = MaterialTheme.colorScheme.primary
    val outColor = MaterialTheme.colorScheme.error

    Column(modifier = modifier) {
        Row {
            LegendDot(color = inColor, label = "Stock in")
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(color = outColor, label = "Stock out")
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (points.size < 2) {
            Text(
                "Not enough data yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
            return@Column
        }

        val maxValue = (points.maxOf { maxOf(it.stockIn, it.stockOut) }).let { if (it <= 0.0) 1.0 else it }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            val stepX = size.width / (points.size - 1)
            fun pathFor(selector: (DayPoint) -> Double): Path {
                val path = Path()
                points.forEachIndexed { index, point ->
                    val x = index * stepX
                    val y = size.height - (selector(point) / maxValue * size.height).toFloat()
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                return path
            }

            drawPath(path = pathFor { it.stockIn }, color = inColor, style = Stroke(width = 4f))
            drawPath(path = pathFor { it.stockOut }, color = outColor, style = Stroke(width = 4f))

            points.forEachIndexed { index, point ->
                val x = index * stepX
                val yIn = size.height - (point.stockIn / maxValue * size.height).toFloat()
                val yOut = size.height - (point.stockOut / maxValue * size.height).toFloat()
                drawCircle(color = inColor, radius = 5f, center = Offset(x, yIn))
                drawCircle(color = outColor, radius = 5f, center = Offset(x, yOut))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                points.first().label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                points.last().label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color = color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Compact donut chart with a legend list, used for category/status
 * breakdowns -- again a small hand-rolled Canvas drawing rather than a new
 * charting dependency, since the app only needs a handful of slices.
 */
@Composable
fun DonutChart(slices: List<DonutSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.value }
    if (total <= 0) {
        Text(
            "No data",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = size.minDimension * 0.28f
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = 360f * slice.value / total
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                )
                startAngle += sweep
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            slices.forEach { slice ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color = slice.color),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${slice.label} · ${Math.round(100f * slice.value / total)}%",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

/**
 * Vertical bar chart for a short top-N list (e.g. most-moved products),
 * mirroring the "Top 3 Products" style bars on the web reference.
 */
@Composable
fun BarChart(entries: List<BarEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) {
        Text(
            "No movement data yet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 24.dp),
        )
        return
    }

    val maxValue = entries.maxOf { it.value }.let { if (it <= 0.0) 1.0 else it }
    val barColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier.height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        entries.forEach { entry ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight((entry.value / maxValue).toFloat().coerceIn(0.05f, 1f))
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(barColor),
                    )
                }
                Text(
                    entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

/**
 * A row of small circular arc gauges (percentage rings), mirroring the
 * "Profit Margin" style gauges on the web reference.
 */
@Composable
fun GaugeRow(entries: List<GaugeEntry>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly) {
        entries.forEach { entry ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val strokeWidth = 8f
                        drawArc(
                            color = entry.color.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        drawArc(
                            color = entry.color,
                            startAngle = -90f,
                            sweepAngle = 3.6f * entry.pct.coerceIn(0, 100),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                    Text("${entry.pct}%", style = MaterialTheme.typography.labelLarge)
                }
                Text(
                    entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}
