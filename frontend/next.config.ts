import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Enables minimal standalone bundle for Docker deployment
  output: "standalone",
};

export default nextConfig;
