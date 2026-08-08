import { openDB, type IDBPDatabase } from 'idb'
import type { RunState } from '../state/types'

const DB_NAME = 'chouette-save'
const STORE_NAME = 'save'
const RECORD_KEY = 'save'

/**
 * Save schema — issue #8, corrected by #24 (door count lives in
 * `doorsCleared`; `activeDoor` is the currently-open-encounter object, not a
 * number). One record. `currentRun: null` means no run in progress.
 */
export interface SaveRecord {
  bestScore: number
  currentRun: RunState | null
  unlockedAchievementIds: string[]
}

const DEFAULT_SAVE: SaveRecord = {
  bestScore: 0,
  currentRun: null,
  unlockedAchievementIds: [],
}

let dbPromise: Promise<IDBPDatabase> | null = null

function getDb() {
  if (!dbPromise) {
    dbPromise = openDB(DB_NAME, 1, {
      upgrade(db) {
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          db.createObjectStore(STORE_NAME)
        }
      },
    })
  }
  return dbPromise
}

export async function loadSave(): Promise<SaveRecord> {
  const db = await getDb()
  const record = await db.get(STORE_NAME, RECORD_KEY)
  return record ?? DEFAULT_SAVE
}

export async function writeSave(save: SaveRecord): Promise<void> {
  const db = await getDb()
  await db.put(STORE_NAME, save, RECORD_KEY)
}

/** Door-boundary checkpoint (#8): persists the in-progress run, not mid-combat state. */
export async function persistCurrentRun(currentRun: RunState | null): Promise<void> {
  const save = await loadSave()
  await writeSave({ ...save, currentRun })
}

/** End-of-run write (#24): identical for death and victory — update bestScore if beaten, clear currentRun. */
export async function persistRunEnd(doorsCleared: number): Promise<void> {
  const save = await loadSave()
  await writeSave({
    ...save,
    bestScore: Math.max(save.bestScore, doorsCleared),
    currentRun: null,
  })
}
