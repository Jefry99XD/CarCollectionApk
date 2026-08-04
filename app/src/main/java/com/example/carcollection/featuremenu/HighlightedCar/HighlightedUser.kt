package com.example.carcollection.featuremenu.HighlightedCar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.carcollection.featurecar.data.CarMethods

private const val TAG = "TubaDelMes"

// Data class para el usuario del mes
data class UserOfTheMonth(
    val userId: String = "",
    val username: String = "",
    val photoUrl: String = "",
    val carCount: Int = 0
)

// Static singleton cache — persists across recompositions and scroll
object TubaDelMesCache {
    @Volatile private var cachedFeaturedUser: UserOfTheMonth? = null
    @Volatile private var cachedLeaderboard: List<UserOfTheMonth> = emptyList()
    @Volatile private var isLoaded = false
    
    fun isReady(): Boolean = isLoaded
    
    fun getFeaturedUser(): UserOfTheMonth? = cachedFeaturedUser
    
    fun getLeaderboard(): List<UserOfTheMonth> = cachedLeaderboard
    
    fun setData(featured: UserOfTheMonth?, leaderboard: List<UserOfTheMonth>) {
        cachedFeaturedUser = featured
        cachedLeaderboard = leaderboard
        isLoaded = true
    }
    
    fun invalidate() {
        cachedFeaturedUser = null
        cachedLeaderboard = emptyList()
        isLoaded = false
    }
}

// Static instance to prevent recreations
private val carMethodsInstance = CarMethods()

@Composable
fun TubaDelMesScreen() {
    var featuredUser by remember { mutableStateOf<UserOfTheMonth?>(null) }
    var leaderboard by remember { mutableStateOf<List<UserOfTheMonth>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // If cache is ready, use cached data
        if (TubaDelMesCache.isReady()) {
            featuredUser = TubaDelMesCache.getFeaturedUser()
            leaderboard = TubaDelMesCache.getLeaderboard()
            isLoading = false
            return@LaunchedEffect
        }
        
        try {
            // Try current month first
            var result = carMethodsInstance.getUserOfTheMonth(monthOffset = 0)
            
            // If no data in current month, try previous month
            val resultData = result.getOrNull()
            if (resultData == null) {
                Log.d(TAG, "No users in current month, trying previous month as fallback")
                result = carMethodsInstance.getUserOfTheMonth(monthOffset = -1)
            }
            
            Log.d(TAG, "Featured user result: success=${result.isSuccess}")
            
            var finalFeaturedUser: UserOfTheMonth? = null
            if (result.isSuccess) {
                val data = result.getOrNull()
                if (data != null) {
                    finalFeaturedUser = UserOfTheMonth(
                        userId = data["userId"].toString(),
                        username = data["username"].toString(),
                        photoUrl = data["photoUrl"].toString(),
                        carCount = (data["carCount"] as? Number)?.toInt() ?: 0
                    )
                    Log.d(TAG, "Featured user: ${finalFeaturedUser.username}")
                }
            }
            
            // Get top 5 leaderboard
            val leaderboardResult = carMethodsInstance.getMonthlyLeaderboard(monthOffset = 0, limit = 5)
            var finalLeaderboard: List<UserOfTheMonth> = emptyList()
            if (leaderboardResult.isSuccess) {
                finalLeaderboard = leaderboardResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Leaderboard loaded: ${finalLeaderboard.size} users")
            }
            
            // Cache the results for next scroll recompositions
            TubaDelMesCache.setData(finalFeaturedUser, finalLeaderboard)
            
            featuredUser = finalFeaturedUser
            leaderboard = finalLeaderboard
            isLoading = false
        } catch (e: Exception) {
            Log.e(TAG, "Exception in TubaDelMesScreen", e)
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Only render if we have at least featured user or leaderboard data
    if (featuredUser == null && leaderboard.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ─── Título y descripción ───
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Tuba del mes",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "El tuba que ha agregado mas carros este mes es",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ─── Featured User Card ───
        if (featuredUser != null) {
            val user = featuredUser!!
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // ─── Foto de perfil ───
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = user.username,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = user.username.firstOrNull()?.toString() ?: "U",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    fontSize = 48.sp
                                )
                            }
                        }

                        Box(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.username,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Box(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${user.carCount} ${if (user.carCount == 1) "auto" else "autos"} agregados",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    // Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "🏆",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // ─── Leaderboard ───
        if (leaderboard.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Top 5 Tubas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                leaderboard.forEachIndexed { index, user ->
                    LeaderboardItem(user, position = index + 1)
                }
            }
        }
    }
}


@Composable
fun LeaderboardItem(user: UserOfTheMonth, position: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (position) {
                1 -> MaterialTheme.colorScheme.primaryContainer
                2 -> MaterialTheme.colorScheme.secondaryContainer
                3 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Position badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // User info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${user.carCount} carros",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Car count badge
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = user.carCount.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

