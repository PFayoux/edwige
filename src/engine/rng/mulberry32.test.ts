import { describe, expect, it } from 'vitest'
import { createRng, restoreRng } from './mulberry32'

describe('mulberry32 RNG (issue #3)', () => {
  it('is deterministic for a given seed', () => {
    const a = createRng(42)
    const b = createRng(42)
    expect(a.next()).toBe(b.next())
    expect(a.rollD100()).toBe(b.rollD100())
  })

  it('rollD100 stays within [1, 100]', () => {
    const rng = createRng(7)
    for (let i = 0; i < 1000; i++) {
      const roll = rng.rollD100()
      expect(roll).toBeGreaterThanOrEqual(1)
      expect(roll).toBeLessThanOrEqual(100)
    }
  })

  it('restoreRng continues deterministically from a persisted state', () => {
    const original = createRng(1)
    original.next()
    original.next()
    const savedState = original.state
    const expected = original.next()

    const restored = restoreRng(savedState)
    expect(restored.next()).toBe(expected)
  })
})
