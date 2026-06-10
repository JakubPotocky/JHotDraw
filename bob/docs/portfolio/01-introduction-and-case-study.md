# 01 Introduction and Case Study

## Case Study

The project is JHotDraw, a Java/Swing drawing framework with several sample applications. The selected feature is Basic Editing - Copy/Paste.

This feature is already implemented. The maintenance task is to understand it, document it, add focused tests, and keep refactoring small.

## Build and Run Evidence

The requested build command succeeded:

```bash
mvn clean install -DskipTests
```

The SVG sample launch command was attempted:

```bash
timeout 20s mvn exec:java "-Dexec.mainClass=org.jhotdraw.samples.svg.Main"
```

It initialized and then was stopped by timeout. No manual GUI interaction is claimed.

## Selected Feature Entry Points

- `jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/CopyAction.java`
- `jhotdraw-actions/src/main/java/org/jhotdraw/action/edit/PasteAction.java`
- `jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java`
- `jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/ClipboardUtil.java`
