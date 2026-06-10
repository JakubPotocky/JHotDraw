# 06 Refactoring

## Tooling Limitation

I checked for SonarLint with:

```bash
command -v sonarlint
```

No SonarLint executable was available, so the refactoring lab used manual inspection.

## Smell 1: Repeated Paste Import/Undo Logic

Class:

```text
jhotdraw-core/src/main/java/org/jhotdraw/draw/DefaultDrawingViewTransferHandler.java
```

The copy/paste path had repeated logic for:

- reading data through an `InputFormat`;
- finding newly imported figures;
- selecting imported figures;
- moving imported figures to a drop point;
- creating the paste undo edit.

The refactored code centralizes this with helper methods:

- `importTransferData(...)`
- `firePasteUndoableEdit(...)`

## Smell 2: Non-final Stable Fields

Class:

```text
jhotdraw-datatransfer/src/main/java/org/jhotdraw/datatransfer/CompositeTransferable.java
```

The internal collection references are initialized once and never reassigned. They were marked `final`:

```java
private final HashMap<DataFlavor, Transferable> transferables = new HashMap<>();
private final LinkedList<DataFlavor> flavors = new LinkedList<>();
```

This preserves behavior and clarifies the invariant.

## Verification

Commands:

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
mvn test --file pom.xml
```

Both succeeded.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
