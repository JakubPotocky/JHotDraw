# Theory Recap Mapped to Copy/Paste

## Concept Location

Concept location means finding where a user-visible feature lives in the code. For Copy/Paste, the search started from names such as `CopyAction`, `PasteAction`, `ClipboardUtil`, and `TransferHandler`.

## Scattering and Entanglement

Copy/Paste is scattered across actions, transfer infrastructure, drawing view code, format strategies, and sample setup.

It is also entangled with drag/drop because `DefaultDrawingViewTransferHandler` handles both paste and drop import paths.

## Strategy Pattern

`InputFormat` and `OutputFormat` are strategies. The transfer handler asks strategies whether they support a data flavor instead of hardcoding every clipboard type.

## SOLID

- SRP: actions, formats, figures, and clipboard utilities have separate roles.
- OCP: new formats can be registered.
- DIP: action code depends on abstractions.

## Clean Architecture

The design separates user actions, coordination, domain model, strategy implementations, and infrastructure. It is not a strict Clean Architecture implementation, but it follows the same direction of separating policy from technical details.

## Refactoring Theory

The refactoring is an extract-method style change for repeated paste logic and a small immutability/readability improvement in `CompositeTransferable`.

## Testing Theory

The tests focus on logic that can run without the GUI. That keeps feedback fast and avoids fragile Swing UI automation for this lab.
