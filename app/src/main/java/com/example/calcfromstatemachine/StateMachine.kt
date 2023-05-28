package com.example.calcfromstatemachine

import android.util.Log
import androidx.annotation.VisibleForTesting

/**
 * A single-thread state machine.
 */
open class StateMachine {

    companion object {
        private const val TAG: String = "StateMachine"
    }

    open class State(var name: String) {
        open fun onEnter() {}
        open fun onExit() {}
        open fun onProcessMessage(message: Any) {}

        final override fun toString(): String {
            return name
        }
    }

    @VisibleForTesting
    var states: MutableList<State> = ArrayList()

    @VisibleForTesting
    var started: Boolean = false

    @VisibleForTesting
    lateinit var currentState: State

    fun addState(state: State) {
        if (started) {
            throw Exception("Cannot add a new state for an already started StateMachine!")
        }
        if (!states.contains(state)) {
            states.add(state)
        }
    }

    fun setInitialState(state: State) {
        if (!states.contains(state)) {
            throw Exception("A state must be added in order to become an initial state!")
        }
        if (started) {
            throw Exception("Cannot set an initial state for an already started StateMachine!")
        }
        currentState = state
    }

    fun start() {
        if (!this::currentState.isInitialized) {
            throw Exception("Initial state must be set before calling start()")
        }
        if (started) {
            return
        }
        started = true
        currentState.onEnter()
    }

    fun stop() {
        started = false
    }

    fun processMessage(message: Any) {
        if (!started) {
            return
        }
        Log.d(TAG, "processMessage: State=$currentState message=$message")
        currentState.onProcessMessage(message)
    }

    fun transitionTo(state: State) {
        if (!started) {
            return
        }
        if (!states.contains(state)) {
            throw Exception("Cannot make transition to an unkown state: $state")
        }
        Log.d(TAG, "transitionTo: prevState=$currentState newState=$state")
        currentState.onExit()

        currentState = state
        currentState.onEnter()
    }
}