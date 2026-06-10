# ChangeReqLab - Copy/Paste Change Request

## Lab Goal

The ChangeReqLab asks each student to select an existing JHotDraw feature and write a user story for the mandatory individual portfolio assignment.

Bob's screenshots show the selected parent feature and sub-issues:

- Basic editing #12.
- Copy #13.
- Paste #14.

## Selected Feature

System under maintenance: JHotDraw 7.6.

Feature area: Basic editing.

Selected sub-feature pair: Copy and Paste.

## Source Input From Screenshots

### Parent Issue - Basic Editing #12

Basic editing lets users make simple changes to a selected object, such as moving, resizing, or updating its appearance.

Sub-issues:

- copy #13.
- paste #14.

### Sub-Issue - Copy #13

As a user, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.

### Sub-Issue - Paste #14

As a user, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

## User Stories

### US-1 - Copy Selected Object

As a user editing a drawing, I want to copy a selected object so that I can quickly duplicate existing work without recreating it manually.

Trigger:

- Edit -> Copy.
- Keyboard shortcut Ctrl+C / Command+C.
- Copy action in a toolbar or popup button where available.

Acceptance criteria:

1. The command is available when at least one figure is selected and the active drawing view can export figures.
2. When the user triggers Copy, the selected figure or figures are exported to the clipboard.
3. The original selected figures remain in the drawing.
4. The copied content is represented in one or more supported transfer formats.
5. The copied content can later be pasted into a drawing view that supports a matching input format.

### US-2 - Paste Copied Object

As a user editing a drawing, I want to paste a copied object so that I can quickly place a duplicate on the canvas.

Trigger:

- Edit -> Paste.
- Keyboard shortcut Ctrl+V / Command+V.
- Paste action in a toolbar or popup button where available.

Acceptance criteria:

1. The command reads the current clipboard content.
2. If the clipboard content has a supported data flavor, the drawing view imports it.
3. Imported figures are added to the active drawing.
4. Imported figures become the current selection after paste.
5. The paste operation creates an undoable edit named with the paste action label.
6. If the clipboard content is unsupported, the drawing is left unchanged.

## Change Type

This is primarily an analysis and documentation task for an existing feature, not a new feature implementation. If the portfolio later changes the code to improve copy/paste maintainability, that change will be preventive/refactoring work.

The user-facing copy/paste behavior itself is a perfective/basic editing feature because it helps users duplicate existing drawing work faster.

## Why This Feature Is Suitable For The Portfolio

Copy/paste is suitable because it crosses several course concepts:

- User stories and change requests.
- Concept location through actions, focused components, transfer handlers, clipboard classes, drawing views, and input/output formats.
- Impact analysis across several modules.
- Framework reuse through Swing `Action`, `TransferHandler`, and JHotDraw drawing abstractions.
- Design patterns such as Command/Action, Proxy, Strategy-like input/output formats, Prototype cloning, and Observer/Undo integration.
- Code smells and refactoring opportunities in duplicated action logic and transfer-handler complexity.
- Testing challenges because clipboard and GUI behavior cross component boundaries.

## Portfolio Summary

Bob's selected maintenance story is:

Copy and paste selected figures in the JHotDraw drawing canvas so users can duplicate existing work without recreating it manually.

The next exercise is concept location: identify the domain classes that implement copy/paste.

