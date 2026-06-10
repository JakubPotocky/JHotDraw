# 03 Concept Location

## Technique

Concept location was done with static analysis and source tracing. I did not use an interactive debugger.

## Main Trace

Copy starts in `CopyAction.actionPerformed()` and delegates to the target component's `TransferHandler`.

`DefaultDrawingView` installs `DefaultDrawingViewTransferHandler`, which creates a transferable from the selected figures. The transfer handler asks each registered `OutputFormat` to produce data and combines results in `CompositeTransferable`.

Paste starts in `PasteAction.actionPerformed()`, which reads clipboard contents and calls `importData`. `DefaultDrawingViewTransferHandler` chooses a matching `InputFormat`, imports figures, selects them, and creates an undoable paste edit.

## Initial Class Set

| Class | Role |
|---|---|
| `CopyAction` | Copy command entry point |
| `PasteAction` | Paste command entry point |
| `ClipboardUtil` | Clipboard provider/fallback |
| `DefaultDrawingView` | Selection-owning Swing drawing component |
| `DefaultDrawingViewTransferHandler` | Copy/Paste coordinator |
| `CompositeTransferable` | Multi-flavor clipboard payload |
| `Drawing` | Figure container and format registry |
| `Figure` | Copied/pasted domain object |
| `InputFormat` | Paste/import strategy |
| `OutputFormat` | Copy/export strategy |
| `DOMStorableInputOutputFormat` | Native figure serialization |
| `ImageOutputFormat` | Image export data |
| `TextInputFormat` | Text paste import |
| `DrawView` | Draw sample format registration |
| `SVGApplet` | SVG sample format registration |
