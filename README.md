# portfolio-api — Google One Tap Soft-Auth Gateway

A reusable Spring Boot backend that gates arbitrary contact/protected data behind Google Sign-In. Any static site can deploy this image with different env vars.

## How it works

1. Frontend fetches `/api/config` to get the Google Client ID.
2. Google One Tap prompt appears; on success the credential JWT is POSTed to `/api/auth/google`.
3. Backend verifies the token with Google's public keys, creates a server-side session (stored in Redis).
4. Frontend fetches `/api/protected` (session cookie sent automatically); backend returns whatever `PROTECTED_*` env vars were configured.
5. Sign-in events are logged to MongoDB for owner review at `/api/admin/sign-ins`.

## Environment variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `GOOGLE_CLIENT_ID` | yes | — | OAuth 2.0 client ID from Google Cloud Console |
| `OWNER_GOOGLE_EMAIL` | yes | — | Google email that can access `/api/admin/sign-ins` |
| `SERVICE_NAME` | no | `portfolio` | Prefix for MongoDB collection and Redis session namespace |
| `PROTECTED_*` | no | — | Any key prefixed `PROTECTED_` is exposed after sign-in (e.g. `PROTECTED_EMAIL`, `PROTECTED_PHONE`) |
| `MONGODB_URI` | no | `mongodb://mongodb.mongodb.svc.cluster.local:27017/portfolio-auth` | MongoDB connection string |
| `REDIS_HOST` | no | `redis.redis.svc.cluster.local` | Redis host for session storage |
| `REDIS_PORT` | no | `6379` | Redis port |

## Google Cloud Console setup

1. Go to APIs & Services → Credentials → Create OAuth 2.0 Client ID (Web application).
2. Authorized JavaScript origins: `https://your-site.kamradtfamily.net`
3. No redirect URIs needed (One Tap uses postMessage, not redirect).
4. Copy the Client ID into the K8s secret as `GOOGLE_CLIENT_ID`.

## K8s setup steps

1. Copy `secret-template.yaml`, fill in real values, apply:
   ```bash
   kubectl apply -f secret-template-filled.yaml
   ```
2. Copy `ghcr-secret` into the `portfolio-api` namespace (created by ArgoCD on first sync):
   ```bash
   kubectl get secret ghcr-secret -n receipts -o yaml \
     | sed 's/namespace: receipts/namespace: portfolio-api/' \
     | kubectl apply -f -
   ```
3. Add `portfolio-api` entry to `rkamradt-helm-charts/apps/values.yaml` (already done).
4. ArgoCD will auto-sync and deploy.
5. Add Cloudflare route if needed (this service is typically accessed via the nginx proxy in the frontend, not directly).
