import { createInitialRunState, type RunState, type RunStats } from './types'
import { restoreRng } from '../engine/rng/mulberry32'
import { selectDoorType } from '../engine/doors/selectDoorType'
import { getPalierForDoor, selectBossForPalier } from '../engine/bosses/selectBoss'
import monstersData from '../data/monsters.json'
import type { MonsterContent } from '../data/types'

const monsters = monstersData as MonsterContent[]

export type RunAction =
  | { type: 'HYDRATE'; currentRun: RunState | null; bestScore: number }
  | { type: 'START_NEW_RUN'; seed: number }
  | { type: 'OPEN_NEXT_DOOR' }
  | { type: 'WIN_COMBAT'; playerPv: number }
  | { type: 'LOSE_COMBAT'; playerPv: number }
  | { type: 'RESOLVE_EVENT'; statDelta?: Partial<RunStats>; pvDelta?: number }
  | { type: 'APPLY_REWARD_STAT'; stat: keyof RunStats }
  | { type: 'APPLY_REWARD_ITEM'; itemId: string }
  | { type: 'GO_TO_COMBAT_SCREEN' }

export function runReducer(state: RunState, action: RunAction): RunState {
  switch (action.type) {
    case 'HYDRATE':
      return action.currentRun ?? { ...createInitialRunState(0, action.bestScore), screen: 'title' }

    case 'START_NEW_RUN':
      return createInitialRunState(action.seed, state.bestScore)

    case 'OPEN_NEXT_DOOR': {
      if (state.runOutcome) return state
      const doorNumber = state.doorsCleared + 1
      const rng = restoreRng(state.rngState)
      const { type, nextState } = selectDoorType(doorNumber, state.doorSequenceState, rng)

      let monsterId: string | null = null
      if (type === 'boss') {
        const palier = getPalierForDoor(doorNumber)
        monsterId = selectBossForPalier(palier, rng).id
      } else if (type === 'monstre' || type === 'tresor') {
        monsterId = monsters[rng.nextInt(monsters.length)].id
      }

      const screen = type === 'monstre' || type === 'tresor' || type === 'boss' ? 'combat' : 'eventChoice'

      return {
        ...state,
        screen,
        activeDoor: { doorType: type, monsterId },
        rngState: rng.state,
        doorSequenceState: nextState,
      }
    }

    case 'WIN_COMBAT': {
      const doorsCleared = state.doorsCleared + 1
      // Bring the combat's final HP back into RunContext before anything else —
      // CombatContext tracks it locally during the salvo loop and doesn't sync
      // automatically (caught by driving the app — see project notes).
      const statsAfterCombat = { ...state.stats, pv: action.playerPv }

      if (doorsCleared === 30) {
        // Tier-3 boss defeated — victory, skipping the normal reward flow (#18).
        return {
          ...state,
          screen: 'endRun',
          runOutcome: 'victory',
          doorsCleared,
          activeDoor: null,
          stats: statsAfterCombat,
          bestScore: Math.max(state.bestScore, doorsCleared),
        }
      }

      const rng = restoreRng(state.rngState)
      const healPct = 0.1 + rng.next() * 0.05
      const pv = Math.min(statsAfterCombat.pvMax, Math.round(statsAfterCombat.pv + statsAfterCombat.pvMax * healPct))

      return {
        ...state,
        screen: 'reward',
        doorsCleared,
        activeDoor: null,
        stats: { ...statsAfterCombat, pv },
        rngState: rng.state,
      }
    }

    case 'LOSE_COMBAT':
      return {
        ...state,
        screen: 'endRun',
        runOutcome: 'death',
        stats: { ...state.stats, pv: action.playerPv },
        bestScore: Math.max(state.bestScore, state.doorsCleared),
      }

    case 'RESOLVE_EVENT': {
      const doorsCleared = state.doorsCleared + 1
      const stats = { ...state.stats, ...action.statDelta }
      if (action.pvDelta) {
        stats.pv = Math.max(0, Math.min(stats.pvMax, stats.pv + action.pvDelta))
      }
      if (stats.pv <= 0) {
        return { ...state, stats, doorsCleared, screen: 'endRun', runOutcome: 'death', activeDoor: null, bestScore: Math.max(state.bestScore, doorsCleared - 1) }
      }
      return { ...state, stats, doorsCleared, activeDoor: null }
    }

    case 'APPLY_REWARD_STAT': {
      const stats = { ...state.stats }
      if (action.stat === 'pvMax') {
        stats.pvMax += 5
        stats.pv = Math.min(stats.pvMax, stats.pv + 5)
      } else {
        stats[action.stat] = Math.min(95, stats[action.stat] + 1)
      }
      return { ...state, stats }
    }

    case 'APPLY_REWARD_ITEM':
      return { ...state, permanentItems: [...state.permanentItems, action.itemId] }

    case 'GO_TO_COMBAT_SCREEN':
      return { ...state, screen: 'combat' }

    default:
      return state
  }
}

export { createInitialRunState }
