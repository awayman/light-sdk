# Setup GitHub Token for Private Packages

## Purpose

Guide users through setting up a GitHub Personal Access Token (PAT) for accessing private Light SDK package dependencies from GitHub Packages. This is a required one-time setup for building light-sdk projects.

## When to Use

- First-time setup of light-sdk project
- Build fails with "authentication" or "unauthorized" errors
- User needs to regenerate or rotate their GitHub token

## Why It's Needed

Light SDK packages are published to GitHub Packages (not Maven Central), requiring authentication to download dependencies. The build will fail without a valid token:

```
Execution failed for task ':sdk:client:checkDependencies'
> Could not resolve com.thelightphone:...
  Credentials are missing
```

## Setup Steps

### Option A: Environment Variables (Recommended for CI/CD)

Set these in your shell or CI/CD system:

```bash
export GITHUB_ACTOR=your_github_username
export GITHUB_TOKEN=your_personal_access_token
```

Then verify:

```bash
./gradlew --version  # Should print without auth errors
```

### Option B: local.properties File (Local Development)

1. Create or edit `local.properties` in the project root
2. Add these lines:

```properties
gpr.user=your_github_username
gpr.key=your_personal_access_token
```

3. Save and rebuild

## Creating a GitHub Personal Access Token

1. Go to GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
2. Click **Generate new token (classic)**
3. Name it: `light-sdk-build`
4. Set expiration (recommend 90 days)
5. Select scopes: `read:packages` (minimum required)
6. Click **Generate token**
7. **Copy immediately** — you won't see it again
8. Use the token for `GITHUB_TOKEN` or `gpr.key`

## Verification

After setup, verify the token works:

```bash
./gradlew :sdk:client:dependencies
```

Should print dependency tree without authentication errors.

## Troubleshooting

| Error | Solution |
|-------|----------|
| `Credentials are missing` | Token not set or invalid. Check env vars / `local.properties` |
| `Invalid credentials` | Token has expired or been revoked. Generate a new one |
| `401 Unauthorized` | Token doesn't have `read:packages` scope |
| `403 Forbidden` | Token has insufficient permissions or repo access |

## Security Notes

- **Never commit** `local.properties` to git (it's in `.gitignore`)
- **Never hardcode** tokens in source code
- **Rotate tokens** periodically (every 90 days recommended)
- Use **environment variables** for CI/CD systems, not hardcoded tokens

---

**Related**: [README.md — Quickstart](README.md#grabbing-a-token)
