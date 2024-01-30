package com.example.jettipapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.SliderPositions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection.Companion.Content
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jettipapp.components.InputField
import com.example.jettipapp.ui.theme.JetTipAppTheme
import com.example.jettipapp.util.calculateTotalPerson
import com.example.jettipapp.util.calculateTotalTip
import com.example.jettipapp.widgets.RoundIconButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MainContent()
            }
        }
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit){
    JetTipAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.background
        ) {
            content()
        }
    }
}

@Composable
fun TopHeader(totalPerPerson: Double = 133.456){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(150.dp)
            .clip(shape = RoundedCornerShape(corner = CornerSize(12.dp))),
        color = Color(0xFFE9D7F7)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val total = "%.2f".format(totalPerPerson)
            Text(text = "Total Per Person",
                style = MaterialTheme.typography.h5
            )
            Text(text = "$$total",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold)
        }

    }
}


@Preview
@Composable
fun MainContent() {
    Surface (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        val range = IntRange(start = 1, endInclusive = 100)

        val splitByState = remember {
            mutableStateOf(1)
        }

        val tipAmountState = remember {
            mutableStateOf(0.0)
        }

        val totalPerPersonState = remember {
            mutableStateOf(0.0)
        }

        Column (
            modifier = Modifier
                .padding(12.dp)
        ) {
            BillForm(
                splitByState = splitByState,
                tipAmountState = tipAmountState,
                totalPerPersonState = totalPerPersonState
            ) { billAmt ->
                Log.d("AMT", "MainContent: $billAmt")
            }
        }
    }
}

@Composable
fun BillForm(
    modifier: Modifier = Modifier,
    range: IntRange = 1..100,
    splitByState: MutableState<Int>,
    tipAmountState: MutableState<Double>,
    totalPerPersonState: MutableState<Double>,
    onValChange: (String) -> Unit = {}
){
    Column {
        val totalBillState = remember {
            mutableStateOf("")
        }

        val validState = remember(totalBillState.value) {
            totalBillState.value.trim().isNotEmpty()
        }

        val slidePositionState = remember {
            mutableStateOf(0f)
        }

        val tipPercentage = (slidePositionState.value * 100).toInt()

        val keyboardController = LocalSoftwareKeyboardController.current

        TopHeader(totalPerPerson = totalPerPersonState.value)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            color = MaterialTheme.colors.background,
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            border = BorderStroke(width = 1.dp, color = Color.LightGray)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                InputField(valueState = totalBillState,
                    labelId = "Enter Bill",
                    enabled = true,
                    isSingleLine = true,
                    onAction = KeyboardActions {
                        if (!validState) return@KeyboardActions

                        onValChange(totalBillState.value.trim())
                        //TODO - onValueChanged
                        keyboardController?.hide()
                    }
                )
                if (validState) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = "Split",
                            modifier = Modifier.align(
                                alignment = Alignment.CenterVertically
                            )
                        )

                        Spacer(modifier = Modifier.width(120.dp))

                        Row(
                            modifier = Modifier.padding(horizontal = 3.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            RoundIconButton(imageVector = Icons.Default.Remove,
                                onClick = {
                                    splitByState.value =
                                        if (splitByState.value > 1) splitByState.value - 1 else 1

                                    totalPerPersonState.value =
                                        calculateTotalPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value,
                                            tipPercentage = tipPercentage
                                        )
                                })

                            Text(
                                text = "${splitByState.value}",
                                modifier = Modifier
                                    .padding(start = 9.dp, end = 9.dp)
                                    .align(Alignment.CenterVertically)
                            )

                            RoundIconButton(imageVector = Icons.Default.Add,
                                onClick = {
                                    if (splitByState.value < range.last) {
                                        splitByState.value = splitByState.value + 1
                                    }
                                    totalPerPersonState.value =
                                        calculateTotalPerson(
                                            totalBill = totalBillState.value.toDouble(),
                                            splitBy = splitByState.value,
                                            tipPercentage = tipPercentage
                                        )
                                })
                        }

                    }

                    // tip row
                    Row(
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Tip",
                            modifier = Modifier
                                .padding(3.dp)
                                .align(Alignment.CenterVertically)
                        )

                        Spacer(modifier = Modifier.width(180.dp))

                        Text(text = "$ ${tipAmountState.value}")

                    }

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "$tipPercentage %")

                        Spacer(modifier = Modifier.height(14.dp))

                        // slider

                        Slider(value = slidePositionState.value,
                            onValueChange = { newVal ->
                                slidePositionState.value = newVal
                                tipAmountState.value =
                                    calculateTotalTip(
                                        totalBill = totalBillState.value.toDouble(),
                                        tipPercentage = tipPercentage
                                    )

                                totalPerPersonState.value =
                                    calculateTotalPerson(
                                        totalBill = totalBillState.value.toDouble(),
                                        splitBy = splitByState.value,
                                        tipPercentage = tipPercentage
                                    )
                            },
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp),
                            steps = 5,
                            onValueChangeFinished = {

                            }
                        )
                    }
                }
                else{
                    Box(){}
                }
            }
        }
    }
}




