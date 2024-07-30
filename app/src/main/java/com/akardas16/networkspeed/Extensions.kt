package com.akardas16.networkspeed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit


@Composable
fun LifecycleOwner.ListenLifecycle(
    onCreate: () -> Unit = {},
    onStart: () -> Unit = {},
    onResume: () -> Unit = {},
    onStop: () -> Unit = {},
    onPause: () -> Unit = {},
    onDestroy: () -> Unit = {},
    onRemoved: () -> Unit = {},
) {
    DisposableEffect(key1 = this) {
        val observer = LifecycleEventObserver { source, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> onCreate()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_STOP -> onStop()
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_ANY -> {
                }

                Lifecycle.Event.ON_DESTROY -> onDestroy()

            }
        }
        this@ListenLifecycle.lifecycle.addObserver(observer)

        onDispose {
            onRemoved()
            this@ListenLifecycle.lifecycle.removeObserver(observer)
        }
    }
}






fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) = composed {

    val interactionSource = remember { MutableInteractionSource() }

    val animatable = remember {
        Animatable(1f)
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> animatable.animateTo(scaleDown)
                is PressInteraction.Release -> animatable.animateTo(1f)
                is PressInteraction.Cancel -> animatable.animateTo(1f)
            }
        }
    }

    then(
        Modifier
            .graphicsLayer {
                val scale = if (isEnabled) animatable.value else 1f
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (isEnabled) {
                    onClick()
                }

            })
}

private const val SECOND = 1
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR
private const val MONTH = 30 * DAY
private const val YEAR = 12 * MONTH

private fun currentDate(): Long {
    val calendar = Calendar.getInstance()
    return calendar.timeInMillis
}

fun Long.toTimeAgo(date:String): String {
    val time = this
    val now = currentDate()

    // convert back to second
    val diff = (now - time) / 1000

    return when {
        diff < MINUTE -> "Now" //getDate(time,"hh:mm")//"Just now"
        diff < 2 * MINUTE -> utcToLocal("HH:mm",date).toString()//getDate(time,"hh:mm")//"a minute\nago"
        diff < 60 * MINUTE -> utcToLocal("HH:mm",date).toString()
        diff < 2 * HOUR -> utcToLocal("HH:mm",date).toString()
        diff < 24 * HOUR -> "Today"
        diff < 2 * DAY -> "yesterday"
        diff < 30 * DAY -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 2 * MONTH -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 12 * MONTH -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 2 * YEAR -> utcToLocal("dd.MM.yyyy",date).toString()
        else -> utcToLocal("dd.MM.yyyy",date).toString()
    }
}

fun Long.toTimeAgoForList(date:String): String {
    val time = this
    val now = currentDate()

    // convert back to second
    val diff = (now - time) / 1000

    return when {
        diff < MINUTE -> utcToLocal("HH:mm",date).toString() //getDate(time,"hh:mm")//"Just now"
        diff < 2 * MINUTE -> utcToLocal("HH:mm",date).toString()//getDate(time,"hh:mm")//"a minute\nago"
        diff < 60 * MINUTE -> utcToLocal("HH:mm",date).toString()
        diff < 2 * HOUR -> utcToLocal("HH:mm",date).toString()
        diff < 24 * HOUR -> utcToLocal("HH:mm",date).toString()
        diff < 2 * DAY -> "yesterday"
        diff < 30 * DAY -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 2 * MONTH -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 12 * MONTH -> utcToLocal("dd.MM.yyyy",date).toString()
        diff < 2 * YEAR -> utcToLocal("dd.MM.yyyy",date).toString()
        else -> utcToLocal("dd.MM.yyyy",date).toString()
    }
}
fun String.getFirst2Words(): String {
    if (this.split(" ").size >=2){
        val sArr = this.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        var firstStrs = ""
        for (i in 0 until 2) firstStrs += sArr[i] + " "
        return firstStrs.trim { it <= ' ' }
    }else return this

}

@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}
fun Modifier.brushColor(colors:List<Color>) = this
    .graphicsLayer(alpha = 0.99f)
    .drawWithCache {
        val brush = Brush.horizontalGradient(colors)
        onDrawWithContent {
            drawContent()
            drawRect(brush, blendMode = BlendMode.SrcAtop)
        }
    }
fun Modifier.bounceWithLongClick(
    scaleDown: Float = 0.96f,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) = this.composed {

    val interactionSource = remember { MutableInteractionSource() }

    val animatable = remember {
        Animatable(1f)
    }

    val scope = rememberCoroutineScope()
    var job: Job? = null
    var isLongClick by remember { mutableStateOf(false) }


    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isLongClick = false
                    animatable.animateTo(scaleDown)
                    job = scope.launch {
                        delay(1000)
                        isLongClick = true
                        onLongClick()
                    }


                }
                is PressInteraction.Release -> {
                    animatable.animateTo(1f)
                    job?.cancel()

                }
                is PressInteraction.Cancel -> {
                    animatable.animateTo(1f)
                    job?.cancel()

                }
            }
        }
    }

    Modifier
        .graphicsLayer {
            val scale = animatable.value
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            if (isLongClick.not()) {
                onClick()
            }

        }
}
fun getDate(milliSeconds: Long, dateFormat: String?): String {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")


    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}

fun utcToLocal(dateFomratOutPut: String?, datesToConvert: String?): String? {
    var dateToReturn = datesToConvert
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")

    var gmt: Date? = null
    val sdfOutPutToSend = SimpleDateFormat(dateFomratOutPut, Locale.getDefault())
    sdfOutPutToSend.timeZone = TimeZone.getDefault()
    try {
        gmt = datesToConvert?.let { sdf.parse(it) }
        dateToReturn = gmt?.let { sdfOutPutToSend.format(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return dateToReturn
}

fun findDifferenceInSeconds(promotionEndDate: String = "2024-05-16T14:12:31.804Z"): Int {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val gmt = promotionEndDate.let { sdf.parse(it) }
    return TimeUnit.MILLISECONDS.toSeconds(gmt?.time?.minus(Date().time)!!).toInt()
}
@SuppressLint("HardwareIds")
fun Context.deviceID():String = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}

fun Context.findIntent(): Intent = this.findActivity().intent

// This function downloads a video from a URL and saves it to the cache
fun downloadAndCacheVideo(
    context: Context, url: String, fileName: String = "cached_video.mp4",
    onSuccess: () -> Unit, onFail: () -> Unit,
) {

    CoroutineScope(Dispatchers.IO).launch {
        // Create a file object for the cache directory
        val cacheDir = context.cacheDir
        val file = File(cacheDir, fileName)

        // Open a connection and download the video
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connect()

        // Check for successful download
        if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = urlConnection.inputStream
            val outputStream = FileOutputStream(file)

            // Write the video data to the cache file
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream.close()
            outputStream.close()
            Log.i("werwerwer23", "success video")
            onSuccess()
        } else {
            onFail()
            // Handle download error (optional)
            Log.i("werwerwer23", "Error downloading video")
        }

        urlConnection.disconnect()
    }

}

fun getCachedFileUri(context: Context, fileName: String = "cached_video.mp4"): Uri? {
    val file = File(context.cacheDir, fileName)
    return if (file.exists()) {
        Uri.fromFile(file)
    } else {
        null
    }
}



fun unselectedGradient() = Brush.horizontalGradient(
    arrayListOf(
        Color(0xFF282828),
        Color(0xFF282828)
    )
)
fun gradientMain():Brush = Brush.horizontalGradient(listOf( Color(0xFF007AFF),
    Color(0xFF51A3FC)
))


fun gradientMainVertical():Brush = Brush.verticalGradient(listOf( Color(0xFF007AFF),
    Color(0xFF51A3FC)
))

fun String.toLanguageName():String = Locale.getDefault().getDisplayLanguage(Locale(this))

fun ContentDrawScope.drawNeonStroke(radius: Dp, color: Color = Color.Magenta) {
    this.drawIntoCanvas {
        val paint =
            Paint().apply {
                style = PaintingStyle.Fill
                strokeWidth = 20f
            }

        val frameworkPaint =
            paint.asFrameworkPaint()


        this.drawIntoCanvas {
            frameworkPaint.color = color.copy(alpha = 0f).toArgb()
            frameworkPaint.setShadowLayer(
                radius.toPx(), 0f, 0f, color.copy(alpha = .5f).toArgb()
            )
            it.drawRoundRect(
                left = 0f,
                right = size.width,
                bottom = size.height,
                top = 0f,
                radiusY = radius.toPx(),
                radiusX = radius.toPx(),
                paint = paint
            )

        }
    }
}

fun promoTimerText(expireDate:String):String{
    val sdf = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        Locale.getDefault()
    )
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val gmt = sdf.parse(expireDate)
    val diffs: Long = gmt.time - Date().time
    val g = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    g.timeZone = TimeZone.getTimeZone("UTC")
    return g.format(Date(diffs))
}

fun isPromoTimerValid(expireDate:String):Boolean{
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val gmt = sdf.parse(expireDate)
    val diffs: Long = gmt.time - Date().time
    return diffs > 0

}

fun Context.isInternetAvailable(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val cap = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            return cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        else -> {
            val networks = cm.allNetworks
            for (n in networks) {
                val nInfo = cm.getNetworkInfo(n)
                if (nInfo != null && nInfo.isConnected) return true
            }
        }
    }
    return false
}
fun gradientBlue():Brush = Brush.verticalGradient(listOf( Color(0xFF0099FF),
    Color(0xFF00C6FF)
))

fun gradientBlueChild():Brush = Brush.verticalGradient(listOf(
    Color(0xFF34C759),
    Color(0xFF74FF98)
))

fun gradientDisable():Brush = Brush.horizontalGradient(listOf( Color(0x740099FF),
    Color(0x7300C6FF)
))

fun gradientUnselect():Brush = Brush.horizontalGradient(listOf( Color(0xFF171719),
    Color(0xFF171719)
))


fun gradientTransparent():Brush = Brush.horizontalGradient(listOf( Color(0x00171719),
    Color(0x00171719)
))

val shimmerColors = listOf(
    Color(0xFF007AFF).copy(1f),
    Color(0xFFAABDD1),
    Color(0xFF007AFF).copy(1f))

fun shimmerColors(isChildMode: Boolean):List<Color>{
    return if (isChildMode){
        listOf(
            Color(0xFF34C759).copy(1f),
            Color(0xFFAABDD1),
            Color(0xFF34C759).copy(1f))
    }else {
        listOf(
            Color(0xFF0099FF).copy(1f),
            Color(0xFFAABDD1),
            Color(0xFF0099FF).copy(1f)
        )
    }
}
fun radialColor():Brush = Brush.radialGradient(listOf(Color(0xFF102330),
    Color(0xFF171719)
))

fun Context.shareApp(){
    val shareText = Intent(Intent.ACTION_SEND)
    shareText.type = "text/plain"
    val dataToShare = "Download LingoJam Now\nhttp://play.google.com/store/apps/details?id=${this.packageName}"
    shareText.putExtra(Intent.EXTRA_TEXT, dataToShare)
    startActivity(Intent.createChooser(shareText, "Share Via"))
}

fun Context.rateUs(){
    val uri: Uri = Uri.parse("market://details?id=${this.packageName}")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
            Uri.parse("http://play.google.com/store/apps/details?id=com.cerasus.linguistic"))
        )
    }
}

fun fontDigital():FontFamily = FontFamily(Font(R.font.digital_regular))
fun fontDigital2():FontFamily = FontFamily(Font(R.font.technology_bold))

fun currentTime():String = getDate(System.currentTimeMillis(), "yyyy-MM-dd'T'HH:mm:ss.SSS")

fun Context.navigateToChatty(){
    val uri: Uri = Uri.parse("market://details?id=com.cerasus.aiexperts")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    // To count with Play market backstack, After pressing back button,
    // to taken back to our application, we need to add following flags to intent.
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=com.cerasus.aiexperts"))
        )
    }
}


@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }


@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }