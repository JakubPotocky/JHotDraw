# 04 Impact Analysis

## Change Under Analysis

The maintenance change concerns existing Copy/Paste behavior. Any change can cross from UI action code into transfer infrastructure, drawing selection, format strategies, and sample application setup.

## Packages Visited

| Package name | # of classes visited | Comments |
|---|---:|---|
| `org.jhotdraw.action.edit` | 2 | `CopyAction` and `PasteAction` are command entry points. |
| `org.jhotdraw.datatransfer` | 3 | Clipboard and transferable infrastructure. |
| `org.jhotdraw.draw` | 6 | Drawing view, transfer handler, drawing model, undo/selection behavior. |
| `org.jhotdraw.draw.io` | 5 | Input/output format strategies used for copy/paste. |
| `org.jhotdraw.draw.figure` | 5 | Figure abstractions and concrete figures being copied. |
| `org.jhotdraw.samples.draw` | 2 | Registers DOM, image, and text formats for the Draw sample. |
| `org.jhotdraw.samples.svg` | 1 | Registers SVG/image formats for the SVG sample. |

## Impact Conclusion

The safest implementation approach is narrow:

- keep actions thin;
- keep format selection in `DefaultDrawingViewTransferHandler`;
- keep serialization inside `InputFormat`/`OutputFormat`;
- keep production behavior unchanged except for small maintainability refactoring;
- test the native DOM transferable path without launching the full GUI.

## Main Risks

- Changing data-flavor preference can change which format is pasted.
- Changing `DefaultDrawingViewTransferHandler.importData()` can affect paste and drag/drop.
- Changing `DOMStorableInputOutputFormat` can break native figure copy/paste.
- Changing selection updates can paste figures but leave the UI in the wrong state.
- Changing undo edit logic can make paste difficult to reverse.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
