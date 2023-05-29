package com.example.calcfromstatemachine

import android.util.Log
import androidx.annotation.VisibleForTesting

// TODO: Support non-integer calculations
class CalculatorStateMachine : StateMachine() {

    @VisibleForTesting
    var numberOnView: String = ""

    companion object {
        private const val TAG: String = "CalcStateMachine"
    }

    class DigitMessage(val value: Int) {
        init {
            when (value) {
                in 0..9 -> Unit
                else -> throw Exception("Value should be between 0 and 9!")
            }
        }
    }

    class OperatorMessage(val op: String) {
        init {
            when (op) {
                in listOf("+", "-", "*", "/") -> Unit
                else -> throw Exception("Wrong operator $op!")
            }
        }
    }

    class ClearMessage
    class CalculateMessage

    // TODO: Add access modifier with writing tests
    var leftNumber: String = ""
    var op: String = ""
    var rightNumber: String = ""

    var clearedState: State = ClearedState()
    var leftNumberState: State = LeftNumberState()
    var operatorState: State = OperatorState()
    var rightNumberState: State = RightNumberState()
    var calculatedByEqualKeyState: State = CalculatedByEqualKeyState()

    init {
        addState(clearedState)
        addState(leftNumberState)
        addState(operatorState)
        addState(rightNumberState)
        addState(calculatedByEqualKeyState)

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
        Log.d(TAG, "Updating view to $value")
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
                    if (message.value == 0 && leftNumber == "0") {
                        return
                    }
                    leftNumber += message.value
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
                    if (message.value == 0 && rightNumber == "0") {
                        return
                    }
                    rightNumber += message.value
                }
                is OperatorMessage -> {
                    val calcResult = calculate()
                    leftNumber = calcResult.toString()
                    rightNumber = ""
                    op = message.op

                    transitionTo(operatorState)
                }
                is CalculateMessage -> {
                    val calcResult = calculate()
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
            updateView(rightNumber)
        }

        override fun onProcessMessage(message: Any) {
            when (message) {
                is DigitMessage -> {
                    clear()
                    leftNumber = message.value.toString()
                    transitionTo(leftNumberState)
                }
                is CalculateMessage -> {
                    val calcResult = calculate()
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
}