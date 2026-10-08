package com.fang.loan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fang.cosmos.foundation.NumberFormat
import com.fang.cosmos.foundation.ui.ext.clickableNoRipple
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class LoanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoanContent(
                modifier =
                    Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun LoanContent(
    modifier: Modifier,
    todayDate: LocalDate =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date,
) {
    val startDate = Loan.startDate
    val monthAvailable = Loan.MONTH_AVAILABLE
    var dateState by remember { mutableStateOf(todayDate) }
    val loansState = remember { mutableStateOf(Loan.all) }
    loansState.value
        .mapNotNull { loan ->
            startDate
                .monthsUntil(dateState)
                .takeIf { it < loan.remain }
                ?.let { loan.copy(remain = loan.remain - it) }
        }.takeIf { it.isNotEmpty() }
        ?.let { loans ->
            Column(modifier.padding(horizontal = 20.dp)) {
                val totalRemain = NumberFormat(loans.sumOf { it.remainAmount }) ?: "-"
                val remain = loans.sumOf { it.amount }
                val remainFormat = NumberFormat(remain) ?: "-"
                val available = NumberFormat(monthAvailable - remain) ?: "-"
                Text(
                    text = "$totalRemain / $remainFormat / $available",
                    modifier =
                        Modifier
                            .clickableNoRipple {
                                dateState = todayDate
                            }.padding(vertical = 16.dp),
                    fontSize = 24.sp,
                    color = Color(0xffc5c5c5),
                )
                loans.forEach { loan ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        with(loansState) {
                            LoanText(loan.name)
                            LoanText(NumberFormat(loan.amount), "00,000") { amount }
                            LoanText(loan.remain, "000") { remain }
                            LoanText(NumberFormat(loan.remainAmount), "0,000,000") { remainAmount }
                            LoanText(
                                loan.lastPaymentDate(dateState).toString().dropLast(3),
                            ) { remain }
                            LoanText(loan.day, "000") { day }
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
                loansState.value
                    .mapNotNull { loan ->
                        startDate
                            .monthsUntil(todayDate)
                            .takeIf { it < loan.remain }
                            ?.let { loan.copy(remain = loan.remain - it) }
                    }.map {
                        it.lastPaymentDate(todayDate)
                    }.distinct()
                    .forEachIndexed { i, freeDate ->
                        val (totalRemain, remain) =
                            loansState.value
                                .mapNotNull { loan ->
                                    startDate
                                        .monthsUntil(freeDate)
                                        .takeIf { it < loan.remain }
                                        ?.let { loan.copy(remain = loan.remain - it) }
                                }.let { loans ->
                                    val totalRemain = loans.sumOf { it.remainAmount }
                                    val remain = loans.sumOf { it.amount }
                                    totalRemain to remain
                                }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickableNoRipple {
                                    dateState = freeDate
                                }.padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ItemText("${i + 1}")
                            ItemText("$$remain", "$00,000")
                            ItemText(
                                "$${NumberFormat(monthAvailable - remain).orEmpty()}",
                                "$00,000",
                            )
                            ItemText(
                                freeDate.plus(DatePeriod(months = 1)).toString().dropLast(3),
                            )
                            ItemText(
                                "$${NumberFormat(totalRemain).orEmpty()}",
                                alignment = Alignment.CenterStart,
                            )
                        }
                    }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                ItemText("11,490")
                ItemText("17,560")
            }
        } ?: Box(Modifier.fillMaxSize()) {
        Text(
            text = "還清",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Gray,
        )
    }
}

@Composable
private fun MutableState<List<Loan>>.LoanText(
    text: Any?,
    holder: String? = null,
    alignment: Alignment = Alignment.CenterEnd,
    trans: (Loan.() -> Int)? = null,
) = Box(
    modifier =
        Modifier.clickableNoRipple {
            value = trans?.let {
                val asc = value.sortedBy { trans(it) }
                if (asc == value) {
                    Loan.all.sortedByDescending { trans(it) }
                } else {
                    asc
                }
            } ?: Loan.all
        },
) {
    ItemText(text, holder, alignment)
}

@Composable
private fun ItemText(
    text: Any?,
    holder: String? = null,
    alignment: Alignment = Alignment.CenterEnd,
) = Box(contentAlignment = alignment) {
    val content = text?.toString() ?: "-"
    Text(
        text = content,
        color = Color(0xffbcbcbc),
        fontSize = 16.sp,
    )
    Text(
        text = holder ?: content,
        color = Color.Transparent,
        fontSize = 16.sp,
    )
}
