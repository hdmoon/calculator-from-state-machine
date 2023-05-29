package com.example.calcfromstatemachine

import android.util.Log
import androidx.annotation.VisibleForTesting

/**
 * A single-thread state machine. Cannot be reused once destroyed.
 */
open class StateMachine {

    companion object {
        private const val TAG: String = "StateMachine"
    }

    open class State(val name: String) {
        open fun onEnter() {
            Log.d(TAG, "$name: onEnter")
        }

        open fun onExit() {
            Log.d(TAG, "$name: onExit")
        }

        open fun onProcessMessage(message: Any) {
            Log.d(TAG, "$name: onProcessMessage($message)")
        }

        override fun toString(): String {
            return name
        }
    }

    @VisibleForTesting
    var states: MutableList<State> = ArrayList()

    @VisibleForTesting
    var started: Boolean = false

    @VisibleForTesting
    var destroyed: Boolean = false

    @VisibleForTesting
    lateinit var currentState: State

    fun start() {
        if (!this::currentState.isInitialized) {
            throw Exception("Initial state must be set before calling start()")
        }
        if (started || destroyed) {
            return
        }
        started = true
        currentState.onEnter()
    }

    fun destroy() {
        destroyed = true
    }

    fun processMessage(message: Any) {
        if (!started || destroyed) {
            return
        }
        Log.d(TAG, "processMessage: state=$currentState message=$message")
        currentState.onProcessMessage(message)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun addState(state: State) {
        if (started || destroyed) {
            throw Exception("Cannot add a new state for an already started or " +
                    "destroyed StateMachine!")
        }
        if (!states.contains(state)) {
            states.add(state)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun setInitialState(state: State) {
        if (!states.contains(state)) {
            throw Exception("A state must be added in order to become an initial state!")
        }
        if (started || destroyed) {
            throw Exception("Cannot set an initial state for an already started or " +
                    "destroyed StateMachine!")
        }
        currentState = state
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun transitionTo(state: State) {
        if (!started || destroyed) {
            return
        }
        if (!states.contains(state)) {
            throw Exception("Cannot make transition to an unknown state: $state")
        }
        Log.d(TAG, "transitionTo: prevState=$currentState newState=$state")
        currentState.onExit()

        currentState = state
        currentState.onEnter()
    }
}