import type { Trait } from '../engine/combat/types'

export interface MonsterContent {
  id: string
  nom: string
  pv: number
  defense: number
  attaque: number
  particularite: string
  traits: Trait[]
}

/** Boss content — same shape as Monster, plus `palier` (issue #22's tier field). */
export interface BossContent extends MonsterContent {
  palier: 1 | 2 | 3
}

export interface ItemContent {
  id: string
  nom: string
  categorie: 'bonus_stat' | 'permanent' | 'double_tranchant' | 'consommable'
  description: string
}
