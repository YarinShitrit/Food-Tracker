package com.example.burgertracker

import org.junit.Test

import org.junit.Assert.*
import kotlin.math.log

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun calculateBinaryGap() {
        val num = 3
        val binaryNum = Integer.toBinaryString(num)
        var binNum = ""
        var quotient = num.toDouble()
        while (quotient / 2.0 != 0.0 && quotient >= 1.0) {
            println("quotient is $quotient")
            if ((quotient / 2.0) % 1.0 == 0.0) {
                binNum += '0'
                quotient /= 2.0
            } else {
                binNum += '1'
                quotient = (quotient / 2.0).toInt().toDouble()
            }
            quotient = quotient.toInt().toDouble()
        }
        binNum = binaryNum.reversed()
        println(binNum)
        print(binaryNum)





    }
}
