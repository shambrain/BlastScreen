export type UserRole = "user" | "admin";

export interface UserSession {
  userId: string;
  role: UserRole;
  scopes: string[];
}

export interface QueueMember {
  userId: string;
  joinedAt: number;
}
