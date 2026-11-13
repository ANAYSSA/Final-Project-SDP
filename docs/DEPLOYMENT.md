# Deployment and maintenance

The deployment target is the existing DigitalOcean Droplet. The restaurant and HTTPS proxy have their own Compose project. Existing services are not part of this project.

## Files

- `Dockerfile`: portable multi-stage build with Java 21 and the test suite.
- `deploy/compose.yml`: lightweight runtime for a JAR built and tested locally; restaurant memory capped at 320 MB, Caddy at 96 MB.
- `deploy/Caddyfile`: HTTPS reverse proxy and 32 KB request-body limit.
- `deploy/app.yaml`: optional App Platform configuration. Creating an App Platform resource may incur an additional recurring charge; it is not the active deployment.

The public hostname uses sslip.io DNS for the server IP. The hostname depends on that external DNS service. Caddy obtains and renews a public TLS certificate. A personally owned domain can replace the hostname in `Caddyfile` later.

## Reproduce on a Droplet

Requirements: Docker Engine, the Compose plugin, an SSH account authorized for this server, and free public ports 80 and 443. Keep private SSH keys and tokens outside the repository.

1. Build and test on your computer with JDK 21:

   ```sh
   ./mvnw --batch-mode verify
   python3 scripts/check_structure.py
   java -jar target/hearth-and-table-1.0.0.jar --demo
   ```

2. Copy `deploy/compose.yml`, `deploy/Caddyfile` and the JAR to a dedicated server folder. Name the JAR `app.jar`. Make it readable by UID 10001. The current deployment folder is `/opt/hearth-and-table`.

3. Start from that folder:

   ```sh
   docker compose -p hearth-and-table up -d
   docker compose -p hearth-and-table ps
   docker compose -p hearth-and-table logs --tail=80
   ```

4. Allow incoming TCP 80 and 443 in the host and DigitalOcean firewall. Port 8080 remains inside the Docker network. Never publish it directly for this Compose deployment.

5. Verify the HTTPS homepage, `/api/health`, and a complete order in a browser.

## Updating from GitHub

GitHub Actions runs verification on pushes and pull requests. It does not automatically deploy, so an unreviewed teammate push does not replace the live application.

Pull the merged source, rerun verification, copy the new JAR as `app.jar.next`, preserve the previous JAR as `app.jar.previous`, atomically rename the new file to `app.jar`, and recreate only the application container:

```sh
docker compose -p hearth-and-table up -d --force-recreate restaurant
```

Because a bind-mounted file can retain its old inode, restarting alone is insufficient after an atomic replacement. Recreate the container. If health checks fail, restore `app.jar.previous` and recreate the application again.

## Limits of this teaching deployment

- Orders and idempotency records live in each HTTP session, not a database. Restarting/redeploying loses orders and sessions.
- Session cookies are HttpOnly, SameSite=Lax and Secure in the HTTPS deployment. Separate browser sessions cannot read each other's orders.
- Order history is bounded to 20 orders per session; a session expires after inactivity. This is suitable for the defense, not sustained commercial traffic.
- The status button deliberately simulates kitchen progress. There is no real payment processor, SMS, email or restaurant integration.
- The existing small Droplet shares CPU and RAM with another service. Watch memory during demonstrations. Do not increase infrastructure spending without agreeing on a budget.

Use `docker compose -p hearth-and-table logs --tail=100 restaurant` to inspect startup errors. Do not publish logs containing customer input or session cookies.
