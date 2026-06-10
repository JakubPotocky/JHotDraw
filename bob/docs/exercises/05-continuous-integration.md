# 05 Continuous Integration

## Workflow

The Maven workflow is:

```text
.github/workflows/maven.yml
```

## CI Configuration

The workflow already existed, so no workflow file change is part of the current diff.

The existing workflow runs on:

- `push` to `develop`
- `pull_request` to `develop`

It uses:

- `actions/checkout@v4`
- `actions/setup-java@v4`
- Temurin JDK 23
- Maven dependency cache
- GitHub Packages credentials setup

Workflow commands:

```bash
mvn -B clean install --file pom.xml
mvn test --file pom.xml
```

## Reasoning

The lab asks to add or verify a GitHub Actions workflow. Because `.github/workflows/maven.yml` already exists and builds/tests with Maven on push and pull-request events, I verified and documented the existing workflow instead of changing it.

Local verification was run separately with Java 17:

```bash
mvn test --file pom.xml
```

## Local Verification

Command:

```bash
mvn test --file pom.xml
```

Result: **BUILD SUCCESS**.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
