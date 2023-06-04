package com.example.calcfromstatemachine

import android.util.Log
import androidx.annotation.VisibleForTesting

// TODO: Support non-integer calculations
class CalculatorStateMachine : StateMachine() {

    @VisibleForTesting
    var numberOnView: String = ""

    companion object {
        const val ERROR: String = "ERROR!"
        private const val TAG: String = "CalcStateMachine"
    }

    interface Message

    class ClearMessage: Message
    class CalculateMessage: Message

    class DigitMessage(val value: Int): Message {
        init {
            when (value) {
                in 0..9 -> Unit
                else -> throw Exception("Value should be between 0 and 9!")
            }
        }
    }

    class OperatorMessage(val op: String): Message {
        init {
            when (op) {
                in listOf("+", "-", "*", "/") -> Unit
                else -> throw Exception("Wrong operator $op!")
            }
        }
    }

    // TODO: Add access modifier with writing tests
    var leftNumber: String = ""
    var op: String = ""
    var rightNumber: String = ""

    var clearedState: State = ClearedState()
    var leftNumberState: State = LeftNumberState()
    var operatorState: State = OperatorState()
    var rightNumberState: State = RightNumberState()
    var calculatedByEqualKeyState: State = CalculatedByEqualKeyState()
    var errorState: State = ErrorState()

    init {
        addState(clearedState)
        addState(leftNumberState)
        addState(operatorState)
        addState(rightNumberState)
        addState(calculatedByEqualKeyState)
        addState(errorState)

        setInitialState(clearedState)
    }

    fun clear() {
        leftNumber = ""
        op = ""
        rightNumber = ""
    }

    fun calculate(): Int {
        Log.d(TAG, "calculate: $leftNumber $op $rightNumber")

        val leftNumAsInt = leftNumber.toInt()
        val rightNumAsInt = rightNumber.toInt()

        val result: Int = when (op) {
            "+" -> leftNumAsInt + rightNumAsInt
            "-" -> leftNumAsInt - rightNumAsInt
            "*" -> leftNumAsInt * rightNumAsInt
            "/" -> leftNumAsInt / rightNumAsInt
            else -> {
                throw Exception("Wrong operator $op!")
            }
        }

        Log.d(TAG, "calculate: result is $result")
        return result
    }

    fun updateView(value: String) {
        if (value.isEmpty()) {
            Log.d(TAG, "Clearing view")
        } else {
            Log.d(TAG, "Updating view to $value")
        }
        numberOnView = value
    }

    inner class ClearedState : State("Cleared") {
        override fun onEnter() {
            super.onEnter()
            clear()
            updateView("")
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    leftNumber = message.value.toString()
                    transitionTo(leftNumberState)
                }
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }

    inner class LeftNumberState : State("Writing left number") {
        override fun onEnter() {
            super.onEnter()
            updateView(leftNumber)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    if (leftNumber == "0") {
                        if (message.value == 0) {
                            return
                        } else {
                            leftNumber = message.value.toString()
                        }
                    } else {
                        leftNumber += message.value
                    }
                    transitionTo(this)
                }
                is OperatorMessage -> {
                    op = message.op
                    transitionTo(operatorState)
                }
                is ClearMessage -> transitionTo(clearedState)
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }

    inner class OperatorState : State("Setting op") {
        override fun onEnter() {
            super.onEnter()
            updateView(leftNumber)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    rightNumber = message.value.toString()
                    transitionTo(rightNumberState)
                }
                is OperatorMessage -> {
                    op = message.op
                    transitionTo(this)
                }
                is ClearMessage -> transitionTo(clearedState)
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }

    inner class RightNumberState : State("Writing right number") {
        override fun onEnter() {
            super.onEnter()
            updateView(rightNumber)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    if (rightNumber == "0") {
                        if (message.value == 0) {
                            return
                        } else {
                            rightNumber = message.value.toString()
                        }
                    } else {
                        rightNumber += message.value
                    }
                    transitionTo(this)
                }
                is OperatorMessage -> {
                    val calcResult: Int
                    try {
                        calcResult = calculate()
                    } catch (ex: ArithmeticException) {
                        transitionTo(errorState)
                        return
                    }

                    leftNumber = calcResult.toString()
                    rightNumber = ""
                    op = message.op

                    transitionTo(operatorState)
                }
                is CalculateMessage -> {
                    val calcResult: Int
                    try {
                        calcResult = calculate()
                    } catch (ex: ArithmeticException) {
                        transitionTo(errorState)
                        return
                    }
                    leftNumber = calcResult.toString()

                    transitionTo(calculatedByEqualKeyState)
                }
                is ClearMessage -> transitionTo(clearedState)
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }

    inner class CalculatedByEqualKeyState : State("Calculated by pressing '=' key") {
        override fun onEnter() {
            super.onEnter()
            updateView(leftNumber)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    clear()
                    leftNumber = message.value.toString()
                    transitionTo(leftNumberState)
                }
                is CalculateMessage -> {
                    val calcResult: Int
                    try {
                        calcResult = calculate()
                    } catch (ex: ArithmeticException) {
                        transitionTo(errorState)
                        return
                    }
                    leftNumber = calcResult.toString()

                    transitionTo(this)
                }
                is ClearMessage -> transitionTo(clearedState)
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }

    inner class ErrorState : State("Error") {
        override fun onEnter() {
            super.onEnter()
            clear()
            updateView(ERROR)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is ClearMessage -> transitionTo(clearedState)
                else -> {
                    Log.d(TAG, "$name: Ignoring message $message")
                    return
                }
            }
        }
    }
}