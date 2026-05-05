# Branch map

This project is split into three feature branches for GitHub review:

- `feature/database`: SQLite schema and persistence logic for users, workouts, analysis, recommendations, and saved videos.
- `feature/tracker`: CameraX and MediaPipe workout tracking, rep counting, form scoring, and live feedback.
- `feature/statistics`: Progress dashboard data, period filters, trends, session history, and coaching statistics.

Start from `main`, then push each branch independently:

```sh
git push -u origin main
git push -u origin feature/database
git push -u origin feature/tracker
git push -u origin feature/statistics
```
