# Disaster Recovery

## 1. Objectives

| Objective | Target | Result |
| --- | --- | --- |
| RTO (time to restore database and services) | <= 5 minutes (300 s) | **62-73 s measured, average 67 s** |
| RPO (acceptable data loss) | Up to the last backup | Equal to the backup interval (see section 3) |

## 2. Scenarios

| # | Scenario | Solution | Tested |
| --- | --- | --- | --- |
| A | A bad version was deployed | `bash scripts/rollback.sh` returns to the previous version | Yes (issue #23) |
| B | Containers, images or the database volume are lost | `bash scripts/restore.sh` | Yes (this issue) |
| C | The whole VM is lost | Build a new VM and restore (section 5) | No, documented procedure only |

## 3. Backups
- **Manual:** `bash scripts/backup.sh` creates `~/backups/unicloud-<UTC timestamp>.sql.gz` (a `pg_dump` with `--clean --if-exists`, compressed).
- **Retention:** the 7 newest files are kept.
- **Recommended schedule** (daily at 02:00 UTC), added with `crontab -e` on the VM:
  ```
  0 2 * * * cd ~/app && bash scripts/backup.sh >> ~/backups/backup.log 2>&1
  ```
  With this schedule the RPO is up to 24 hours.
- **Copy outside the VM** (needed for scenario C), run on your own computer:
  ```
  scp <VM_USER>@<VM_HOST>:~/backups/<file>.sql.gz .
  ```

## 4. Restore database and services (scenario B)
Run on the VM:

1. `cd ~/app`
2. `bash scripts/restore.sh` (newest backup) or `bash scripts/restore.sh ~/backups/<file>.sql.gz`
3. Verify:
   - `docker ps` shows `deploy-demo-web-1` and `deploy-demo-db-1` running
   - `curl http://localhost:8080/version.txt` returns the SHA from `~/deploy-state/current.env`

What `restore.sh` does: starts the `db` container, waits until its healthcheck is `healthy`, loads the SQL dump with `ON_ERROR_STOP=1`, then starts all services. Missing images (including the `web` image from GHCR) are pulled automatically.

Requirement: the VM must be able to pull the image from GHCR. The package must be public, or `docker login ghcr.io` must be done with a token that has `read:packages`.

## 5. Full VM loss (scenario C)
1. Create a new Ubuntu 22.04 VM in Oracle Cloud and open ports 22, 80 and 8080.
2. Install Docker and Docker Compose v2, and clone the repository to `~/app`.
3. Update the GitHub secrets `VM_HOST` (and `VM_SSH_KEY` / `VM_USER` if they changed).
4. If the package is private, run `docker login ghcr.io`.
5. Deploy the last known version: `bash scripts/deploy.sh <commit sha> <owner>`. This recreates `~/deploy-state`.
6. Copy the latest backup from your offsite copy into `~/backups/` on the new VM.
7. `bash scripts/restore.sh`
8. Verify as in section 4.

## 6. Disaster recovery test
Script: `scripts/dr-test.sh`, run on the VM.

| Step | Action |
| --- | --- |
| 1. Seed and backup | Starts the services, creates the table `dr_check` if missing, inserts 100 rows, records the row count, and takes a backup |
| 2. Disaster | `docker compose down -v` (containers, network and database volume) and `docker image prune -af` (all images) |
| 3. Recovery (timer starts) | Runs `restore.sh`, then waits until `/version.txt` returns the expected SHA (timer stops) |
| 4. Verification | Row count after equals row count before, the version matches the deployed SHA, and time is <= 300 s |

The measured time includes pulling the PostgreSQL and web images from the registries, the database restore and the service start-up.

### Results

| Run | Date (UTC) | Rows before / after | Version | Recovery time | Result |
| --- | --- | --- | --- | --- | --- |
| 1 | 2026-11-21 | 100 / 100 | b2988b5 | 73 s | PASS |
| 2 | 2026-11-21 | 200 / 200 | b2988b5 | 66 s | PASS |
| 3 | 2026-11-21 | 300 / 300 | b2988b5 | 62 s | PASS |

The row count grows by 100 per run because each run adds rows to the table restored by the previous run.

**Conclusion:** the average recovery time is 67 s (maximum 73 s), well below the 300 s limit. The requirement "recovery <= 5 min is measured" is met for scenario B.

## 7. Known risks and next steps
- Backups live on the same VM. If the VM is lost, so are the backups, unless copied offsite (section 3). Automating an offsite copy (for example to Oracle Object Storage, which has a free tier) would close this gap.
- Scenario C is a documented procedure and has not been timed.
- Recovery depends on GitHub (GHCR) and Docker Hub being reachable.
- The test used demo data. Restore time will grow with the real database size, so re-measure once real data exists.
- The cron schedule is recommended but must be enabled by an operator on the VM.