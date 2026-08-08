import { useRunState } from '../../state/RunContext'

/** Read-only overlay reachable from any in-run screen — issue #5. */
export function CharacterSheet({ onClose }: { onClose: () => void }) {
  const { stats, permanentItems } = useRunState()

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label="Fiche personnage"
      style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 500 }}
    >
      <div style={{ background: 'var(--color-bg-raised)', border: '2px solid var(--color-border)', borderRadius: 'var(--radius)', padding: 'var(--space-6)', minWidth: '260px' }}>
        <h2>Fiche personnage</h2>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li>PV : {stats.pv} / {stats.pvMax}</li>
          <li>Force : {stats.force}</li>
          <li>Dextérité : {stats.dexterite}</li>
          <li>Charisme : {stats.charisme}</li>
          <li>Chance : {stats.chance}</li>
        </ul>
        <h3>Objets permanents</h3>
        {permanentItems.length === 0 ? <p>Aucun pour l'instant.</p> : <ul>{permanentItems.map((id, i) => <li key={i}>{id}</li>)}</ul>}
        <button onClick={onClose} className="accent">
          Fermer
        </button>
      </div>
    </div>
  )
}
