# 07 Actualization: Clean Architecture and SOLID

## SOLID Examples

| Principle | Copy/Paste example |
|---|---|
| Single Responsibility | `CopyAction` and `PasteAction` only translate user commands into transfer calls. Serialization lives elsewhere. |
| Open/Closed | New clipboard/file formats can be added through `InputFormat` and `OutputFormat` without rewriting actions. |
| Liskov Substitution | The transfer handler works with `Drawing` and `Figure` abstractions, not one concrete drawing or figure. |
| Interface Segregation | Input and output are separate strategy interfaces. `ImageOutputFormat` does not need input behavior. |
| Dependency Inversion | High-level copy/paste actions depend on Swing and JHotDraw abstractions instead of concrete DOM serialization classes. |

## Clean Architecture Mapping

| Layer | Classes |
|---|---|
| Action/controller | `CopyAction`, `PasteAction` |
| Coordination | `DefaultDrawingViewTransferHandler` |
| Strategy | `InputFormat`, `OutputFormat`, `DOMStorableInputOutputFormat`, `ImageOutputFormat`, `TextInputFormat` |
| Domain/model | `Drawing`, `Figure`, concrete figure classes |
| Infrastructure | `ClipboardUtil`, `CompositeTransferable`, Swing `TransferHandler`, Java `Clipboard`, `DataFlavor` |

## Key Design Point

Copy/Paste is format-driven. The same user action can copy native JHotDraw XML, expose image data, or paste text depending on the formats registered by the drawing.

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
