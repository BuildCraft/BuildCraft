# BuildCraft 8.0.x — Developer Guide

Instructions for **building, testing and depending on** BuildCraft `8.0.x-1.12.2`
(the branch this repo is on). This fills the gap noted in `README.md`
("8.0.x hasn't been finished yet, so there are no instructions for depending on it").

> **Status:** 8.0.x is a work-in-progress branch. APIs may still change.

---

## 1. Prerequisites

| Tool | Version | Why |
|------|---------|-----|
| **JDK** | **Java 8 (1.8)** | ForgeGradle 2.3 runs on Gradle 4.3.1, which **requires Java 8**. Newer JDKs (11/17/21) will fail with class-loading errors. |
| Git | any recent | Repo uses 3 submodules (must be cloned recursively). |
| Internet | required once | First build downloads Minecraft 1.12.2 + Forge + mappings (hundreds of MB) and deobfuscates them. |

### Clone (recursive — do not skip)

```bash
git clone --recursive https://github.com/ghshhf/BuildCraft.git
cd BuildCraft
```

Submodules: `BuildCraftAPI`, `BuildCraftGuide`, `BuildCraft-Localization`.

---

## 2. First-time workspace setup (deobfuscation)

ForgeGradle must generate a **deobfuscated** Minecraft + Forge classpath before
anything can compile (vanilla MC ships obfuscated as `aap` / `func_123456_a`).

```bash
# generates the mapped classpath (class names only, fast-ish)
./gradlew setupDevWorkspace

# OR, if you also need deobfuscated *sources* for IDE debugging:
./gradlew setupDecompWorkspace
```

This step downloads Gradle 4.3.1, Minecraft 1.12.2, Forge and the mapping
tables, then decompiles. On a mechanical disk expect **10–30 minutes**; it is a
one-time cost (cached in `GRADLE_USER_HOME`).

> **Windows / non-ASCII paths:** Gradle 4.3.1 mis-parses classpaths under paths
> containing non-ASCII characters (e.g. `项目改变`). Keep the repo on an ASCII
> path such as `E:/BuildCraft`.

---

## 3. Building

```bash
./gradlew build -x test      # produce jars in build/libs/*.jar
./gradlew build              # full build incl. unit tests
```

---

## 4. Running the tests

### 4.1 Unit tests (JUnit, via Gradle)

```bash
./gradlew test
```

Results: `build/test-results/test/TEST-*.xml`. CI publishes these as JUnit
reports (see `.github/workflows/ci.yml`).

### 4.2 In-game test harness

BuildCraft ships a headless Minecraft test harness that boots a real server with
pre-built test worlds and runs scripted sequences.

```bash
python test.py            # launches ServerLaunchWrapper --test test.seq --quit
```

Test worlds live under `testsuite/` (e.g. `testsuite/base`, `testsuite/1732-01`,
`testsuite/1846`); the run order is defined in `test.seq`.

### 4.3 Fast pure-subset unit tests (no Forge needed)

A subset of the codebase is **pure Java with zero Minecraft dependencies** and can be
compiled and tested with plain `javac` + JUnit 4 — no `setupDevWorkspace`
required. Useful for quick feedback / CI pre-checks:

| Module | How to run | Coverage |
|--------|-------------|----------|
| Expression engine (`sub_projects/expression`) | standalone Gradle 4.3.1 project; run its `test` task or `javac`+JUnit directly | 59 tests |
| Pure lib utils (`common/buildcraft/lib/misc/`) | `javac` + JUnit4: `MathUtil`, `ArrayUtil`, `TimeUtil`, `ObjectUtilBC` | 21 tests |

> These are the only parts testable without the deobfuscated Forge classpath.
> Everything else (`net.minecraft.*` / `net.minecraftforge.*` coupled code) needs §2 first.

---

## 5. Continuous Integration

CI moved from Jenkins to **GitHub Actions** (see `.github/workflows/ci.yml`):
JDK 8 (temurin) + recursive submodules → `./gradlew build -x test` (archives
`build/libs/*.jar`) → `./gradlew test` (publishes + archives `TEST-*.xml`).

---

## 6. Dependency-source notes (build health)

Two dead repositories were removed from `build.gradle` because their hosts shut down:

- `jcenter()` — JFrog discontinued JCenter (2021). Replaced by the Aliyun
  public mirror (`https://maven.aliyun.com/repository/public`).
- `https://oss.sonatype.org/content/repositories/snapshots/` — Sonatype
  sunset OSSRH (2025-03). The ForgeGradle `2.3-SNAPSHOT` artifact is served
  by `files.minecraftforge.net/maven`, already present in the repositories block,
  so the Sonatype entry was simply dropped.

If you add a new plugin/classpath dependency, prefer `mavenCentral()` or the
Forge/Mojang-hosted mavens already configured.

---

## 7. Depending on BuildCraft (for other mods)

See the **7.99.12 (1.12.2)** instructions in `README.md` (section
*Depending on BuildCraft*). 8.0.x Maven coordinates are not yet published; for
now, build from source (§3) and consume the `build/libs/*-api.jar` / `*-lib.jar`
artifacts locally, or install them into your own `mavenLocal()`.
