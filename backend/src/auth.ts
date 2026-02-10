import jwt from "jsonwebtoken";
import { config } from "./config.js";
import { UserSession } from "./types.js";

export function signSession(session: UserSession): string {
  return jwt.sign(session, config.jwtSecret, { expiresIn: "12h" });
}

export function verifyBearer(auth?: string): UserSession {
  if (!auth?.startsWith("Bearer ")) throw new Error("Missing bearer token");
  const token = auth.slice("Bearer ".length);
  return jwt.verify(token, config.jwtSecret) as UserSession;
}
