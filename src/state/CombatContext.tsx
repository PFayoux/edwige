import { createContext, useContext, useMemo, useReducer, type ReactNode } from 'react'
import type { ActionStats, ActionType, ConfrontationOutcome, DiceAssignment, Trait } from '../engine/combat/types'
import { resolveSalvo, initialTraitState, type TraitState } from '../engine/combat/resolveSalvo'
import { restoreRng } from '../engine/rng/mulberry32'

export interface CombatMonster {
  nom: string
  pv: number
  stats: ActionStats
  traits: Trait[]
}

interface CombatState {
  monster: CombatMonster
  monsterPv: number
  playerPv: number
  playerPvMax: number
  playerStats: ActionStats
  phase: 'assigning' | 'resolved'
  playerDice: number[]
  assignments: DiceAssignment[]
  lastConfrontations: ConfrontationOutcome[]
  traitState: TraitState
  rngState: number
  outcome: 'ongoing' | 'win' | 'lose' | 'fled'
}

type CombatAction =
  | { type: 'ROLL_SALVO' }
  | { type: 'ASSIGN_DIE'; dieIndex: number; action: ActionType }
  | { type: 'UNASSIGN_LAST' }
  | { type: 'RESOLVE_SALVO' }

function rollSalvoState(state: CombatState): CombatState {
  const rng = restoreRng(state.rngState)
  const playerDice = Array.from({ length: 5 }, () => rng.rollD100())
  return { ...state, playerDice, assignments: [], phase: 'assigning', rngState: rng.state }
}

function combatReducer(state: CombatState, action: CombatAction): CombatState {
  switch (action.type) {
    case 'ROLL_SALVO':
      return rollSalvoState(state)

    case 'ASSIGN_DIE': {
      if (state.assignments.some((a) => a.die === state.playerDice[action.dieIndex])) return state
      if (state.assignments.length >= 5) return state
      const die = state.playerDice[action.dieIndex]
      return { ...state, assignments: [...state.assignments, { die, action: action.action }] }
    }

    case 'UNASSIGN_LAST':
      return { ...state, assignments: state.assignments.slice(0, -1) }

    case 'RESOLVE_SALVO': {
      if (state.assignments.length !== 5) return state

      const rng = restoreRng(state.rngState)
      const actionPool: ActionType[] = ['attaque', 'defense', 'raillerie']
      const monsterOrder: DiceAssignment[] = Array.from({ length: 5 }, () => ({
        die: rng.rollD100(),
        action: actionPool[rng.nextInt(3)],
      }))

      const result = resolveSalvo(
        state.assignments,
        state.playerStats,
        state.playerPv,
        monsterOrder,
        state.monster.stats,
        state.monsterPv,
        state.monster.traits,
        state.traitState,
      )

      let outcome: CombatState['outcome'] = 'ongoing'
      if (result.monsterFled) outcome = 'fled'
      else if (result.monsterPv <= 0) outcome = 'win'
      else if (result.playerPv <= 0) outcome = 'lose'

      return {
        ...state,
        phase: 'resolved',
        playerPv: result.playerPv,
        monsterPv: result.monsterPv,
        lastConfrontations: result.confrontations,
        traitState: result.traitState,
        rngState: rng.state,
        outcome,
      }
    }

    default:
      return state
  }
}

const CombatStateContext = createContext<CombatState | null>(null)
const CombatDispatchContext = createContext<((action: CombatAction) => void) | null>(null)

export function CombatProvider({
  children,
  monster,
  playerStats,
  playerPv,
  playerPvMax,
  rngState,
}: {
  children: ReactNode
  monster: CombatMonster
  playerStats: ActionStats
  playerPv: number
  playerPvMax: number
  rngState: number
}) {
  const initial = useMemo<CombatState>(
    () =>
      rollSalvoState({
        monster,
        monsterPv: monster.pv,
        playerPv,
        playerPvMax,
        playerStats,
        phase: 'assigning',
        playerDice: [],
        assignments: [],
        lastConfrontations: [],
        traitState: initialTraitState(),
        rngState,
        outcome: 'ongoing',
      }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  )
  const [state, dispatch] = useReducer(combatReducer, initial)

  return (
    <CombatStateContext.Provider value={state}>
      <CombatDispatchContext.Provider value={dispatch}>{children}</CombatDispatchContext.Provider>
    </CombatStateContext.Provider>
  )
}

export function useCombatState(): CombatState {
  const ctx = useContext(CombatStateContext)
  if (!ctx) throw new Error('useCombatState must be used within CombatProvider')
  return ctx
}

export function useCombatDispatch(): (action: CombatAction) => void {
  const ctx = useContext(CombatDispatchContext)
  if (!ctx) throw new Error('useCombatDispatch must be used within CombatProvider')
  return ctx
}
