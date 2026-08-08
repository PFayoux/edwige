import { createContext, useContext, useEffect, useReducer, useRef, type ReactNode } from 'react'
import { runReducer, type RunAction } from './runReducer'
import { createInitialRunState, type RunState } from './types'
import { loadSave, persistCurrentRun, persistRunEnd } from '../persistence/db'
import { createRealRng } from '../engine/rng/mulberry32'

const RunStateContext = createContext<RunState | null>(null)
const RunDispatchContext = createContext<((action: RunAction) => void) | null>(null)

/**
 * App-root context (issue #2) — owns run progression, screen routing, and
 * the door-boundary persistence checkpoint (#8). `CombatContext` (scoped to
 * the Combat screen) is separate and never persisted mid-salvo.
 */
export function RunProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(runReducer, createInitialRunState(0, 0))
  const hydrated = useRef(false)

  useEffect(() => {
    loadSave().then((save) => {
      dispatch({ type: 'HYDRATE', currentRun: save.currentRun, bestScore: save.bestScore })
      hydrated.current = true
    })
  }, [])

  useEffect(() => {
    if (!hydrated.current) return
    if (state.runOutcome) {
      persistRunEnd(state.doorsCleared)
    } else if (state.screen !== 'title') {
      persistCurrentRun(state)
    }
  }, [state])

  return (
    <RunStateContext.Provider value={state}>
      <RunDispatchContext.Provider value={dispatch}>{children}</RunDispatchContext.Provider>
    </RunStateContext.Provider>
  )
}

export function useRunState(): RunState {
  const ctx = useContext(RunStateContext)
  if (!ctx) throw new Error('useRunState must be used within RunProvider')
  return ctx
}

export function useRunDispatch(): (action: RunAction) => void {
  const ctx = useContext(RunDispatchContext)
  if (!ctx) throw new Error('useRunDispatch must be used within RunProvider')
  return ctx
}

export function startNewRun(dispatch: (action: RunAction) => void) {
  const seed = createRealRng().state
  dispatch({ type: 'START_NEW_RUN', seed })
}
