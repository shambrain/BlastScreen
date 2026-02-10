# ML Models

Admin-only blur rule:
- Blur only when full-body framing (head to waist) is detected.
- No blur for face-only framing.

Pipeline plan:
- Pose model (MoveNet class)
- Face detector
- Rule engine with confidence threshold and temporal smoothing
