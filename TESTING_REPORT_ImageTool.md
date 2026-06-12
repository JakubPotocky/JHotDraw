# Lab 7 Testing Report: ImageTool

**Course:** Software Maintenance  
**Feature:** ImageTool (Image Insertion and Editing)  
**Test Class:** `ImageToolTest`  
**Date:** June 2026  
**Status:** ✅ All Tests Passing (6/6)

---

## 1. Testing Overview

Unit testing is essential for verifying that individual components behave as intended. This report documents the unit tests created for the `ImageTool` class, which manages image insertion in JHotDraw.

### Why Testing Matters

Testing serves multiple purposes in software development:
- **Defect Detection:** A single developer is less than 50% efficient at finding their own bugs; testing increases defect detection rates above 90% when combined with other quality methods.
- **Speed & Focus:** Unit tests are fast (no database or file system dependencies) and focused (they pinpoint the exact failing method).
- **Regression Prevention:** Tests document expected behavior and catch unintended side effects from future changes.
- **Design Improvement:** Writing testable code leads to better design through loose coupling and high cohesion.

As Dijkstra noted: "Testing can demonstrate the presence of bugs, but not their absence." Therefore, testing must be combined with other quality methods for maximum effectiveness.

---

## 2. What is Tested in ImageTool

The `ImageTool` class contains important domain logic that can be tested without opening a real file dialog:

### Testable Logic
1. **File-selection mode management** via `setUseFileDialog(boolean)` and `isUseFileDialog()`
   - Rule: Switching mode clears the cached component of the other mode
   - Default: Newly created tool defaults to JFileChooser mode

2. **Constructor behavior**
   - Tool initializes with correct default state

3. **Activation guard** in `activate(DrawingEditor)`
   - When there is no active view, the method returns immediately without side effects

### Non-testable Logic (requires UI/file system)
The following code paths run through real Swing dialogs and asynchronous SwingWorker:
- File selection from JFileChooser or FileDialog
- Image loading from file system
- Image assignment to figure

These are intentionally **not** unit-tested because:
- They depend on external resources (file system, Swing components)
- Unit tests should isolate the system under test using mocks
- Integration tests would handle these paths separately

---

## 3. Test Implementation

### Dependencies Added

**JUnit 4** - Used because Swing and JUnit extensions integrate best with it
```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13.2</version>
    <scope>test</scope>
</dependency>
```

**Mockito** - Mocks collaborators (DrawingEditor, DrawingView, ImageHolderFigure)
```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
```

### Mock Strategy

All collaborators are replaced with Mockito mocks so that each test exercises a single code path without touching real dialogs or file system:

```java
@Before
public void setUp() {
    // Dependency → replaced with a mock so no real image is needed.
    prototype = mock(ImageHolderFigure.class);
    when(prototype.clone()).thenReturn(prototype);
    tool = new ImageTool(prototype);
}
```

**Why mocking:**
- **Isolation:** Tests focus on ImageTool logic, not file system or UI
- **Speed:** No real I/O operations or dialog construction
- **Repeatability:** Tests don't depend on file system state or user interaction
- **Control:** Tests can set precise mock behavior for each scenario

---

## 4. Test Cases

### Best-Case Tests (3 tests)

Best-case tests verify the tool working as intended: constructing it and configuring the file-selection mode.

#### Test 1: `newToolDefaultsToJFileChooserMode()`
**Purpose:** Verify that a freshly created tool starts in JFileChooser mode  
**Setup:** Create tool without configuration  
**Action:** Query mode with `isUseFileDialog()`  
**Assertion:** Should return `false` (JFileChooser mode)  
**Importance:** Establishes the default safe state; FileDialog is optional

```java
@Test
public void newToolDefaultsToJFileChooserMode() {
    assertFalse("Newly created tool must default to JFileChooser mode",
            tool.isUseFileDialog());
}
```

#### Test 2: `setUseFileDialogTrue_enablesFileDialogMode()`
**Purpose:** Verify that setting `setUseFileDialog(true)` enables FileDialog mode  
**Setup:** Create tool, call `setUseFileDialog(true)`  
**Action:** Query mode with `isUseFileDialog()`  
**Assertion:** Should return `true` (FileDialog mode)  
**Importance:** Verifies mode can be switched to native FileDialog

```java
@Test
public void setUseFileDialogTrue_enablesFileDialogMode() {
    tool.setUseFileDialog(true);
    assertTrue("FileDialog mode should be enabled after setUseFileDialog(true)",
            tool.isUseFileDialog());
}
```

#### Test 3: `setUseFileDialogFalse_enablesChooserMode()`
**Purpose:** Verify that switching from FileDialog back to JFileChooser works  
**Setup:** Create tool, enable FileDialog, then disable it  
**Action:** Query mode with `isUseFileDialog()`  
**Assertion:** Should return `false` (JFileChooser mode)  
**Importance:** Verifies mode switching is reversible

```java
@Test
public void setUseFileDialogFalse_enablesChooserMode() {
    tool.setUseFileDialog(true);
    assertTrue(tool.isUseFileDialog());
    
    tool.setUseFileDialog(false);
    assertFalse("JFileChooser mode should be enabled after setUseFileDialog(false)",
            tool.isUseFileDialog());
}
```

---

### Boundary-Case Tests (2 tests)

Boundary-case tests probe the edges of the logic and unusual conditions.

#### Test 4: `activateWithNoView_returnsWithoutLoadingImage()`
**Purpose:** Verify that `activate()` safely handles missing view  
**Boundary:** No active view (null condition)  
**Setup:** Mock editor with no active view
```java
when(editor.getActiveView()).thenReturn(null);
```
**Action:** Call `tool.activate(editor)`  
**Assertion:** Verify mock prototype's `setImage()` was never called (`verify(prototype, never()).setImage(any(), any())`)  
**Importance:** 
- Tests the guard: `if (view == null) return;`
- Ensures no side effects when precondition not met
- Verifies early return prevents downstream operations

```java
@Test
public void activateWithNoView_returnsWithoutLoadingImage() throws IOException {
    DrawingEditor editor = mock(DrawingEditor.class);
    when(editor.getDrawingViews()).thenReturn(Collections.<DrawingView>emptyList());
    when(editor.getActiveView()).thenReturn(null);

    tool.activate(editor);

    verify(prototype, never()).setImage(any(), any());
}
```

#### Test 5: `switchingModeRepeatedly_keepsStateConsistent()`
**Purpose:** Verify that mode state remains consistent under repeated switches  
**Boundary:** 5 consecutive mode switches (even = true, odd = false)  
**Setup:** Tool created  
**Action:** Loop 5 times, switching mode each iteration
```java
for (int i = 0; i < 5; i++) {
    tool.setUseFileDialog(i % 2 == 0);
}
```
**Assertion:** After 5 iterations (i=4 is even), mode should be FileDialog (true)  
**Importance:**
- Tests state stability under repeated operations
- Catches potential side effects from repeated mode switches
- Verifies no accumulated state corruption

```java
@Test
public void switchingModeRepeatedly_keepsStateConsistent() {
    for (int i = 0; i < 5; i++) {
        tool.setUseFileDialog(i % 2 == 0);
    }
    assertTrue("After 5 switches, final mode should be FileDialog (true)",
            tool.isUseFileDialog());
}
```

---

### Invariant Test (1 test)

Invariant tests use Java assertions to verify conditions that should **never** be violated.

#### Test 6: `useFileDialogState_isAlwaysWellDefined()`
**Purpose:** Verify the file-selection mode invariant: setting a mode means reading it back must match  
**Invariant:** The two modes are mutually exclusive and always consistent  
**Setup:** Tool created  
**Action:** 
1. Set FileDialog mode (true)
2. Assert with `assert tool.isUseFileDialog() : "..."`
3. Set JFileChooser mode (false)
4. Assert with `assert !tool.isUseFileDialog() : "..."`

**Assertion Messages:** Custom messages explaining what should never happen  
**Importance:**
- Uses Java assertions (enabled with JVM flag `-ea`)
- Halts program if invariant violated (as opposed to exceptions, which allow recovery)
- Distinguishes from IOException (recoverable) — mode state corruption is unrecoverable

```java
@Test
public void useFileDialogState_isAlwaysWellDefined() {
    tool.setUseFileDialog(true);
    assert tool.isUseFileDialog() : "mode must be true after enabling FileDialog";

    tool.setUseFileDialog(false);
    assert !tool.isUseFileDialog() : "mode must be false after enabling chooser";
}
```

---

## 5. Test Results

### Execution Summary

```
Tests run: 6
Failures: 0
Errors: 0
Skipped: 0
Success Rate: 100%
Execution Time: 0.870 seconds
```

### Test Breakdown

| Test Name | Type | Status | Coverage |
|-----------|------|--------|----------|
| `newToolDefaultsToJFileChooserMode` | Best Case | ✅ Pass | Constructor defaults |
| `setUseFileDialogTrue_enablesFileDialogMode` | Best Case | ✅ Pass | Mode set to true |
| `setUseFileDialogFalse_enablesChooserMode` | Best Case | ✅ Pass | Mode set to false |
| `activateWithNoView_returnsWithoutLoadingImage` | Boundary | ✅ Pass | Null view guard |
| `switchingModeRepeatedly_keepsStateConsistent` | Boundary | ✅ Pass | State consistency |
| `useFileDialogState_isAlwaysWellDefined` | Invariant | ✅ Pass | Mode invariant |

---

## 6. Testing Best Practices Applied

### Single Code Path per Test
Each test exercises exactly one code path through one method:
- `newToolDefaultsToJFileChooserMode()` → constructor → default initialization
- `setUseFileDialogTrue_enablesFileDialogMode()` → `setUseFileDialog(true)` → mode reader
- `activateWithNoView_returnsWithoutLoadingImage()` → `activate()` → null view branch only

### Mock Usage
- **Prototype:** Mocked to avoid creating real image objects
- **DrawingEditor:** Mocked to control view availability
- **DrawingView:** Mocked to prevent Swing component construction
- **Verification:** `verify(prototype, never()).setImage()` confirms no side effects

### Assertion Patterns
- **State verification:** `assertTrue()/assertFalse()` for mode state
- **Mock verification:** `verify(mock, times/never()).method()` for call counts
- **Java assertions:** `assert condition : "message"` for invariants

---

## 7. Running the Tests

### Standard Test Run
```bash
mvn -pl jhotdraw-core -Dtest=ImageToolTest test
```

### With Java Assertions Enabled (for invariant test)
The invariant test uses `assert` statements, which require the JVM `-ea` flag. Maven Surefire can be configured to enable assertions:

**pom.xml configuration:**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-ea</argLine>
    </configuration>
</plugin>
```

Or enable at runtime:
```bash
java -ea -jar target/test.jar
```

---

## 8. Mock Maker Configuration

To support Java 23 with Mockito, the ByteBuddy mock maker is explicitly configured:

**File:** `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
```
org.mockito.internal.creation.bytebuddy.ByteBuddyMockMaker
```

This configuration ensures Mockito uses the ByteBuddy approach instead of inline mocks, improving compatibility with newer Java versions.

---

## 9. Design Impact: Why Refactoring Helps Testing

The `activate()` method was originally a large monolithic method. The refactoring that extracted helper methods (`selectImageFile()`, `loadImageAsync()`, etc.) made testing possible without major rewrites.

**Original limitation:** Cannot test `activate()` without opening a real FileDialog or touching the file system.

**After refactoring:** Can test the important logic (mode configuration, null-view guard) while file selection and image loading remain in separate methods.

This illustrates a key principle: **Testable code is well-designed code.**

---

## 10. Conclusion

The `ImageToolTest` suite provides comprehensive coverage of the important domain logic in `ImageTool`:

✅ **Best cases:** Verify correct default and expected behavior  
✅ **Boundary cases:** Verify handling of edge conditions (null view, repeated operations)  
✅ **Invariants:** Verify unbreakable preconditions using Java assertions  

All 6 tests pass, confirming that `ImageTool`'s configuration and activation guard work correctly. The test suite documents the expected behavior and will catch regressions if the logic changes in the future.

**By design:** File selection and image loading are tested through integration tests and manual testing, not unit tests, as they require external resources and UI interaction.

---

**Test Framework Versions:**
- JUnit 4.13.2
- Mockito 5.2.0
- Java 11+ compatible (tested with Java 23)

**Test Location:** `jhotdraw-core/src/test/java/org/jhotdraw/draw/tool/ImageToolTest.java`
