import { defineConfig } from 'vitest/config'

// Scoped to pure engine functions only, not UI/component tests — issue #10.
export default defineConfig({
  test: {
    include: ['src/engine/**/*.test.ts'],
    environment: 'node',
  },
})
