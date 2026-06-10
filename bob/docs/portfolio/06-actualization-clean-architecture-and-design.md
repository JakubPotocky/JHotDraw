# 06 Actualization: Clean Architecture and Design

## Architecture Summary

Copy/Paste has a practical layered structure:

- Actions: `CopyAction`, `PasteAction`
- Coordinator: `DefaultDrawingViewTransferHandler`
- Strategies: `InputFormat`, `OutputFormat`
- Domain model: `Drawing`, `Figure`
- Infrastructure: `ClipboardUtil`, `CompositeTransferable`, Java/Swing transfer APIs

## SOLID

| Principle | Example |
|---|---|
| SRP | Actions trigger commands; formats serialize data; figures model drawing objects. |
| OCP | New formats can be added without rewriting actions. |
| LSP | Transfer code uses `Drawing` and `Figure` abstractions. |
| ISP | Input and output strategies are separate interfaces. |
| DIP | High-level actions depend on transfer abstractions, not concrete DOM internals. |

## Design Patterns

- Strategy: `InputFormat` and `OutputFormat`
- Composite: `Drawing` and `Figure`/`CompositeFigure`
- Observer: selection and figure events in the drawing view
- Command/action: Swing `Action` classes for Copy/Paste

The design makes the feature extensible without making each action know every clipboard format.
