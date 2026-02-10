import dotenv from "dotenv";

dotenv.config();

function required(key: string): string {
  const value = process.env[key];
  if (!value) throw new Error(`Missing required env: ${key}`);
  return value;
}

export const config = {
  port: Number(process.env.PORT ?? 8080),
  jwtSecret: required("JWT_SECRET"),
  turnUrl: required("TURN_URL"),
  turnUser: required("TURN_USER"),
  turnPass: required("TURN_PASS"),
  stunUrls: (process.env.STUN_URLS ?? "stun:stun.l.google.com:19302").split(",")
};
