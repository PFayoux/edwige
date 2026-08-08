import { useRunDispatch, useRunState } from '../../state/RunContext'

export function IntroScreen() {
  const state = useRunState()
  const dispatch = useRunDispatch()

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', gap: 'var(--space-4)', padding: 'var(--space-6)' }}>
      <p style={{ maxWidth: '50ch', textAlign: 'center' }}>
        Une chouette s'envole vers un donjon inconnu, son talent Œil de nuit en poche. Chaque porte est un pari — jusqu'à la chute, ou jusqu'au sommet.
      </p>
      <ul style={{ listStyle: 'none', padding: 0 }}>
        <li>PV : {state.stats.pv}</li>
        <li>Force : {state.stats.force}</li>
        <li>Dextérité : {state.stats.dexterite}</li>
        <li>Charisme : {state.stats.charisme}</li>
        <li>Chance : {state.stats.chance}</li>
      </ul>
      <button className="accent" onClick={() => dispatch({ type: 'OPEN_NEXT_DOOR' })}>
        Ouvrir la première porte
      </button>
    </div>
  )
}
