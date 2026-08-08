/**
 * Seeded PRNG (mulberry32) — issue #3.
 *
 * Injected explicitly into resolution functions rather than called globally,
 * so exact rolls can be asserted in tests without mocking. `state` is exposed
 * so it can round-trip through the save schema (`currentRun.rngState`, #8).
 */
export interface Rng {
  /** Returns a float in [0, 1). */
  next(): number
  /** Returns an integer d100 roll in [1, 100]. */
  rollD100(): number
  /** Returns an integer in [0, max) — used for uniform draws from a pool. */
  nextInt(max: number): number
  /** Current internal state, for persisting mid-run RNG progress. */
  readonly state: number
}

export function createRng(seed: number): Rng {
  let a = seed >>> 0

  function next(): number {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }

  return {
    next,
    rollD100() {
      return Math.floor(next() * 100) + 1
    },
    nextInt(max: number) {
      return Math.floor(next() * max)
    },
    get state() {
      return a
    },
  }
}

/** Seeds a real (non-test) run from the platform CSPRNG, per issue #3. */
export function createRealRng(): Rng {
  const seedArray = new Uint32Array(1)
  crypto.getRandomValues(seedArray)
  return createRng(seedArray[0])
}

/** Restores an in-progress run's RNG from its persisted state (#8's `rngState`). */
export function restoreRng(state: number): Rng {
  return createRng(state)
}
