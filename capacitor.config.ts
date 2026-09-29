// Capacitor 配置。注意：@capacitor/cli 6.x 的入口未提供 defineConfig 导出，
// 因此这里直接导出一个普通对象即可。
const config = {
  appId: "com.example.yunzhou",
  appName: "云舟轻应用",
  webDir: "dist",
  server: {
    androidScheme: "https",
  },
};

export default config;