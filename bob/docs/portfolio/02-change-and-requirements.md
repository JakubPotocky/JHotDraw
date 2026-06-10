# 02 Change and Requirements

## User Story

> As a user editing a drawing, I want to copy selected figures and paste them back into the drawing, so that I can duplicate existing work without recreating it manually.

## Acceptance Criteria

1. Copy creates a transferable from selected figures.
2. Paste imports supported transferable data into the active drawing.
3. Pasted figures become selected.
4. Paste produces undoable behavior.
5. Empty selection does not create a figure copy.
6. Unsupported data flavors are rejected.
7. Registered input/output formats drive supported data types.

## Maintenance Request

Analyse the existing feature and improve maintainability with a small safe refactor and tests. Do not rebuild copy/paste.

## Why This Is a Good Maintenance Case

Copy/Paste is small enough to trace but broad enough to show maintenance concepts:

- UI actions
- Swing transfer infrastructure
- clipboard abstraction
- strategy pattern
- domain model objects
- undo behavior
- sample application configuration
