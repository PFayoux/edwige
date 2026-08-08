import { useRunState } from '../../state/RunContext'

/**
 * Combat-local consumable display (issue #5) — 3 slots. Display-only for
 * this pass: consumable-use effects during a salvo aren't wired up yet
 * (scope reduction, see project notes); this shows what's held.
 */
export function ItemTray() {
  const { consumables } = useRunState()

  return (
    <div aria-label="Objets de combat" style={{ display: 'flex', gap: 'var(--space-2)' }}>
      {consumables.map((slot, i) => (
        <div
          key={i}
          style={{ width: 'var(--tap-target-min)', height: 'var(--tap-target-min)', border: '2px dashed var(--color-border)', borderRadius: 'var(--radius)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.7em' }}
        >
          {slot ?? '—'}
        </div>
      ))}
    </div>
  )
}
