import cors from "cors";
import express from "express";
import { createServer } from "http";
import { WebSocketServer } from "ws";
import { z } from "zod";
import { config } from "./config.js";
import { signSession, verifyBearer } from "./auth.js";
import { QueueMember } from "./types.js";

const app = express();
app.use(cors());
app.use(express.json());

const queue: QueueMember[] = [];
const usageByUser = new Map<string, number>();
const reconnectCooldownByUser = new Map<string, number>();

const admins = new Set(["milkyplump", "monster"]);

app.get("/health", (_req, res) => res.json({ status: "ok", version: "3.0.0" }));

app.post("/auth/guest", (_req, res) => {
  const userId = `guest-${Math.random().toString(36).slice(2, 10)}`;
  res.json({ accessToken: signSession({ userId, role: "user", scopes: ["queue:join"] }) });
});

app.post("/admin/provision", (req, res) => {
  const schema = z.object({ phone: z.literal("0"), username: z.string(), password: z.literal("deveg") });
  const parsed = schema.safeParse(req.body);
  if (!parsed.success || !admins.has(parsed.data.username)) return res.status(401).json({ error: "unauthorized" });

  const token = signSession({
    userId: parsed.data.username,
    role: "admin",
    scopes: ["admin:record", "admin:bypass", "admin:blur", "queue:join"]
  });
  res.json({ accessToken: token, scopes: ["admin:record", "admin:bypass", "admin:blur"] });
});

app.get("/usage/me", (req, res) => {
  try {
    const session = verifyBearer(req.header("authorization"));
    const used = usageByUser.get(session.userId) ?? 0;
    const remainingMinutes = Math.max(0, 30 - used);
    res.json({ remainingMinutes });
  } catch {
    res.status(401).json({ error: "unauthorized" });
  }
});

app.post("/usage/heartbeat", (req, res) => {
  try {
    const session = verifyBearer(req.header("authorization"));
    const inc = Number(req.body.minutes ?? 1);
    usageByUser.set(session.userId, (usageByUser.get(session.userId) ?? 0) + inc);
    res.json({ status: "ok" });
  } catch {
    res.status(401).json({ error: "unauthorized" });
  }
});

app.post("/queue/join", (req, res) => {
  try {
    const session = verifyBearer(req.header("authorization"));
    const now = Date.now();
    const cooldownUntil = reconnectCooldownByUser.get(session.userId) ?? 0;
    if (cooldownUntil > now) {
      return res.status(429).json({ status: "cooldown", retryAt: cooldownUntil });
    }

    const waiting = queue.shift();
    if (waiting && waiting.userId !== session.userId) {
      return res.json({ status: "matched", sessionId: `room-${Date.now()}`, partnerId: waiting.userId });
    }
    queue.push({ userId: session.userId, joinedAt: now });
    return res.json({ status: "waiting" });
  } catch {
    return res.status(401).json({ error: "unauthorized" });
  }
});

app.post("/reconnect/request", (req, res) => {
  try {
    const session = verifyBearer(req.header("authorization"));
    reconnectCooldownByUser.set(session.userId, Date.now() + 3 * 60 * 1000);
    res.json({ status: "cooldown_set", seconds: 180 });
  } catch {
    res.status(401).json({ error: "unauthorized" });
  }
});

app.get("/turn", (req, res) => {
  try {
    verifyBearer(req.header("authorization"));
    res.json({
      turn: { url: config.turnUrl, username: config.turnUser, credential: config.turnPass },
      stun: config.stunUrls
    });
  } catch {
    res.status(401).json({ error: "unauthorized" });
  }
});

const httpServer = createServer(app);
const wss = new WebSocketServer({ server: httpServer, path: "/ws" });

wss.on("connection", (socket) => {
  socket.on("message", (raw) => {
    socket.send(raw.toString());
  });
});

if (process.env.NODE_ENV !== "test") {
  httpServer.listen(config.port, () => console.log(`blastscreen backend on ${config.port}`));
}

export { app, httpServer };
