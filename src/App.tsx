import { RunProvider, useRunState } from './state/RunContext'
import { Hud } from './components/Hud/Hud'
import { TitleScreen } from './screens/TitleScreen/TitleScreen'
import { IntroScreen } from './screens/IntroScreen/IntroScreen'
import { CombatScreen } from './screens/CombatScreen/CombatScreen'
import { EventChoiceScreen } from './screens/EventChoiceScreen/EventChoiceScreen'
import { RewardScreen } from './screens/RewardScreen/RewardScreen'
import { EndRunScreen } from './screens/EndRunScreen/EndRunScreen'

function Screens() {
  const state = useRunState()
  const showHud = state.screen !== 'title'

  return (
    <div className="app-content" style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {showHud && <Hud />}
      <div style={{ flex: 1, overflow: 'auto' }}>
        {state.screen === 'title' && <TitleScreen />}
        {state.screen === 'intro' && <IntroScreen />}
        {state.screen === 'combat' && <CombatScreen />}
        {state.screen === 'eventChoice' && <EventChoiceScreen />}
        {state.screen === 'reward' && <RewardScreen />}
        {state.screen === 'endRun' && <EndRunScreen />}
      </div>
    </div>
  )
}

export default function App() {
  return (
    <RunProvider>
      <div className="rotate-prompt">
        <p>Tourne ton téléphone en mode paysage pour jouer.</p>
      </div>
      <Screens />
    </RunProvider>
  )
}
