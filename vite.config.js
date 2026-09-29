import { defineConfig } from "vite";

// Vite 默认以 / 作为 base。在 Capacitor WebView 里我们通过 https://localhost 访问，
// 因此保持根路径即可；如需部署到子路径，可改为 base: "./"。
export default defineConfig({
  root: "src",
  base: "./",
  build: {
    outDir: "../dist",
    emptyOutDir: true,
  },
});