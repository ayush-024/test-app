package com.moviestream.app.ui.player
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.moviestream.app.ui.theme.MovieStreamTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
@AndroidEntryPoint
class PlayerActivity : ComponentActivity() {
    
    private var player: ExoPlayer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fullscreen immersive mode
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        
        val title = intent.getStringExtra("title") ?: "Unknown"
        val source = intent.getStringExtra("source") ?: ""
        
        setContent {
            MovieStreamTheme {
                PlayerScreen(
                    title = title,
                    onBack = { finish() }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    title: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var showSourceSheet by remember { mutableStateOf(false) }
    var showSubtitleSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableIntStateOf(0) }
    var selectedSubtitle by remember { mutableStateOf("English") }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isBuffering by remember { mutableStateOf(true) }
    
    // Sample video sources from the API structure
    val videoSources = listOf(
        VideoSource(
            name = "StreamFlow",
            quality = "1080p",
            url = "https://sunmelt27.xyz/file2/sample.m3u8",
            referer = "https://videostr.net/",
            origin = "https://videostr.net"
        ),
        VideoSource(
            name = "VidCloud",
            quality = "720p",
            url = "https://example.com/video2.m3u8",
            referer = "",
            origin = ""
        )
    )
    
    val subtitles = listOf(
        Subtitle("Arabic", "https://example.com/ar.vtt"),
        Subtitle("Danish", "https://example.com/da.vtt"),
        Subtitle("Dutch", "https://example.com/nl.vtt"),
        Subtitle("English", "https://example.com/en.vtt"),
        Subtitle("French", "https://example.com/fr.vtt"),
        Subtitle("Hindi", "https://example.com/hi.vtt"),
        Subtitle("Italian", "https://example.com/it.vtt"),
        Subtitle("Russian", "https://example.com/ru.vtt"),
        Subtitle("Spanish", "https://example.com/es.vtt")
    )
    
    // ExoPlayer setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Create data source with headers
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to videoSources[0].referer,
                        "Origin" to videoSources[0].origin
                    )
                )
            
            // Use sample HLS stream for demo
            val mediaItem = MediaItem.Builder()
                .setUri("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8")
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            
            setMediaSource(hlsMediaSource)
            prepare()
            playWhenReady = true
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
                
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            })
        }
    }
    
    // Update position
    LaunchedEffect(Unit) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }
    
    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { showControls = !showControls }
    ) {
        // Video Player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (isBuffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFD0BCFF),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(56.dp)
                )
            }
        }
        
        // Controls overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                // Top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = videoSources[selectedSource].name,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showSourceSheet = true }) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = "Sources",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        
                        IconButton(onClick = { showSubtitleSheet = true }) {
                            Icon(
                                Icons.Default.Subtitles,
                                contentDescription = "Subtitles",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
                
                // Center controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { exoPlayer.seekTo(exoPlayer.currentPosition - 10000) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Play/Pause
                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                Color(0xFFD0BCFF).copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    // Forward 10s
                    IconButton(
                        onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10000) },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                // Bottom controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    // Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        
                        Slider(
                            value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                            onValueChange = { 
                                exoPlayer.seekTo((it * duration).toLong())
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD0BCFF),
                                activeTrackColor = Color(0xFFD0BCFF),
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quality and speed indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD0BCFF).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = videoSources[selectedSource].quality,
                                color = Color(0xFFD0BCFF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        
                        if (playbackSpeed != 1f) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFD0BCFF).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = Color(0xFFD0BCFF),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        IconButton(
                            onClick = { /* Toggle fullscreen */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
