import { describe, expect, it } from 'vitest'
import { getPalierForDoor, selectBossForPalier } from './selectBoss'
import { createRng } from '../rng/mulberry32'

describe('getPalierForDoor (issue #22, corrected by #24)', () => {
  it('maps doors 10/20/30 to palier 1/2/3', () => {
    expect(getPalierForDoor(10)).toBe(1)
    expect(getPalierForDoor(20)).toBe(2)
    expect(getPalierForDoor(30)).toBe(3)
  })

  it('throws for a non-boss door number', () => {
    expect(() => getPalierForDoor(15)).toThrow()
  })
})

describe('selectBossForPalier (issue #22)', () => {
  it('always draws from the requested palier pool', () => {
    for (const palier of [1, 2, 3] as const) {
      for (let seed = 0; seed < 20; seed++) {
        const boss = selectBossForPalier(palier, createRng(seed))
        expect(boss.palier).toBe(palier)
      }
    }
  })

  it('draws vary across seeds (independent per run, issue #17)', () => {
    const drawn = new Set<string>()
    for (let seed = 0; seed < 20; seed++) {
      drawn.add(selectBossForPalier(1, createRng(seed)).id)
    }
    expect(drawn.size).toBeGreaterThan(1)
  })
})
