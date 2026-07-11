---
name: "github-actions-cicd-publish"
description: "GitHub Actions CI/CD for Maven Central auto-publish via vanniktech plugin"
type: project
lastUpdated: 2026-07-11T01:57
lastRecall: 2026-07-11T16:12
---

## GitHub Actions CI/CD for publish to Maven Central

- Workflow: `.github/workflows/release.yml`
- Triggers on tag push matching `[0-9]+.[0-9]+.[0-9]+` (e.g. `0.0.2`)
- Version is taken from tag name via `-Pversion=${{ github.ref_name }}`
- Uses `com.vanniktech.maven.publish` plugin with `automaticRelease = true`
- Requires GitHub secrets: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, `GPG_KEY_ID`, `GPG_PASSWORD`

### Secrets values (from local ~/.gradle/gradle.properties):
- MAVEN_CENTRAL_USERNAME = mavenCentralUsername (qDZwt2)
- MAVEN_CENTRAL_PASSWORD = mavenCentralPassword
- GPG_PRIVATE_KEY = signingInMemoryKey (the full armored private key)
- GPG_KEY_ID = signingInMemoryKeyId (C6C6227A)
- GPG_PASSWORD = signingInMemoryKeyPassword

### To publish new version:
```bash
git tag 0.0.X
git push origin 0.0.X
```
No "v" prefix — tag is used as-is.
