package com.example.calcfromstatemachine

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class StateMachineTest {

    lateinit var sm: StateMachine

    @Before
    fun setUp() {
        sm = StateMachine()
    }

    @After
    fun tearDown() {
        sm.stop()
    }

    @Test
    fun testInitialState() {
        val initialState = StateMachine.State("Initial")
        sm.addState(initialState)
        sm.setInitialState(initialState)

        assertThat(sm.currentState).isEqualTo(initialState)
    }

    @Test
    fun testStart() {
        val state = mock(StateMachine.State::class.java)
        sm.addState(state)
        sm.setInitialState(state)
        sm.start()

        verify(state).onEnter()
    }

    @Test
    fun testTransition() {
        val stateA = mock(StateMachine.State::class.java)
        val stateB = mock(StateMachine.State::class.java)

        sm.addState(stateA)
        sm.addState(stateB)
        sm.setInitialState(stateA)

        sm.start()
        sm.transitionTo(stateB)

        assertThat(sm.currentState).isEqualTo(stateB)
        verify(stateA).onExit()
        verify(stateB).onEnter()
    }

    @Test
    fun testProcessMessage() {
        val state = mock(StateMachine.State::class.java)

        sm.addState(state)
        sm.setInitialState(state)
        sm.start()

        val message = "Test Message"
        sm.processMessage(message)

        verify(state).onProcessMessage(message)
    }

    @Test
    fun testTransitionToAfterStop() {
        val stateA = mock(StateMachine.State::class.java)
        val stateB = mock(StateMachine.State::class.java)

        sm.addState(stateA)
        sm.addState(stateB)
        sm.setInitialState(stateA)

        sm.start()
        sm.stop()
        sm.transitionTo(stateB)

        verify(stateA, never()).onExit()
        verify(stateB, never()).onEnter()
        assertThat(sm.currentState).isEqualTo(stateA)
    }

    @Test
    fun testProcessMessageAfterStop() {
        val state = mock(StateMachine.State::class.java)

        sm.addState(state)
        sm.setInitialState(state)
        sm.start()
        sm.stop()

        val message = "Test Message"
        sm.processMessage(message)

        verify(state, never()).onProcessMessage(message)
    }

    @Test
    fun testAddStateTwice_stateIsAddedOnlyOnce() {
        val state = StateMachine.State("State")
        sm.addState(state)
        sm.addState(state)

        assertThat(sm.states).hasSize(1);
        assertThat(sm.states.get(0)).isEqualTo(state);
    }

    @Test
    fun testAddStateAfterStart_throwsException() {
        val state = StateMachine.State("State")
        sm.addState(state)
        sm.setInitialState(state)
        sm.start()

        assertThrows(Exception::class.java) {
            sm.addState(state)
        }
    }

    @Test
    fun testSetInitialStateToUnknownState_throwsException() {
        val state = StateMachine.State("State")
        // Not added the state

        assertThrows(Exception::class.java) {
            sm.setInitialState(state)
        }
    }

    @Test
    fun testSetInitialStateAfterStart_throwsException() {
        val state = StateMachine.State("State")
        sm.addState(state)
        sm.setInitialState(state)
        sm.start()

        assertThrows(Exception::class.java) {
            sm.setInitialState(state)
        }
    }

    @Test
    fun testStartWithNoStates_throwsException() {
        assertThrows(Exception::class.java) {
            sm.start()
        }
    }

    @Test
    fun testTransitionToUnknownState_throwsException() {
        val state = StateMachine.State("State")
        sm.addState(state)
        sm.setInitialState(state)
        sm.start()

        val unknownState = StateMachine.State("State which is not added")
        assertThrows(Exception::class.java) {
            sm.transitionTo(unknownState)
        }
    }
}