# Infrastructure

## 1. Overview
The UniCloud demo environment runs on a single virtual machine in Oracle Cloud (Always Free tier, Frankfurt region). Services run as Docker containers managed by Docker Compose. Container images are built in GitHub Actions and stored in GitHub Container Registry (GHCR).

| Parameter | Value |
| --- | --- |
| Provider | Oracle Cloud Always Free |
| Region | Frankfurt |
| Shape | VM.Standard.E2.1.Micro (1 GB RAM) |
| OS | Ubuntu 22.04 |
| Access | SSH with key authentication (no password login) |
| Open ports | 22 (SSH), 80, 8080 (web) |
| Runtime | Docker + Docker Compose v2 |
| Image registry | ghcr.io |

The public IP address is not stored in the repository. It is kept in the GitHub secret `VM_HOST`.

## 2. Services
Defined in `deploy-demo/compose.yml`.

| Service | Image | Port | Persistent data |
| --- | --- | --- | --- |
| `web` | `ghcr.io/<owner>/unicloud-demo:<commit sha>` | 8080 -> 80 | none |
| `db` | `postgres:16-alpine` | internal only (not published to the host) | Docker volume `pgdata` |

- Both services use `restart: unless-stopped`.
- `db` has a healthcheck (`pg_isready`), so other steps can wait until the database is ready.
- The database name and user are `unicloud`. The password comes from the `POSTGRES_PASSWORD` environment variable and defaults to a demo value (`unicloud_demo`). Set a real password before using real data.
- The `web` image serves a static page and `/version.txt`, which contains the deployed commit SHA. It is used to verify which version is live.

## 3. CI/CD
Workflows are in `.github/workflows/`.

| Workflow | Trigger | What it does |
| --- | --- | --- |
| `ci.yml` | pull request, push to `main` | Builds the placeholder image. Runs backend (Maven) and frontend (npm) build and tests only when `backend/unicloud/pom.xml` or `frontend/package.json` exist. |
| `deploy.yml` | push to `main` (and `add-cicd` while testing) | Builds the image tagged with the commit SHA, pushes it to GHCR, deploys to the VM over SSH, then checks that `/version.txt` returns the new SHA (6 attempts, 5 s apart). |

GitHub secrets used by the deploy workflow: `VM_HOST`, `VM_USER`, `VM_SSH_KEY`. The package registry login uses the built-in `GITHUB_TOKEN`. The VM logs out of GHCR after each deploy.

## 4. State on the VM

| Path | Content |
| --- | --- |
| `~/app` | Clone of the repository |
| `~/deploy-state/current.env` | Currently deployed `IMAGE_TAG` and `OWNER` (used by every compose command) |
| `~/deploy-state/history` | One line per deployed version, used by rollback |
| `~/backups/` | Database backups (`unicloud-<UTC timestamp>.sql.gz`), last 7 kept |
| Docker volume `deploy-demo_pgdata` | PostgreSQL data |

## 5. Operations scripts
All scripts are in `scripts/` and are run from the VM.

| Script | Purpose |
| --- | --- |
| `deploy.sh <sha> [owner]` | Records the version, then starts or updates all services |
| `rollback.sh` | Returns to the previous version from `history` |
| `backup.sh` | Creates a compressed SQL dump of the database in `~/backups` and keeps the last 7 |
| `restore.sh [file]` | Starts the database, loads the given dump (default: the newest), and starts all services |
| `dr-test.sh` | Disaster recovery test: seeds data, backs up, destroys everything, restores, measures time and verifies |

`.gitattributes` forces LF line endings for `*.sh` so scripts work on Linux when edited on Windows.

## 6. Limitations
- Single VM: there is no high availability. Loss of the VM means downtime until it is rebuilt (see `DISASTER_RECOVERY.md`).
- Backups are stored on the same VM. A copy outside the VM must be taken manually.
- 1 GB RAM limits the environment to lightweight services.
- The `web` service is a placeholder demo. The real backend and frontend will be added to the same compose file when their code is ready.