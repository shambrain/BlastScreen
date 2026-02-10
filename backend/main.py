from collections import defaultdict, deque
from dataclasses import dataclass
from datetime import datetime, timezone
from uuid import uuid4

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(title="BlastScreen Backend", version="3.0.0")


class MatchRequest(BaseModel):
    userId: str
    region: str = "global"


class MatchResponse(BaseModel):
    roomId: str
    roomUrl: str
    partnerId: str
    ttlSeconds: int = 180


class LeaveRequest(BaseModel):
    userId: str
    region: str = "global"


@dataclass
class WaitingUser:
    user_id: str
    queued_at: datetime


queues: dict[str, deque[WaitingUser]] = defaultdict(deque)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "version": app.version}


@app.post("/match", response_model=MatchResponse)
def match(request: MatchRequest) -> MatchResponse:
    queue = queues[request.region]

    while queue:
        candidate = queue.popleft()
        if candidate.user_id != request.userId:
            room_id = f"BlastScreen-{uuid4().hex[:10]}"
            return MatchResponse(
                roomId=room_id,
                roomUrl=f"https://meet.jit.si/{room_id}",
                partnerId=candidate.user_id,
            )

    queue.append(WaitingUser(user_id=request.userId, queued_at=datetime.now(timezone.utc)))
    waiting_room = f"BlastScreen-WAIT-{request.userId[:6]}"
    return MatchResponse(
        roomId=waiting_room,
        roomUrl=f"https://meet.jit.si/{waiting_room}",
        partnerId="waiting",
    )


@app.post("/leave")
def leave(request: LeaveRequest) -> dict[str, str]:
    queue = queues[request.region]
    queues[request.region] = deque(item for item in queue if item.user_id != request.userId)
    return {"status": "removed"}
