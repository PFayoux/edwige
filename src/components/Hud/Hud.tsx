import { useState } from 'react'
import { useRunState } from '../../state/RunContext'
import { CharacterSheet } from '../CharacterSheet/CharacterSheet'

/** Persistent HUD reachable from any in-run screen — issue #5. */
export function Hud() {
  const state = useRunState()
  const [sheetOpen, setSheetOpen] = useState(false)

  return (
    <>
      <div
        role="status"
        aria-label={`Vie : ${state.stats.pv} sur ${state.stats.pvMax}. Portes franchies : ${state.doorsCleared}.`}
        style={{ display: 'flex', gap: 'var(--space-4)', alignItems: 'center', padding: 'var(--space-2) var(--space-4)', borderBottom: '2px solid var(--color-border)' }}
      >
        <span>PV : {state.stats.pv} / {state.stats.pvMax}</span>
        <span>Portes : {state.doorsCleared}</span>
        <button onClick={() => setSheetOpen(true)} aria-haspopup="dialog">
          Fiche
        </button>
      </div>
      {sheetOpen && <CharacterSheet onClose={() => setSheetOpen(false)} />}
    </>
  )
}
