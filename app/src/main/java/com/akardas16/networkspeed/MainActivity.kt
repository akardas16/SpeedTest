package com.akardas16.networkspeed

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akardas16.networkspeed.ui.theme.NetworkSpeedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkSpeedTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                val scope = rememberCoroutineScope()
                val viewModel by viewModels<MainViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()
                var brushColor by remember {
                    mutableStateOf(listOf(Color(0xFF05DEFA),Color(0xFF08B8EE)))
                }
                // Set up infinite animation
                val infiniteTransition = rememberInfiniteTransition("")
                val animValue by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
                    ),
                    label = ""
                )

                val hapticFeedback = LocalHapticFeedback.current

                val progressUI by viewModel.progressUI.collectAsStateWithLifecycle()
                val download by viewModel.download.collectAsStateWithLifecycle()
                val upload by viewModel.upload.collectAsStateWithLifecycle()
                val ping by viewModel.ping.collectAsStateWithLifecycle()
                val jitter by viewModel.jitter.collectAsStateWithLifecycle()
                val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
                val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()

                val servers by viewModel.servers.collectAsStateWithLifecycle()

                var showBottomSheet by remember {
                    mutableStateOf(false)
                }
                val context = LocalContext.current

                DisposableEffect(Unit) {
                    context.findActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    onDispose {
                        context.findActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                lifecycleOwner.ListenLifecycle(onCreate = {
                    scope.launch {
                        viewModel.getServerList{resp, err ->

                        }

                    }
                }, onRemoved = {viewModel.releaseResources()})


                Column(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14182B))
                    .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly) {

                    if (servers.isEmpty().not() && selectedServer != null){
                        Box(modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(64.dp)
                            .clickable {

                            }
                            .background(Color(0xFF283E5E), RoundedCornerShape(32.dp)),
                            contentAlignment = Alignment.Center){

                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {

                                Column {
                                    Text(text = selectedServer?.sponsor?.getFirst2Words()?.take(18) ?: "", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                    Text(text = selectedServer?.name ?: "", color = Color.Gray, fontWeight = FontWeight.Normal, fontSize = 12.sp)
                                }

                                Text(text = "${selectedServer?.distance} km", color = Color.Gray, fontWeight = FontWeight.Normal, fontSize = 12.sp)


                                Box(modifier = Modifier
                                    .bounceClick {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showBottomSheet = true
                                    }
                                    .background(
                                        brush = Brush.horizontalGradient(brushColor),
                                        shape = CircleShape
                                    ),
                                    contentAlignment = Alignment.Center){
                                    Text(text = "Change", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp))
                                }
                            }

                        }
                    }


                    CustomCircleProgressCut(count = progressUI,
                        gradientColors = brushColor)

                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(text = "Results", color = Color.White.copy(if (isRunning) animValue else 1f), fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center, fontSize = 18.sp)

                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly) {

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.arrow), contentDescription = "",
                                        modifier = Modifier
                                            .brushColor(brushColor)
                                            .size(18.dp))
                                    Text(text = "Download", color = Color(0x61DBDADA), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }

                                Text(
                                    text = if (download == 0f) "--" else String.format(Locale.getDefault(),"%.1f", download),
                                    modifier = Modifier.brushColor(brushColor),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.arrow), contentDescription = "",
                                        modifier = Modifier
                                            .brushColor(brushColor)
                                            .size(18.dp)
                                            .rotate(180f))
                                    Text(text = "Upload", color = Color(0x61DBDADA), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                                Text(
                                    text = if (upload == 0f) "--" else String.format(Locale.getDefault(),"%.1f", upload),
                                    modifier = Modifier.brushColor(brushColor),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp,Alignment.CenterHorizontally)) {

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.ic_ping), contentDescription = "", tint = Color.Green,
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(12.dp))
                                    Text(text = "Ping", color = Color(0x61DBDADA), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }

                                Text(
                                    text = if (ping == 0.0) "--" else String.format(Locale.getDefault(),"%.1f", ping),
                                    modifier = Modifier.brushColor(brushColor),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(id = R.drawable.ic_jitter), contentDescription = "", tint = Color(0xFFFFC046),
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(12.dp))
                                    Text(text = "Jitter", color = Color(0x61DBDADA), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                }
                                Text(
                                    text = if (jitter == 0.0) "--" else String.format(Locale.getDefault(),"%.1f", jitter),
                                    modifier = Modifier.brushColor(brushColor),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }

                    if (selectedServer != null){
                        Box(modifier = Modifier
                            .bounceClick(isEnabled = isRunning.not()) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    viewModel.downloadTest {
                                        brushColor = listOf(Color(0xFF4C73FF), Color(0xFF82BAFF))
                                        scope.launch {
                                            viewModel.uploadTest {
                                                scope.launch { viewModel.calculatePingAndJitter() }
                                            }
                                        }
                                    }
                                }
                            }
                            .background(
                                brush = Brush.horizontalGradient(brushColor),
                                shape = CircleShape
                            ),
                            contentAlignment = Alignment.Center){
                            Text(text = "Start Test", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp))
                        }
                    }

                }

                if (showBottomSheet){
                    ModalBottomSheet(onDismissRequest = { showBottomSheet = false },
                        shape = RoundedCornerShape(topEnd = 24.dp, topStart = 24.dp), windowInsets = WindowInsets(top = 0.dp, bottom = 0.dp),
                        containerColor = Color(0xFF14182B),modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f)) {

                        LazyColumn {
                            items(servers){item ->
                                Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.changeSelectedServer(item)
                                        showBottomSheet = false
                                    }) {
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween) {

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = item.sponsor?.getFirst2Words()?.take(18) ?: "", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            Text(text = item.name ?: "", color = Color.Gray, fontWeight = FontWeight.Normal, fontSize = 12.sp)
                                        }

                                        Text(text = "${item.distance} km", color = Color.Gray, fontWeight = FontWeight.Normal, fontSize = 12.sp,modifier = Modifier.padding(horizontal = 12.dp))


                                        RadioButton(selected = item.url == selectedServer?.url,
                                            colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan,unselectedColor = Color.Cyan),
                                            onClick = {
                                                viewModel.changeSelectedServer(item)
                                                showBottomSheet = false
                                            })
                                    }
                                    HorizontalDivider(thickness = 1.dp, color = Color.Gray.copy(0.3f))
                                }


                            }
                        }
                    }
                }


            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NetworkSpeedTheme {
        Greeting("Android")
    }
}