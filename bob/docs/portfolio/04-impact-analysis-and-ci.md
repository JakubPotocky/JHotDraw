# 04 Impact Analysis and CI

## Impact Analysis

| Package | Classes visited | Impact |
|---|---:|---|
| `org.jhotdraw.action.edit` | 2 | Copy/Paste action entry points |
| `org.jhotdraw.datatransfer` | 3 | Clipboard and transferable infrastructure |
| `org.jhotdraw.draw` | 6 | Drawing view, model, transfer handler, undo, selection |
| `org.jhotdraw.draw.io` | 5 | Format strategies |
| `org.jhotdraw.draw.figure` | 5 | Copied/pasted domain objects |
| `org.jhotdraw.samples.draw` | 2 | Sample format registration |
| `org.jhotdraw.samples.svg` | 1 | SVG sample format registration |

## Impact Conclusion

The copy/paste feature is intentionally spread across layers. A safe change should avoid changing data flavor semantics or model contracts. Tests now focus on both native format behavior and the `DefaultDrawingViewTransferHandler` path for copy, paste, selection, unsupported data, and undo/redo.

## CI

Workflow:

```text
.github/workflows/maven.yml
```

Command:

```bash
mvn -B clean install --file pom.xml
mvn test --file pom.xml
```

The existing workflow runs on push and pull request events targeting `develop` with Temurin JDK 23. No workflow file change is part of the current diff. The added Copy/Paste tests live under the Maven test tree, so they are picked up by the existing `mvn test --file pom.xml` command once the files are committed.
