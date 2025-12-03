# SonarQube Setup Guide

This project is configured to use SonarCloud with the **mbatchelor81** organization.

## Prerequisites

1. **SonarCloud Account**: Ensure you have access to the mbatchelor81 organization on SonarCloud
2. **SonarCloud Token**: Generate a token from [SonarCloud Account Security](https://sonarcloud.io/account/security)

## GitHub Actions Setup

### 1. Add SonarCloud Token to GitHub Secrets

1. Go to your GitHub repository settings
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add the following secret:
   - **Name**: `SONAR_TOKEN`
   - **Value**: Your SonarCloud token

### 2. Create Project on SonarCloud

1. Go to [SonarCloud](https://sonarcloud.io)
2. Click the **+** icon → **Analyze new project**
3. Select your GitHub repository
4. Choose **mbatchelor81** as the organization
5. The project key should be: `mbatchelor81_spring-boot-realworld-example-app`

### 3. Trigger Analysis

The SonarQube analysis will automatically run on:
- Push to `main`, `master`, or `develop` branches
- Pull requests (opened, synchronized, or reopened)

## Local Analysis

To run SonarQube analysis locally:

### 1. Set Environment Variable

```bash
export SONAR_TOKEN=your_sonarcloud_token_here
```

### 2. Run Analysis

```bash
./gradlew clean build test jacocoTestReport sonarqube
```

## Configuration Details

The SonarQube configuration in `build.gradle` includes:

- **Project Key**: `mbatchelor81_spring-boot-realworld-example-app`
- **Organization**: `mbatchelor81`
- **Host**: `https://sonarcloud.io`
- **Code Coverage**: Integrated with JaCoCo (80% minimum coverage)
- **Source Directories**: `src/main/java`
- **Test Directories**: `src/test/java`

## Viewing Results

After analysis completes:

1. Go to [SonarCloud Dashboard](https://sonarcloud.io/organizations/mbatchelor81/projects)
2. Select your project: `spring-boot-realworld-example-app`
3. View code quality metrics, bugs, vulnerabilities, code smells, and coverage

## Quality Gates

The default SonarCloud quality gate checks:
- New code coverage ≥ 80%
- No new bugs
- No new vulnerabilities
- Security hotspots reviewed
- Maintainability rating A

## Troubleshooting

### Build Fails on SonarQube Step

- Verify `SONAR_TOKEN` is correctly set in GitHub Secrets
- Check that the project exists on SonarCloud
- Ensure the project key matches: `mbatchelor81_spring-boot-realworld-example-app`

### Coverage Not Showing

- Ensure tests run before SonarQube analysis: `./gradlew test jacocoTestReport sonarqube`
- Check that JaCoCo XML report is generated in `build/reports/jacoco/test/jacocoTestReport.xml`

### Permission Issues

- Verify you have admin access to the mbatchelor81 organization
- Regenerate your SonarCloud token if needed
