import type { DoorSequenceState, DoorType } from '../engine/doors/selectDoorType'

export interface RunStats {
  pv: number
  pvMax: number
  force: number
  dexterite: number
  charisme: number
  chance: number
}

export const STARTING_STATS: RunStats = {
  pv: 50,
  pvMax: 50,
  force: 60,
  dexterite: 60,
  charisme: 60,
  chance: 10,
}

export interface ActiveDoorInfo {
  doorType: DoorType
  /** Monster or boss content id for this encounter; null for non-combat doors. */
  monsterId: string | null
}

export type Screen = 'title' | 'intro' | 'combat' | 'eventChoice' | 'reward' | 'endRun'

export type RunOutcome = 'death' | 'victory' | null

export interface RunState {
  screen: Screen
  runOutcome: RunOutcome
  stats: RunStats
  doorsCleared: number
  permanentItems: string[]
  consumables: (string | null)[]
  activeDoor: ActiveDoorInfo | null
  rngState: number
  /**
   * Not part of #8's originally-drafted schema, but required for #14's
   * cooldown/streak rules to survive a reload — added during implementation
   * as a necessary completion, not a new product decision.
   */
  doorSequenceState: DoorSequenceState
  bestScore: number
}

export function createInitialRunState(seed: number, bestScore: number): RunState {
  return {
    screen: 'intro',
    runOutcome: null,
    stats: { ...STARTING_STATS },
    doorsCleared: 0,
    permanentItems: [],
    consumables: [null, null, null],
    activeDoor: null,
    rngState: seed,
    doorSequenceState: { previousDoorType: null, monsterStreak: 0 },
    bestScore,
  }
}
