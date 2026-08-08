import type { BossContent } from '../../data/types'
import type { Rng } from '../rng/mulberry32'
import bossesData from '../../data/bosses.json'

const bosses = bossesData as BossContent[]

/**
 * Pure derivation, no RNG — issue #22, corrected by #24: takes the door
 * number being resolved (`doorsCleared + 1`, NOT the `activeDoor` save-schema
 * field, which is a different object entirely). Doors 10/20/30 → palier 1/2/3.
 */
export function getPalierForDoor(doorNumber: number): 1 | 2 | 3 {
  const palier = doorNumber / 10
  if (palier !== 1 && palier !== 2 && palier !== 3) {
    throw new Error(`getPalierForDoor called with a non-boss door number: ${doorNumber}`)
  }
  return palier
}

/**
 * Draws uniformly from the given palier's boss pool using the injected RNG
 * (#3's pattern) — independent per run, no cross-run anti-repeat memory (#17).
 */
export function selectBossForPalier(palier: 1 | 2 | 3, rng: Rng): BossContent {
  const pool = bosses.filter((b) => b.palier === palier)
  return pool[rng.nextInt(pool.length)]
}
