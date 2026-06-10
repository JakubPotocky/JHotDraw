# 05 Refactoring and Better Code

## Smell

The paste path in `DefaultDrawingViewTransferHandler` had repeated logic for importing figures, updating selection, moving dropped figures, and creating undo edits.

`CompositeTransferable` also had collection fields that were never reassigned but not marked `final`.

## Refactoring

The transfer handler now centralizes paste logic in helper methods:

- `importTransferData(...)`
- `firePasteUndoableEdit(...)`

`CompositeTransferable` now uses final field references for internal collections.

## Why This Is Safe

The refactoring does not change:

- action IDs;
- clipboard APIs;
- data flavors;
- serialization format;
- drawing model behavior.

It only makes existing behavior easier to maintain.

## Verification

```bash
mvn -pl jhotdraw-samples/jhotdraw-samples-misc test
mvn test --file pom.xml
```

Both succeeded.
