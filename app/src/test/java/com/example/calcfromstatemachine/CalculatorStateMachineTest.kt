package com.example.calcfromstatemachine

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

// In output string, "E" represents an empty string.
class CalculatorStateMachineTest {

    companion object {
        private const val TAG = "CalculatorStateMachineTest"
    }

    @Test
    fun testPuttingNumberAndClear() {
        testButtonSequences("0 C", "0 E")
        testButtonSequences("0 0 C", "0 0 E")
        testButtonSequences("0 1 C", "0 1 E")

        testButtonSequences("1 C", "1 E")
        testButtonSequences("1 0 C", "1 10 E")
        testButtonSequences("1 2 3 C", "1 12 123 E")
        testButtonSequences("1 0 0 0 5 C", "1 10 100 1000 10005 E")

        testButtonSequences("9 8 C", "9 98 E")

        testButtonSequences("9 8 7 6 5 4 3 2 1 0 C",
            "9 98 987 9876 98765 987654 9876543 98765432 987654321 9876543210 E")
    }

    @Test
    fun testPuttingOperatorOnly_doesNotChangeView() {
        testButtonSequences("+ - * /", "E E E E")
    }

    @Test
    fun testPuttingNumberAndOperator() {
        testButtonSequences("1 + C", "1 1 E")
        testButtonSequences("0 1 - C", "0 1 1 E")
        testButtonSequences("1 2 * C", "1 12 12 E")
        testButtonSequences("1 2 3 / C", "1 12 123 123 E")
    }

    @Test
    fun testPuttingNumberOperatorNumberAndEquals() {
        testButtonSequences("1 + 2 = C", "1 1 2 3 E")
        testButtonSequences("1 2 3 - 1 = C", "1 12 123 123 1 122 E")
        testButtonSequences("0 9 * 3 = C", "0 9 9 3 27 E")
        testButtonSequences("6 / 2 = C", "6 6 2 3 E")
        testButtonSequences("1 2 3 * 4 5 =", "1 12 123 123 4 45 5535")
    }

    @Test
    fun testPuttingNumberOperatorNumberAndConsecutiveEquals_repeatsSameOperation() {
        testButtonSequences("3 + 7 = = C", "3 3 7 10 17 E")
        testButtonSequences("1 0 * 2 = = = = C", "1 10 10 2 20 40 80 160 E")
        testButtonSequences("1 6 / 2 = = = = C", "1 16 16 2 8 4 2 1 E")
        testButtonSequences("5 - 3 = = C", "5 5 3 2 -1 E")
    }

    @Test
    fun testPuttingNumberOperatorNumberAndOperator_triggersCalculation() {
        testButtonSequences("3 + 4 - C", "3 3 4 7 E")
        testButtonSequences("3 + 4 - * C", "3 3 4 7 7 E")
        testButtonSequences("6 3 / 9 - * C", "6 63 63 9 7 7 E")
    }

    @Test
    fun testCalculateAfterClear() {
        testButtonSequences("3 + 4 = C 5 * 2 = =", "3 3 4 7 E 5 5 2 10 20")
    }

    @Test
    fun testCalculateAfterCalculated() {
        testButtonSequences("3 + 4 = 5 * 2 = =", "3 3 4 7 5 5 2 10 20")
    }

    @Test
    fun testDivideByZeroAndRecoverFromError() {
        testButtonSequences("3 / 0 = C 6 + 7 =",
            "3 3 0 ${CalculatorStateMachine.ERROR} E 6 6 7 13")
    }

    // In output string, "E" is considered as an empty string
    private fun testButtonSequences(inputsAsString: String, outputsAsString: String) {
        val sm = CalculatorStateMachine()
        sm.start()

        Log.d(TAG, "inputsAsString     : $inputsAsString")
        Log.d(TAG, "outputsAsString    : $outputsAsString")
        val inputs = inputsAsString.split(" ")
        val outputs = outputsAsString.split(" ")

        assertWithMessage("Inputs and outputs should have the same size!")
            .that(inputs.size).isEqualTo(outputs.size)

        for (i in inputs.indices) {
            val expectedOutput = if (outputs[i] == "E") "" else outputs[i]
            Log.d(TAG, "[Step $i] input: ${inputs[i]}, expectedOutput: $expectedOutput")

            sm.processMessage(convertStringToMessage(inputs[i]))

            assertThat(sm.numberOnView).isEqualTo(expectedOutput)
        }

        sm.destroy()
    }

    private fun convertStringToMessage(str: String): CalculatorStateMachine.Message {
        return when (str) {
            "=" -> CalculatorStateMachine.CalculateMessage()
            "C" -> CalculatorStateMachine.ClearMessage()
            "+", "-", "/", "*" -> CalculatorStateMachine.OperatorMessage(str)
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" ->
                CalculatorStateMachine.DigitMessage(str.toInt())
            else -> throw Exception("Unexpected input string: $str!")
        }
    }
}