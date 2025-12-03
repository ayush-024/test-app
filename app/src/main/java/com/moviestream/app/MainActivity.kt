package com.moviestream.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moviestream.app.ui.theme.MovieStreamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        enableEdgeToEdge()
        
        setContent {
            LaunchedEffect(Unit) {
                delay(1500)
                keepSplashScreen = false
            }
            
            MovieStreamTheme {
                MainApp()
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        containerColor = Color(0xFF0D0D0D),
        bottomBar = {
            ExpressiveBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { 
                    selectedTab = it
                    when (it) {
                        0 -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        1 -> navController.navigate("movies") { popUpTo("home") }
                        2 -> navController.navigate("tvshows") { popUpTo("home") }
                        3 -> navController.navigate("favorites") { popUpTo("home") }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("movies") { MoviesScreen(navController) }
            composable("tvshows") { TvShowsScreen(navController) }
            composable("favorites") { FavoritesScreen(navController) }
            composable("search") { SearchScreen(navController) }
            composable(
                "detail/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "movie"
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetailScreen(navController, type, id)
            }
        }
    }
}
@Composable
fun ExpressiveBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        Triple(Icons.Filled.Home, Icons.Outlined.Home, "Home"),
        Triple(Icons.Filled.Movie, Icons.Outlined.Movie, "Movies"),
        Triple(Icons.Filled.Tv, Icons.Outlined.Tv, "TV Shows"),
        Triple(Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "Favorites")
    )
    
    Surface(
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            items.forEachIndexed { index, (filledIcon, outlinedIcon, label) ->
                val isSelected = selectedTab == index
                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.5f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                )
                
                Box(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) Color(0xFFD0BCFF).copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = label,
                            tint = if (isSelected) Color(0xFFD0BCFF) else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            Text(
                                text = label,
                                color = Color(0xFFD0BCFF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val scrollState = rememberLazyListState()
    
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ExpressiveSearchBar(
                onClick = { navController.navigate("search") }
            )
        }
        
        item {
            FeaturedCarousel(navController)
        }
        
        item {
            ContentSection(
                title = "Trending Now 🔥",
                navController = navController
            )
        }
        
        item {
            ContentSection(
                title = "Popular Movies",
                navController = navController
            )
        }
        
        item {
            ContentSection(
                title = "Top Rated TV Shows",
                navController = navController,
                isTV = true
            )
        }
        
        item {
            ContentSection(
                title = "Upcoming",
                navController = navController
            )
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
@Composable
fun ExpressiveSearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF252525),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFFD0BCFF),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Search movies, TV shows...",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun FeaturedCarousel(navController: NavHostController) {
    val sampleMovies = listOf(
        Triple(1, "Dune: Part Two", "https://image.tmdb.org/t/p/w780/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg"),
        Triple(2, "Oppenheimer", "https://image.tmdb.org/t/p/w780/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),
        Triple(3, "Poor Things", "https://image.tmdb.org/t/p/w780/kCGlIMHnOm8JPXq3rXM6c5wMxcT.jpg")
    )
    
    val pagerState = rememberPagerState { sampleMovies.size }
    
    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) { page ->
            val movie = sampleMovies[page]
            FeaturedCard(
                id = movie.first,
                title = movie.second,
                imageUrl = movie.third,
                navController = navController
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(sampleMovies.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFFD0BCFF)
                            else Color(0xFF4A4A4A)
                        )
                        .animateContentSize()
                )
            }
        }
    }
}
@Composable
fun HorizontalPager(
    state: PagerState,
    modifier: Modifier = Modifier,
    pageSpacing: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable (Int) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(pageSpacing)
    ) {
        items(state.pageCount) { page ->
            Box(modifier = Modifier.fillParentMaxWidth(0.9f)) {
                content(page)
            }
        }
    }
}
@Composable
fun rememberPagerState(pageCount: () -> Int): PagerState {
    return remember { PagerState(pageCount()) }
}
class PagerState(val pageCount: Int) {
    var currentPage by mutableIntStateOf(0)
}
@Composable
fun FeaturedCard(
    id: Int,
    title: String,
    imageUrl: String,
    navController: NavHostController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .clickable { navController.navigate("detail/movie/$id") },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 300f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(
                        onClick = { navController.navigate("detail/movie/$id") },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF1A1A1A)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Watch Now", fontWeight = FontWeight.SemiBold)
                    }
                    
                    OutlinedIconButton(
                        onClick = { },
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Add to list",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContentSection(
    title: String,
    navController: NavHostController,
    isTV: Boolean = false
) {
    val sampleItems = if (isTV) {
        listOf(
            Triple(1, "Breaking Bad", "https://image.tmdb.org/t/p/w342/ggFHVNu6YYI5L9pCfOacjizRGt.jpg"),
            Triple(2, "Game of Thrones", "https://image.tmdb.org/t/p/w342/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg"),
            Triple(3, "Stranger Things", "https://image.tmdb.org/t/p/w342/x2LSRK2Cm7MZhjluni1msVJ3wDF.jpg"),
            Triple(4, "The Last of Us", "https://image.tmdb.org/t/p/w342/uKvVjHNqB5VmOrdxqAt2F7J78ED.jpg"),
            Triple(5, "The Witcher", "https://image.tmdb.org/t/p/w342/7vjaCdMw15FEbXyLQTVa04URsPm.jpg")
        )
    } else {
        listOf(
            Triple(1, "Dune: Part Two", "https://image.tmdb.org/t/p/w342/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg"),
            Triple(2, "Oppenheimer", "https://image.tmdb.org/t/p/w342/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),
            Triple(3, "Poor Things", "https://image.tmdb.org/t/p/w342/kCGlIMHnOm8JPXq3rXM6c5wMxcT.jpg"),
            Triple(4, "Wonka", "https://image.tmdb.org/t/p/w342/qhb1qOilapbapxWQn9jtRCMwXJF.jpg"),
            Triple(5, "Aquaman 2", "https://image.tmdb.org/t/p/w342/7lTnXOy0iNtBAdRP3TZvaKJ77F6.jpg")
        )
    }
    
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = { }) {
                Text(
                    text = "See All",
                    color = Color(0xFFD0BCFF),
                    fontSize = 14.sp
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleItems) { (id, itemTitle, imageUrl) ->
                ContentCard(
                    id = id,
                    title = itemTitle,
                    imageUrl = imageUrl,
                    isTV = isTV,
                    navController = navController
                )
            }
        }
    }
}
@Composable
fun ContentCard(
    id: Int,
    title: String,
    imageUrl: String,
    isTV: Boolean,
    navController: NavHostController
) {
    val type = if (isTV) "tv" else "movie"
    
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { navController.navigate("detail/$type/$id") }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252525))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
@Composable
fun MoviesScreen(navController: NavHostController) {
    val movies = listOf(
        Triple(1, "Dune: Part Two", "https://image.tmdb.org/t/p/w342/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg"),
        Triple(2, "Oppenheimer", "https://image.tmdb.org/t/p/w342/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),
        Triple(3, "Poor Things", "https://image.tmdb.org/t/p/w342/kCGlIMHnOm8JPXq3rXM6c5wMxcT.jpg"),
        Triple(4, "Wonka", "https://image.tmdb.org/t/p/w342/qhb1qOilapbapxWQn9jtRCMwXJF.jpg"),
        Triple(5, "Aquaman 2", "https://image.tmdb.org/t/p/w342/7lTnXOy0iNtBAdRP3TZvaKJ77F6.jpg"),
        Triple(6, "Migration", "https://image.tmdb.org/t/p/w342/ldfCF9RhR40mppkzmftxapaHeTo.jpg")
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Movies",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(movies) { (id, title, imageUrl) ->
                ContentCard(
                    id = id,
                    title = title,
                    imageUrl = imageUrl,
                    isTV = false,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun TvShowsScreen(navController: NavHostController) {
    val tvShows = listOf(
        Triple(1, "Breaking Bad", "https://image.tmdb.org/t/p/w342/ggFHVNu6YYI5L9pCfOacjizRGt.jpg"),
        Triple(2, "Game of Thrones", "https://image.tmdb.org/t/p/w342/u3bZgnGQ9T01sWNhyveQz0wH0Hl.jpg"),
        Triple(3, "Stranger Things", "https://image.tmdb.org/t/p/w342/x2LSRK2Cm7MZhjluni1msVJ3wDF.jpg"),
        Triple(4, "The Last of Us", "https://image.tmdb.org/t/p/w342/uKvVjHNqB5VmOrdxqAt2F7J78ED.jpg"),
        Triple(5, "The Witcher", "https://image.tmdb.org/t/p/w342/7vjaCdMw15FEbXyLQTVa04URsPm.jpg"),
        Triple(6, "Wednesday", "https://image.tmdb.org/t/p/w342/9PFonBhy4cQy7Jz20NpMygczOkv.jpg")
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "TV Shows",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tvShows) { (id, title, imageUrl) ->
                ContentCard(
                    id = id,
                    title = title,
                    imageUrl = imageUrl,
                    isTV = true,
                    navController = navController
                )
            }
        }
    }
}
@Composable
fun FavoritesScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No favorites yet",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF252525),
                    unfocusedContainerColor = Color(0xFF252525),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFFD0BCFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFFD0BCFF)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            )
        }
        
        if (searchQuery.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Search for movies or TV shows",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavHostController,
    type: String,
    id: Int
) {
    val context = LocalContext.current
    val isTV = type == "tv"
    var selectedSeason by remember { mutableIntStateOf(1) }
    var showSeasonSheet by remember { mutableStateOf(false) }
    var showSourceSheet by remember { mutableStateOf(false) }
    
    val sampleDetails = if (isTV) {
        mapOf(
            "title" to "Breaking Bad",
            "overview" to "A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine in order to secure his family's future.",
            "backdrop" to "https://image.tmdb.org/t/p/w1280/tsRy63Mu5cu8etL1X7ZLyf7UP1M.jpg",
            "poster" to "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
            "rating" to "9.5",
            "year" to "2008-2013",
            "seasons" to "5"
        )
    } else {
        mapOf(
            "title" to "Dune: Part Two",
            "overview" to "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen while on a path of revenge against the conspirators who destroyed his family.",
            "backdrop" to "https://image.tmdb.org/t/p/w1280/xOMo8BRK7PfcJv9JCnx7s5hj0PX.jpg",
            "poster" to "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg",
            "rating" to "8.3",
            "year" to "2024",
            "runtime" to "166 min"
        )
    }
    
    val episodes = listOf(
        mapOf("number" to "1", "title" to "Pilot", "duration" to "58m"),
        mapOf("number" to "2", "title" to "Cat's in the Bag...", "duration" to "48m"),
        mapOf("number" to "3", "title" to "...And the Bag's in the River", "duration" to "48m"),
        mapOf("number" to "4", "title" to "Cancer Man", "duration" to "48m"),
        mapOf("number" to "5", "title" to "Gray Matter", "duration" to "48m"),
        mapOf("number" to "6", "title" to "Crazy Handful of Nothin'", "duration" to "47m"),
        mapOf("number" to "7", "title" to "A No-Rough-Stuff-Type Deal", "duration" to "47m")
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(sampleDetails["backdrop"])
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF0D0D0D)
                                    ),
                                    startY = 200f
                                )
                            )
                    )
                    
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
            
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-60).dp)
                ) {
                    Row {
                        Card(
                            modifier = Modifier
                                .width(120.dp)
                                .height(180.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(sampleDetails["poster"])
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.padding(top = 60.dp)
                        ) {
                            Text(
                                text = sampleDetails["title"] ?: "",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = " ${sampleDetails["rating"]}",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " • ${sampleDetails["year"]}",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                if (!isTV) {
                                    Text(
                                        text = " • ${sampleDetails["runtime"]}",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            if (isTV) {
                                Text(
                                    text = "${sampleDetails["seasons"]} Seasons",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSourceSheet = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF1A1A1A)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isTV) "Watch S${selectedSeason}E1" else "Watch Now",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        OutlinedIconButton(
                            onClick = { },
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                        ) {
                            Icon(
                                Icons.Outlined.FavoriteBorder,
                                contentDescription = "Add to favorites",
                                tint = Color(0xFFD0BCFF)
                            )
                        }
                        
                        OutlinedIconButton(
                            onClick = { },
                            border = BorderStroke(1.dp, Color(0xFFD0BCFF))
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color(0xFFD0BCFF)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Overview",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = sampleDetails["overview"] ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            if (isTV) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = (-40).dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Episodes",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            OutlinedButton(
                                onClick = { showSeasonSheet = true },
                                border = BorderStroke(1.dp, Color(0xFFD0BCFF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Season $selectedSeason",
                                    color = Color(0xFFD0BCFF)
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFD0BCFF)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                items(episodes) { episode ->
                    EpisodeCard(
                        episodeNumber = episode["number"] ?: "",
                        title = episode["title"] ?: "",
                        duration = episode["duration"] ?: "",
                        onClick = { showSourceSheet = true },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .offset(y = (-40).dp)
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
        
        if (showSeasonSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSeasonSheet = false },
                containerColor = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Select Season",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    (1..5).forEach { season ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSeason = season
                                    showSeasonSheet = false
                                },
                            color = if (selectedSeason == season) 
                                Color(0xFFD0BCFF).copy(alpha = 0.2f) 
                            else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Season $season",
                                    color = if (selectedSeason == season) 
                                        Color(0xFFD0BCFF) 
                                    else Color.White,
                                    fontSize = 16.sp
                                )
                                
                                if (selectedSeason == season) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFD0BCFF)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        if (showSourceSheet) {
            SourceSelectionSheet(
                onDismiss = { showSourceSheet = false },
                onSourceSelected = { source ->
                    showSourceSheet = false
                    // Navigate to player
                    val intent = android.content.Intent(
                        context,
                        Class.forName("com.moviestream.app.ui.player.PlayerActivity")
                    ).apply {
                        putExtra("title", sampleDetails["title"])
                        putExtra("source", source)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun EpisodeCard(
    episodeNumber: String,
    title: String,
    duration: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF252525),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = episodeNumber,
                        color = Color(0xFFD0BCFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = duration,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Filled.PlayCircle,
                    contentDescription = "Play",
                    tint = Color(0xFFD0BCFF),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSelectionSheet(
    onDismiss: () -> Unit,
    onSourceSelected: (String) -> Unit
) {
    val sources = listOf(
        mapOf("name" to "StreamFlow", "quality" to "1080p", "server" to "Server 1"),
        mapOf("name" to "VidCloud", "quality" to "720p", "server" to "Server 2"),
        mapOf("name" to "MixDrop", "quality" to "1080p", "server" to "Server 3"),
        mapOf("name" to "Filemoon", "quality" to "4K", "server" to "Server 4")
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Select Source",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            sources.forEach { source ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onSourceSelected(source["name"] ?: "") },
                    color = Color(0xFF252525),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = Color(0xFFD0BCFF),
                                modifier = Modifier.size(40.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = source["name"] ?: "",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = source["server"] ?: "",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD0BCFF).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = source["quality"] ?: "",
                                color = Color(0xFFD0BCFF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
