# 03 Concept Location

## Technique Used

I used static concept location with code search and source tracing. I did not run an interactive debugger, so this file does not claim dynamic debugger evidence.

Useful commands:

```bash
rg -n "class CopyAction|class PasteAction|class ClipboardUtil|class DefaultDrawingViewTransferHandler" .
rg -n "interface InputFormat|interface OutputFormat|class DOMStorableInputOutputFormat" .
```

## Runtime Path From Source Trace

Copy:

1. `CopyAction.actionPerformed()` locates a target `JComponent`.
2. It calls `c.getTransferHandler().exportToClipboard(...)`.
3. `DefaultDrawingView` has installed `DefaultDrawingViewTransferHandler`.
4. `DefaultDrawingViewTransferHandler.createTransferable(...)` collects selected figures.
5. The drawing sorts figures in z-order.
6. Registered `OutputFormat`s create transferables.
7. `CompositeTransferable` combines data flavors.
8. `ClipboardUtil.getClipboard()` supplies the clipboard.

Paste:

1. `PasteAction.actionPerformed()` gets clipboard contents.
2. It calls `c.getTransferHandler().importData(...)`.
3. `DefaultDrawingViewTransferHandler.importData(...)` compares transferable flavors with drawing `InputFormat`s.
4. `DOMStorableInputOutputFormat.read(...)` reads native figure data.
5. Imported figures are added to the drawing.
6. The transfer handler selects pasted figures and fires an undoable paste edit.

## Initial Domain Classes

| Domain Class | Responsibility |
|---|---|
| `CopyAction` | Controller entry point for Copy. Delegates export to Swing transfer handling. |
| `PasteAction` | Controller entry point for Paste. Delegates import to Swing transfer handling. |
| `ClipboardUtil` | Provides the clipboard abstraction and fallback behavior. |
| `DefaultDrawingView` | Drawing UI component that owns selection state and installs the transfer handler. |
| `DefaultDrawingViewTransferHandler` | Main Copy/Paste coordinator for drawing views. |
| `CompositeTransferable` | Combines multiple transferable data flavors. |
| `Drawing` | Owns figures and registered input/output formats. |
| `Figure` | Domain object being copied, serialized, pasted, selected, and transformed. |
| `InputFormat` | Strategy for reading content from files, streams, or transferables. |
| `OutputFormat` | Strategy for writing figures and creating transferables. |
| `DOMStorableInputOutputFormat` | Native JHotDraw DOM/XML copy/paste format. |
| `ImageOutputFormat` | Exports copied figures as image data. |
| `TextInputFormat` | Imports plain text as text-holder figures. |
| `DrawView` | Sample draw setup that registers copy/paste formats. |
| `SVGApplet` | SVG sample setup that registers SVG/image formats. |

## Debugger Plan

Recommended breakpoints:

- `CopyAction.actionPerformed()`
- `PasteAction.actionPerformed()`
- `DefaultDrawingViewTransferHandler.createTransferable()`
- `DefaultDrawingViewTransferHandler.importData()`
- `DOMStorableInputOutputFormat.createTransferable()`
- `DOMStorableInputOutputFormat.read(...)`
- `ClipboardUtil.getClipboard()`

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
