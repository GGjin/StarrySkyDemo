package com.gg.starryskydemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.gg.starryskydemo.ui.theme.DemoColor
import com.gg.starryskydemo.ui.theme.StarrySkyDemoTheme
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StarrySkyDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .drawStarrySkyBg(
                                starNum = 50,
                                meteorRadian = 2.356194490192345,  // 135 ???
                                showDebugInfo = true
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var text by remember {
                            mutableStateOf("hello equationl \n at starry sty\n ${System.currentTimeMillis()}")
                        }

//                        Text(text = text,
//                            color = Color.White,
//                            fontSize = 32.sp,
//                            modifier = Modifier.clickable {
//                                text = "hello equationl \n at starry sky\n ${System.currentTimeMillis()}"
//                            }
//                        )
                    }
                }
            }
        }
    }
}

fun Modifier.drawStarrySkyBg(
    seed: Long = -1,
    background: Color = DemoColor.Black,
    starNum: Int = 20,
    starColorList: List<Color> = listOf(Color(0x99CCCCCC), Color(0x99AAAAAA), Color(0x99777777)),
    starSizeList: List<Float> = listOf(0.8f, 0.9f, 1.2f),
    meteorColor: Color = Color.Red,
    meteorTime: Int = 1500,
    meteorScaleTime: Int = 3000,
    meteorVelocity: Float = 10f,
    meteorRadian: Double = 0.7853981633974483,  // 45???
    meteorLength: Float = 500f,
    meteorStrokeWidth: Float = 10f,
    showDebugInfo: Boolean = false,
) = composed {
    val deltaMeteorAnim = rememberInfiniteTransition()
    val meteorTimeAnim by deltaMeteorAnim.animateFloat(
        initialValue = 0f, targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = meteorTime, delayMillis = meteorScaleTime, easing = LinearEasing)
        )
    )
    val meteorAlphaAnima by deltaMeteorAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = meteorTime, delayMillis = meteorScaleTime, easing = LinearEasing))
    )
    drawWithCache {
        //???????????????????????????????????????????????????recompose

        val random = Random(seed)
        val starInfoList = mutableListOf<StarInfo>()

        for (i in 0 until starNum) {
            val sizeScale = starSizeList.random(random)
            starInfoList.add(
                StarInfo(
                    Offset(
                        random.nextDouble(size.width.toDouble()).toFloat(),
                        random.nextDouble(size.height.toDouble()).toFloat()
                    ),
                    starColorList.random(random = random),
                    size.width / 200 * sizeScale
                )
            )
        }
        val cosAngle = cos(meteorRadian).toFloat()
        val sinAngle = sin(meteorRadian).toFloat()
        val safeDistanceStandard = (size.width / 10).toInt()
        val safeDistanceVertical = (size.height / 10).toInt()

        var currentStartX = 0f
        var currentStartY = 0f
        var currentEndX = 0f
        var currentEndY = 0f
        var currentLength = 0f
        var startX = 0
        var startY = 0
        onDrawBehind {
            //?????????drawBehind????????????????????????????????????

            //????????????
            drawRect(color = background)

            //????????????
            for (star in starInfoList) {
                drawCircle(color = star.color, center = star.offset, radius = star.radius)
            }

            //?????????????????????????????????

            //??????????????????????????????????????????????????????????????????????????????
            if (currentStartX <= size.width && currentStartY <= size.height && currentStartX >= 0 && currentStartY >= 0) {
                //?????????????????????????????????????????????????????????????????????????????????
                if (currentLength != meteorLength && meteorLength > 0) {
                    currentLength = sqrt((currentEndX - currentStartX).pow(2) + (currentEndY - currentStartY).pow(2))
                }

                //????????????????????????
                currentStartX = startX + meteorVelocity * meteorTimeAnim * cosAngle
                currentStartY = startY + meteorVelocity * meteorTimeAnim * sinAngle

                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (meteorLength <= 0 || currentLength < meteorLength) {
                    currentEndX = startX + meteorVelocity * meteorTimeAnim * 2 * cosAngle
                    currentEndY = startY + meteorVelocity * meteorTimeAnim * 2 * sinAngle
                } else {
                    //???????????????????????????????????????????????????????????????????????????????????????
                    currentLength = meteorLength
                    currentEndX = currentStartX + meteorVelocity * cosAngle
                    currentEndY = currentStartY + meteorVelocity * sinAngle
                }
                if (meteorTimeAnim != 0f) {
                    //????????????
                    drawLine(
                        color = meteorColor,
                        start = Offset(currentStartX, currentStartY),
                        end = Offset(currentEndX, currentEndY),
                        strokeWidth = meteorStrokeWidth,
                        alpha = (meteorAlphaAnima / 100).coerceAtMost(1f)
                    )
                }
            }
            if (meteorTimeAnim == 0f) {
                Log.w("----->", "+++++")
                //????????????????????????????????????????????????
                startX = random.nextInt(safeDistanceStandard, size.width.toInt() - safeDistanceStandard)
                startY = random.nextInt(safeDistanceVertical, size.height.toInt() - safeDistanceVertical)
                currentStartX = 0f
                currentStartY = 0f
                currentEndX = 0f
                currentEndY = 0f
                currentLength = 0f
            }

            //??????????????????
            if (showDebugInfo) {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                }
                drawIntoCanvas {
                    it.nativeCanvas.drawText("$meteorTimeAnim", 50f, 100f, paint)

                    it.nativeCanvas.drawText("currentStart = $currentStartX , $currentStartY", 50f, 150f, paint)
                    it.nativeCanvas.drawText("currentEnd = $currentEndX , $currentEndY", 50f, 200f, paint)
                    it.nativeCanvas.drawText("length = $currentLength", 50f, 250f, paint)
                }
            }
        }
    }
}

data class StarInfo(
    val offset: Offset,
    val color: Color,
    val radius: Float
)


@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StarrySkyDemoTheme {
        Greeting("Android")
    }
}