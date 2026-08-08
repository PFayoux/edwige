import type { Rng } from '../rng/mulberry32'

export type DoorType = 'monstre' | 'tresor' | 'piege' | 'malediction' | 'marchand' | 'boss'

/** game-design.md's stated table — a soft long-run target, not exact (issue #14). */
const BASE_WEIGHTS: Record<Exclude<DoorType, 'boss'>, number> = {
  monstre: 55,
  tresor: 15,
  piege: 15,
  malediction: 10,
  marchand: 5,
}

const MONSTER_STREAK_CAP = 4

export interface DoorSequenceState {
  previousDoorType: DoorType | null
  monsterStreak: number
}

export function initialDoorSequenceState(): DoorSequenceState {
  return { previousDoorType: null, monsterStreak: 0 }
}

/**
 * Picks the door type for `doorNumber` (1-indexed) — issue #14's
 * type-differentiated cooldown, layered under the tiered boss cadence from
 * issue #16 (doors 10/20/30 are always boss doors).
 *
 * Pure aside from the injected `rng` (#3's pattern) — same inputs, same output.
 */
export function selectDoorType(doorNumber: number, state: DoorSequenceState, rng: Rng): { type: DoorType; nextState: DoorSequenceState } {
  if (doorNumber % 10 === 0) {
    return { type: 'boss', nextState: { previousDoorType: 'boss', monsterStreak: 0 } }
  }

  if (doorNumber === 1) {
    return { type: 'monstre', nextState: { previousDoorType: 'monstre', monsterStreak: 1 } }
  }

  const weights: Record<Exclude<DoorType, 'boss'>, number> = { ...BASE_WEIGHTS }

  if (state.monsterStreak >= MONSTER_STREAK_CAP) {
    weights.monstre = 0
  }

  const cooldownTypes: Exclude<DoorType, 'boss' | 'monstre'>[] = ['tresor', 'piege', 'malediction', 'marchand']
  for (const type of cooldownTypes) {
    if (state.previousDoorType === type) {
      weights[type] = 0
    }
  }

  const type = drawWeighted(weights, rng)
  const nextMonsterStreak = type === 'monstre' ? state.monsterStreak + 1 : 0

  return { type, nextState: { previousDoorType: type, monsterStreak: nextMonsterStreak } }
}

function drawWeighted(weights: Record<Exclude<DoorType, 'boss'>, number>, rng: Rng): Exclude<DoorType, 'boss'> {
  const entries = Object.entries(weights) as [Exclude<DoorType, 'boss'>, number][]
  const total = entries.reduce((sum, [, w]) => sum + w, 0)
  let roll = rng.next() * total
  for (const [type, weight] of entries) {
    if (roll < weight) return type
    roll -= weight
  }
  // Floating-point edge case: fall back to the last positive-weight entry.
  return entries.filter(([, w]) => w > 0).pop()?.[0] ?? 'monstre'
}
