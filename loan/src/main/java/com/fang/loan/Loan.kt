package com.fang.loan

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

data class Loan(
    val name: String,
    val amount: Int,
    val remain: Int,
    val day: Int,
) {
    companion object {
        private const val PIVOT_DATE = 5
        val startDate = LocalDate.parse("2026-09-0${PIVOT_DATE}")
        const val MONTH_AVAILABLE = 55000
        val all =
            listOf(
                Loan(name = "鄉民", amount = 5673, remain = 7, day = 18),
                Loan(name = "信用", amount = 5817, remain = 8, day = 8),
                Loan(name = "安泰", amount = 2029, remain = 12, day = 18),
                Loan(name = "國泰", amount = 3696, remain = 13, day = 17),
                Loan(name = "玉山", amount = 1056, remain = 13, day = 13),
                Loan(name = "連線", amount = 1888, remain = 36, day = 13),
                Loan(name = "連線", amount = 15672, remain = 117, day = 13),
                Loan(name = "樂天", amount = 1980, remain = 119, day = 8),
            )
    }

    val remainAmount get() = amount * remain

    fun lastPaymentDate(date: LocalDate) =
        date.plus(
            remain -
                if (date.day >= PIVOT_DATE) 0 else 1,
            DateTimeUnit.MONTH,
        )
}
