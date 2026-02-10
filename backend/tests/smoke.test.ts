import request from "supertest";
import { describe, expect, it } from "vitest";
import { app } from "../src/server.js";

describe("backend smoke", () => {
  it("health works", async () => {
    const res = await request(app).get("/health");
    expect(res.status).toBe(200);
    expect(res.body.status).toBe("ok");
  });
});
