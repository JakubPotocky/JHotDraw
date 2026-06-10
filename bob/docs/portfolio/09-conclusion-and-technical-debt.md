# 09 Conclusion and Technical Debt

## Conclusion

JHotDraw Copy/Paste is implemented as a reusable transfer pipeline. The actions are small, the drawing view owns selection, the transfer handler coordinates import/export, and input/output formats provide extensibility.

The maintenance work preserved the existing feature and added confidence through documentation, CI, focused JUnit tests, and executable BDD-style scenarios.

## Technical Debt Observed

- Some transfer handler logic is still large and mixes paste, drag/drop, file import, and undo behavior.
- Exception handling often prints stack traces directly.
- Some legacy APIs and deprecated usage appear during compilation.
- Full GUI-level copy/paste remains hard to test without Swing integration helpers.
- The SVG sample launch emitted resource warnings for missing icons.

## Future Improvements

- Add a clipboard-level test using `ClipboardUtil.setClipboard(...)`.
- Add a higher-level Swing/UI test that drives menu or keyboard Copy/Paste actions.
- Improve logging instead of printing stack traces.
- Separate drag/drop file import from clipboard paste coordination if the class grows further.
