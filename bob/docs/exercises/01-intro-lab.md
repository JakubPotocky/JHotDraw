# 01 Intro Lab

## Case Study

The case study is JHotDraw, a Java/Swing drawing framework and sample application. My selected feature is the existing Basic Editing - Copy/Paste behavior.

Main feature classes:

- `CopyAction`
- `PasteAction`
- `ClipboardUtil`
- `DefaultDrawingViewTransferHandler`
- `DefaultDrawingView`
- `Drawing`
- `Figure`
- `InputFormat`
- `OutputFormat`
- `DOMStorableInputOutputFormat`

## Build

Command:

```bash
mvn clean install -DskipTests
```

Result: **BUILD SUCCESS**.

The full Maven reactor built with Java 17 and Maven 3.8.7. Tests were skipped because the command explicitly used `-DskipTests`.

## Git Branch

The Copy/Paste lab work is on the local feature branch:

```bash
copy-paste-basic-editing-labs
```

This branch was created from `develop` for the lab work. The files still need to be committed and pushed before the branch exists on GitHub.

## GUI Launch Attempt

Command run from `jhotdraw-samples/jhotdraw-samples-misc`:

```bash
timeout 20s mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
```

Result: the command was stopped by the 20 second timeout with exit code `124`.

The output showed the SVG application initialized actions and menus, including copy/paste action resources. It also printed missing icon warnings and a Java preferences/XML provider warning near shutdown. I did not manually test the GUI, so I do not claim interactive GUI validation.

## Detailed Combined Note

The longer original intro note is available at:

- [`../../Labs.md`](../../Labs.md)
