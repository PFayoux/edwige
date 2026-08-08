import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// PWA config per issue #11: registerType 'prompt' (not 'autoUpdate') so the
// player is asked before a stale in-progress run gets swapped under them;
// devOptions.enabled lets the service worker + offline behavior be tested in dev.
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'prompt',
      devOptions: {
        enabled: true,
      },
      manifest: {
        name: 'Chouette Roguelite',
        short_name: 'Chouette',
        description: "Roguelite léger — une chouette progresse porte après porte jusqu'à sa mort ou sa victoire.",
        theme_color: '#1a1730',
        background_color: '#1a1730',
        display: 'standalone',
        orientation: 'landscape',
        icons: [
          {
            src: 'pwa-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: 'pwa-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
        ],
      },
    }),
  ],
})
