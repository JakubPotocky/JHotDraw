# 02 Change Request and User Stories

## Change Request

Analyse, document, test, and lightly refactor the existing JHotDraw Basic Editing - Copy/Paste feature.

This is a maintenance exercise, not a rebuild. JHotDraw already has Copy/Paste implemented through Swing actions, transfer handlers, clipboard helpers, and format strategies.

## Main User Story

> As a user editing a drawing, I want to copy selected figures and paste them back into the drawing, so that I can duplicate existing work without recreating it manually.

## Acceptance Criteria

1. Given one or more selected figures, when the user invokes Copy, then a transferable representation is placed on the clipboard.
2. Given the clipboard contains supported JHotDraw figure data, when the user invokes Paste, then the figures are added to the drawing.
3. Given figures are pasted, then the newly pasted figures become selected.
4. Given pasted figures were added, when Undo is invoked, then those pasted figures are removed.
5. Given Undo removed pasted figures, when Redo is invoked, then those pasted figures are added back.
6. Given the selection is empty, when Copy is invoked, then no figure transferable is created.
7. Given the clipboard contains an unsupported data flavor, when Paste is invoked, then the drawing should not import it as a figure.
8. Given the drawing registers a text input format, when plain text is pasted, then JHotDraw may create text-holder figures.

## Scope

In scope:

- Copy and paste action flow
- Clipboard abstraction
- Drawing view transfer handler
- Native DOM figure transferable
- Image and text transfer strategies
- Sample draw/SVG format registration
- Small refactoring and tests

Out of scope:

- Rewriting the feature
- Replacing Swing clipboard APIs
- Large UI redesign

## Detailed Combined Note

- [`../../Labs.md`](../../Labs.md)
