package com.akardas16.networkspeed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CustomCircleProgressCut(count:Float, max:Float = 200f,
                            foregroundColor: Color? = null, gradientColors:List<Color> = listOf(Color(
        0xFF05DEFA
    ),Color(0xFF08B8EE)
    ), backgroundColor:Color = Color(0x32DBDADA), size: Dp = 300.dp) {



    val animatedIndicatorValue = remember {
        androidx.compose.animation.core.Animatable(initialValue = (count * (240 / max)))
    }


    LaunchedEffect(key1 = count) {
        val newValue = (count * (240 / max))
        animatedIndicatorValue.animateTo(
            newValue,
            animationSpec = spring(
                Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        Modifier
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Column(modifier = Modifier
            .size(size)
            .padding(size / 10)
            .drawBehind {
                drawArc(
                    color = backgroundColor, startAngle = 150f,
                    sweepAngle = 240f, useCenter = false,
                    style = Stroke(width = 48f, cap = StrokeCap.Round)
                )
            }
            .drawBehind {
                if (foregroundColor == null) {
                    drawArc(
                        brush = Brush.horizontalGradient(gradientColors), startAngle = 150f,
                        sweepAngle = animatedIndicatorValue.value, useCenter = false,
                        style = Stroke(width = 48f, cap = StrokeCap.Round)
                    )
                } else {
                    drawArc(
                        color = foregroundColor, startAngle = 150f,
                        sweepAngle = animatedIndicatorValue.value, useCenter = false,
                        style = Stroke(width = 48f, cap = StrokeCap.Round)
                    )
                }

            }
            , horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Box(modifier = Modifier.width(400.dp), contentAlignment = Alignment.Center) {
                Text(text = String.format(Locale.getDefault(),"%.1f", count),fontSize = 48.sp,
                    fontFamily = fontDigital2(),
                    modifier = Modifier.brushColor(gradientColors), textAlign = TextAlign.Center
                )
            }

        }



    }
}


@Composable
fun CustomCircleProgress(min:Float = 0f, max:Float = 100f,
                         foregroundColor: Color? = null, gradientColors:List<Color> = listOf(Color(0xFF0A62E4),Color(0xFF7EB1FD)),
                         backgroundColor:Color = Color(0xFFDBDADA), size: Dp = 300.dp) {

    var count by remember {
        mutableStateOf(0f)
    }

    val animatedIndicatorValue = remember {
        androidx.compose.animation.core.Animatable(initialValue = (count * (360 / max)))
    }

    LaunchedEffect(key1 = count) {
        val newValue = if (count > max) {
            count = max
            360f
        } else if (count < min) {
            count = min
            0f
        } else {
            (count * (360 / max))
        }
        animatedIndicatorValue.animateTo(
            newValue,
            animationSpec = spring(
                Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        Modifier
            .background(Color.Transparent)
            .clickable {
                count += 10
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Column(modifier = Modifier
            .size(size)
            .padding(size / 10)
            .drawBehind {
                drawArc(
                    color = backgroundColor, startAngle = 150f,
                    sweepAngle = 360f, useCenter = false,
                    style = Stroke(width = 64f, cap = StrokeCap.Round)
                )
            }
            .drawBehind {
                if (foregroundColor == null) {
                    drawArc(
                        brush = Brush.verticalGradient(gradientColors), startAngle = 150f,
                        sweepAngle = animatedIndicatorValue.value, useCenter = false,
                        style = Stroke(width = 64f, cap = StrokeCap.Round)
                    )
                } else {
                    drawArc(
                        color = foregroundColor, startAngle = 150f,
                        sweepAngle = animatedIndicatorValue.value, useCenter = false,
                        style = Stroke(width = 64f, cap = StrokeCap.Round)
                    )
                }

            }
            , horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Text(text = "${count.toInt()}", fontSize = 45.sp,
                fontWeight = FontWeight.Bold, color = Color.Gray)
        }



    }
}
