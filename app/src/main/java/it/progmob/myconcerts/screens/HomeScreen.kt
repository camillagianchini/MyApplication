package it.progmob.myconcerts.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import it.progmob.myconcerts.Concert
import it.progmob.myconcerts.R
import it.progmob.myconcerts.navigation.ScreenRoute
import it.progmob.myconcerts.ui.theme.MyApplicationTheme
import it.progmob.myconcerts.viewmodels.HomeViewModel
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val concerts by viewModel.concerts.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Define the desired height for the custom top bar
    // Based on your drawing, it needs to be significantly larger,
    // taking up almost half or more of the screen height.
    val customTopBarHeight = screenHeight / 4f // Adjusted fraction to make it very large,
    // as per your drawing, intersecting significantly.

    Scaffold(
        topBar = {
            // Custom Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(customTopBarHeight) // Use the greatly increased height
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp ,bottomStart = 200.dp, bottomEnd = 200.dp)) // Maintain strong curvature
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.top_bar_background), // Your image resource
                    contentDescription = "Top bar background",
                    contentScale = ContentScale.Crop, // Crop to fill the bounds
                    modifier = Modifier.fillMaxSize()
                )
                // Title Text
                Text(
                    text = "Welcome to MyConcerts",
                    style = MaterialTheme.typography.headlineMedium.copy( // Larger size
                        fontWeight = FontWeight.Bold, // Bolder font
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White // Set text color that contrasts with your image
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(Alignment.CenterVertically) // Center text vertically
                        .padding(16.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(ScreenRoute.AddConcert.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add concert")
            }
        }
    ) { paddingValues ->
        // Create a Box to layer the background image behind the content
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.sfondo_lista), // Your new background image
                contentDescription = "Background image",
                contentScale = ContentScale.Crop, // Crop to fill the bounds
                modifier = Modifier.fillMaxSize()
            )

            // Content of the Scaffold, now layered on top of the background image
            if (concerts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), // Use the padding provided by Scaffold
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No concerts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White // Adjust text color for better visibility on background
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), // Use the padding provided by Scaffold
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Padding for items inside LazyColumn
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(concerts) { concert ->
                        ConcertItem(
                            concert = concert,
                            onClick = {
                                navController.navigate(ScreenRoute.ConcertDetail.createRoute(concert.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConcertItem(concert: Concert, onClick: () -> Unit) {
    val daysRemaining = remember(concert.date.toDate().time) {
        calculateDaysRemaining(concert.date)
    }
    val isExpired = daysRemaining < 0

    val backgroundColor = if (isExpired) {
        Brush.verticalGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.Gray.copy(alpha = 0.4f)
            )
        )
    } else {
        val baseColor = remember(concert.id) {
            Color(
                red = (concert.id.hashCode() % 128 + 128) / 255f,
                green = (concert.id.hashCode() % 96 + 160) / 255f,
                blue = (concert.id.hashCode() % 64 + 192) / 255f
            )
        }
        Brush.horizontalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.8f),
                baseColor.copy(alpha = 0.4f)
            )
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp), // Keep this padding for card appearance
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = concert.artist.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 1.sp,
                            color = if (isExpired) Color.DarkGray else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = concert.location,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.SansSerif,
                            color = if (isExpired) Color.DarkGray.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    )
                }

                Text(
                    text = if (isExpired) "✗" else "$daysRemaining",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = if (isExpired) Color.DarkGray else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}


private fun calculateDaysRemaining(date: Timestamp): Long {
    val now = Calendar.getInstance()
    val concertDate = Calendar.getInstance().apply { time = date.toDate() }
    val diff = concertDate.timeInMillis - now.timeInMillis
    return diff / (1000 * 60 * 60 * 24) // Differenza in giorni
}


@Preview(showBackground = true)
@Composable
internal fun HomeScreenPreview() {
    MyApplicationTheme {
        // Create a fake NavController for the preview
        val navController = rememberNavController()
        // Create a HomeViewModel (ensure its constructor is preview-friendly)
        // or pass fake data directly if HomeScreen can accept it
        val fakeViewModel = HomeViewModel() // Assuming it provides some default/empty state
        HomeScreen(navController = navController, viewModel = fakeViewModel)
    }
}

// OR, if you modify HomeScreen to optionally accept a list:
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    previewConcerts: List<Concert>? = null // Optional param for preview
) {
    val concerts by viewModel.concerts.collectAsState()
    val displayConcerts = previewConcerts ?: concerts
    // ... rest of your HomeScreen code using displayConcerts
}

@Preview(showBackground = true)
@Composable
fun HomeScreenWithSampleDataPreview() {
    MyApplicationTheme {
        val sampleConcerts = listOf(
            Concert(id = "1", artist = "Preview Artist 1", location = "Preview Location 1", date = Timestamp.now()),
            Concert(id = "2", artist = "Preview Artist 2", location = "Preview Location 2", date = Timestamp.now())
        )
        HomeScreen(
            navController = rememberNavController(),
            // You might need a way to make the ViewModel use this previewConcerts
            // or have HomeScreen directly use previewConcerts when not null.
            // The simplest for preview is often to pass the data directly if the composable supports it.
            // For this to work best, HomeScreen would need to be adapted,
            // or you'd create a simpler preview that doesn't rely on the full ViewModel.
            viewModel = HomeViewModel(), // This might still be an issue if ViewModel init is problematic
            previewConcerts = sampleConcerts // If HomeScreen is adapted
        )
    }
}