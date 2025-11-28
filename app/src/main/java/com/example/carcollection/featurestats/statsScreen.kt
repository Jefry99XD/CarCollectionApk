package com.example.carcollection.featurestats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.bar.SimpleBarDrawer
import com.github.tehras.charts.line.LineChart
import com.github.tehras.charts.line.LineChartData
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData

// ----------------------------------------------------
// DATA CLASS para representar estadística procesada
// ----------------------------------------------------
data class StatItem(
    val label: String,
    val value: Int,
    val color: Color
)

// ----------------------------------------------------
// Categorías disponibles
// ----------------------------------------------------
enum class StatsCategory(val displayName: String) {
    BRAND("Carros por marca"),
    YEAR("Carros por año"),
    COLOR("Carros por color"),
    TYPE("Carros por tipo"),
    TAGS("Carros por tag"),
    CREATED_AT("Carros creados por mes")
}

// Tipos de gráficos disponibles
enum class ChartType(val display: String) {
    PIE("Pie Chart"),
    BAR("Bar Chart"),
    LINE("Line Chart")
}

// ----------------------------------------------------
// PANTALLA PRINCIPAL
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsCategoryScreen(
    selectedCategory: StatsCategory,
    viewModel: StatsViewModel,
    onBackClick: () -> Unit
) {
    // Datos reales procesados desde Firebase
    val cars by viewModel.cars.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val stats = remember(cars, selectedCategory) {
        viewModel.generateStats(selectedCategory)
    }
    var selectedChart by remember { mutableStateOf(ChartType.PIE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedCategory.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (stats.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No hay datos disponibles para esta categoría",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // -------------------------
                // SELECTOR DE TIPO DE GRAFICO
                // -------------------------
                item {
                    ChartSelector(
                        selected = selectedChart,
                        onSelect = { selectedChart = it }
                    )
                }

                // -------------------------
                // GRAFICO DINÁMICO
                // -------------------------
                item {
                    StatsChart(stats = stats, type = selectedChart)
                }

                // -------------------------
                // TABLA DE ESTADÍSTICAS
                // -------------------------
                item {
                    StatsTable(stats)
                }
            }
        }
    }
}

// ----------------------------------------------------
// SELECTOR DE GRÁFICO
// ----------------------------------------------------
@Composable
fun ChartSelector(selected: ChartType, onSelect: (ChartType) -> Unit) {
    Column {
        Text(
            "Tipo de gráfico",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartType.values().forEach { type ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    label = { Text(type.display) }
                )
            }
        }
    }
}

// ----------------------------------------------------
// TABLA CON COLORES
// ----------------------------------------------------
@Composable
fun StatsTable(stats: List<StatItem>) {
    Column {
        Text(
            "Estadísticas",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        stats.forEachIndexed { index, stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = stat.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(stat.color, RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("${index + 1}. ${stat.label}")
                }

                Text(stat.value.toString(), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

// ----------------------------------------------------
// GRAFICOS
// ----------------------------------------------------
@Composable
fun StatsChart(stats: List<StatItem>, type: ChartType) {
    when (type) {
        ChartType.PIE -> StatsPieChart(stats)
        ChartType.BAR -> StatsBarChart(stats)
        ChartType.LINE -> StatsLineChart(stats)
    }
}

// -------------------- PIE CHART ---------------------
@Composable
fun StatsPieChart(stats: List<StatItem>) {
    val pieData = stats.map {
        PieChartData.Slice(
            value = it.value.toFloat(),
            color = it.color
        )
    }

    PieChart(
        pieChartData = PieChartData(pieData),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )
}

// -------------------- BAR CHART ---------------------
@Composable
fun StatsBarChart(stats: List<StatItem>) {
    val entries = stats.map {
        BarChartData.Bar(
            label = "", // Hide labels on bar chart
            value = it.value.toFloat(),
            color = it.color
        )
    }

    BarChart(
        barChartData = BarChartData(entries),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        barDrawer = SimpleBarDrawer()
    )
}

// -------------------- LINE CHART ---------------------
// -------------------- LINE CHART ---------------------
@Composable
fun StatsLineChart(stats: List<StatItem>) {
    val points = stats.mapIndexed { index, stat ->
        LineChartData.Point(
            value = stat.value.toFloat(),
            label = (index + 1).toString()
        )
    }

    val color = Color(0xFF4A90E2)

    LineChart(
        // Cambia 'lines' por 'linesChartData'
        linesChartData = listOf(
            LineChartData(
                points = points,
                lineDrawer = com.github.tehras.charts.line.renderer.line.SolidLineDrawer(color = color)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )
}



