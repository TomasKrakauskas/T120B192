import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lw1.api.Api
import com.example.lw1.api.getReadings
import com.example.lw1.models.Coordinates
import com.example.lw1.db.ReadingDao
import kotlinx.coroutines.launch


@Composable
fun Map(readingDao: ReadingDao, api: Api) {
    val scope = rememberCoroutineScope()

    var hasFinished by remember { mutableStateOf(false) }

    val readings by readingDao.getAll().collectAsState(initial = emptyList())

    val cells = remember(readings) { readings.map { r -> Coordinates(r.x, r.y) } }

    // Compute min/max only when readings changes
    val (xMin, xMax, yMin, yMax) = remember(readings) {
        val xMin = readings.minOfOrNull { it.x } ?: 0
        val xMax = readings.maxOfOrNull { it.x } ?: 0
        val yMin = readings.minOfOrNull { it.y } ?: 0
        val yMax = readings.maxOfOrNull { it.y } ?: 0
        arrayOf(xMin, xMax, yMin, yMax)
    }.map { it }

    if( !hasFinished ) {
        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
        ) {
            if(readings.isEmpty())
                Text("No readings loaded. Please fetch readings", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            else
                Text("Current Readings: ${readings.size}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    enabled = !loading,
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                getReadings(readingDao, api)
                            } catch (t: Throwable) {
                                error = t.localizedMessage ?: t.toString()
                            } finally {
                                loading = false
                                hasFinished = true
                            }
                        }
                    }
                ) { Text(if (loading) "Updating..." else "Fetch") }
            }

            if (loading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(108.dp), strokeWidth= 16.dp)
                }
            }
            error?.let { Text("Error: $it") }

        }

    } else {
        MapCanvas(
            xMin = xMin,
            xMax = xMax,
            yMin = yMin,
            yMax = yMax,
            activeCells = cells,
            cellPx = 72f
        )
    }


}


@Composable
fun MapCanvas(
    xMin: Int,
    xMax: Int,
    yMin: Int,
    yMax: Int,
    activeCells: List<Coordinates>,
    cellPx: Float
) {
    // how many columns/rows total in that world window
    val cols = (xMax - xMin + 1)
    val rows = (yMax - yMin + 1)

    // total size in pixels
    val widthPx = cols * cellPx
    val heightPx = rows * cellPx

    val textMeasurer = rememberTextMeasurer()

    val contentHeightDp = LocalDensity.current.run { (heightPx+cellPx).toDp() }
    val vScroll = rememberScrollState()

    Box(Modifier.fillMaxSize().verticalScroll(vScroll)) {
        Canvas(Modifier.padding(8.dp).fillMaxSize().height(contentHeightDp)) {
            val linesWidthPx = 1.dp.toPx()
            val lineColor = Color.DarkGray

            // background
            drawRect(
                color = Color.Red,
                alpha = 0.4f,
                topLeft = Offset(cellPx, cellPx),
                size = Size(widthPx, heightPx)
            )

            // draw vertical lines & x labels
            repeat(cols) { i ->
                val x = i + xMin
                val startX = cellPx * (i + 1)
                drawLine(
                    lineColor,
                    start = Offset(startX, 0f),
                    end = Offset(startX, heightPx+cellPx),
                    strokeWidth = linesWidthPx
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(x.toString()),
                    ),
                    topLeft = Offset(startX + cellPx / 4f, cellPx / 4f)
                )
            }
            // draw horizontal lines & y labels
            repeat(rows) { i ->
                val y = i + yMin
                val startY = cellPx * (i + 1)
                drawLine(
                    lineColor,
                    start = Offset(0f, startY),
                    end = Offset(widthPx+cellPx, startY),
                    strokeWidth = linesWidthPx
                )
                drawText(
                    textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(y.toString()),
                    ),
                    topLeft = Offset(cellPx / 4f, startY + cellPx / 4f)
                )
            }

            for (cell in activeCells) {
                val x = cell.x - xMin
                val y = cell.y - yMin

                drawRect(
                    color = Color.Green,
                    alpha = 0.6f,
                    topLeft = Offset(cellPx + x * cellPx, cellPx + y * cellPx),
                    size = Size(cellPx, cellPx)
                )
            }
        }
    }
}

