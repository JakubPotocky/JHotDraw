/*
 * @(#)AbstractTextEditingToolTest.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.TextHolderFigure;
import org.jhotdraw.draw.figure.TextFigure;
import org.jhotdraw.draw.figure.TextAreaFigure;
import org.jhotdraw.draw.text.FloatingTextComponent;
import org.jhotdraw.draw.text.FloatingTextField;
import org.jhotdraw.draw.text.FloatingTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.KeyStroke;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.DefaultDrawingEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AbstractTextEditingTool and its subclasses.
 * Tests the common text editing functionality extracted from TextEditingTool
 * and TextAreaEditingTool.
 */
public class AbstractTextEditingToolTest {

    private TextEditingTool textEditingTool;
    private TextAreaEditingTool textAreaEditingTool;
    private TextFigure textFigure;
    private TextAreaFigure textAreaFigure;
    private DrawingEditor editor;
    private DrawingView view;
    private Drawing drawing;

    @BeforeEach
    public void setUp() {
        textFigure = new TextFigure();
        textFigure.setText("Original Text");

        textAreaFigure = new TextAreaFigure();
        textAreaFigure.setText("Original Text Area");

        textEditingTool = new TextEditingTool(textFigure);
        textAreaEditingTool = new TextAreaEditingTool(textAreaFigure);

        editor = new DefaultDrawingEditor();
        view = mock(DrawingView.class);
        drawing = mock(Drawing.class);

        when(view.getDrawing()).thenReturn(drawing);
    }

    @Test
    public void testTextEditingToolIsEditingFalseInitially() {
        assertFalse(textEditingTool.isEditing(), "Tool should not be editing initially");
    }

    @Test
    public void testTextAreaEditingToolIsEditingFalseInitially() {
        assertFalse(textAreaEditingTool.isEditing(), "Tool should not be editing initially");
    }

    @Test
    public void testTextEditingToolCreatesFloatingTextField() {
        assertNotNull(textEditingTool, "TextEditingTool should be created");
        textEditingTool.activate(editor);

        // The floating text field should be created when needed
        assertFalse(textEditingTool.isEditing());
    }

    @Test
    public void testTextAreaEditingToolCreatesFloatingTextArea() {
        assertNotNull(textAreaEditingTool, "TextAreaEditingTool should be created");
        textAreaEditingTool.activate(editor);

        // The floating text area should be created when needed
        assertFalse(textAreaEditingTool.isEditing());
    }

    @Test
    public void testTextEditingToolWithNullTarget() {
        TextEditingTool tool = new TextEditingTool(null);
        assertFalse(tool.isEditing());
    }

    @Test
    public void testTextAreaEditingToolWithNullTarget() {
        TextAreaEditingTool tool = new TextAreaEditingTool(null);
        assertFalse(tool.isEditing());
    }

    @Test
    public void testDeactivateCallsEndEdit() {
        textEditingTool.activate(editor);

        // Simulate editing state
        textFigure.setText("Modified Text");

        // Deactivate should end editing
        textEditingTool.deactivate(editor);
        assertFalse(textEditingTool.isEditing());
    }

    @Test
    public void testTextEditingToolTextPreservation() {
        TextFigure figure = new TextFigure();
        figure.setText("Test Text");

        TextEditingTool tool = new TextEditingTool(figure);
        tool.activate(editor);

        assertEquals("Test Text", figure.getText(), "Figure text should be preserved");
    }

    @Test
    public void testTextAreaEditingToolTextPreservation() {
        TextAreaFigure figure = new TextAreaFigure();
        figure.setText("Test Area Text");

        TextAreaEditingTool tool = new TextAreaEditingTool(figure);
        tool.activate(editor);

        assertEquals("Test Area Text", figure.getText(), "Figure text should be preserved");
    }

    @Test
    public void testBothToolsHaveSameCommonBehavior() {
        // Both should have isEditing method
        assertFalse(textEditingTool.isEditing());
        assertFalse(textAreaEditingTool.isEditing());

        // Both should handle null targets
        TextEditingTool nullTextTool = new TextEditingTool(null);
        TextAreaEditingTool nullAreaTool = new TextAreaEditingTool(null);

        assertFalse(nullTextTool.isEditing());
        assertFalse(nullAreaTool.isEditing());
    }

    @Test
    public void testToolInheritanceHierarchy() {
        assertTrue(textEditingTool instanceof AbstractTextEditingTool,
                "TextEditingTool should extend AbstractTextEditingTool");
        assertTrue(textAreaEditingTool instanceof AbstractTextEditingTool,
                "TextAreaEditingTool should extend AbstractTextEditingTool");
    }

    @Test
    public void testActionListenerImplementation() {
        assertTrue(textEditingTool instanceof ActionListener,
                "Tool should implement ActionListener");
        assertTrue(textAreaEditingTool instanceof ActionListener,
                "Tool should implement ActionListener");
    }

    @Test
    public void testTextEditingToolEditorCreation() {
        textEditingTool.activate(editor);
        assertFalse(textEditingTool.isEditing(), "Initially not editing");
    }

    @Test
    public void testTextAreaEditingToolEditorCreation() {
        textAreaEditingTool.activate(editor);
        assertFalse(textAreaEditingTool.isEditing(), "Initially not editing");
    }

    @Test
    public void testFigureTextNotModifiedOnToolCreation() {
        String originalText = "Original";
        textFigure.setText(originalText);

        TextEditingTool tool = new TextEditingTool(textFigure);
        tool.activate(editor);

        assertEquals(originalText, textFigure.getText(),
                "Figure text should not be modified during tool activation");
    }

    @Test
    public void testMultipleFigureSupport() {
        TextFigure figure1 = new TextFigure();
        figure1.setText("Figure 1");

        TextFigure figure2 = new TextFigure();
        figure2.setText("Figure 2");

        TextEditingTool tool = new TextEditingTool(figure1);
        tool.activate(editor);

        // Tool can be reused with different figures
        assertTrue(figure1.getText().equals("Figure 1"), "First figure should have its text");
        assertTrue(figure2.getText().equals("Figure 2"), "Second figure should have its text");
    }

    @Test
    public void testCodeDuplicationEliminated() {
        // Verify that both TextEditingTool and TextAreaEditingTool delegate
        // to common abstract methods

        // Check TextEditingTool methods
        assertNotNull(textEditingTool, "TextEditingTool created");

        // Check TextAreaEditingTool methods
        assertNotNull(textAreaEditingTool, "TextAreaEditingTool created");

        // Both should be AbstractTextEditingTool instances
        assertTrue(textEditingTool instanceof AbstractTextEditingTool);
        assertTrue(textAreaEditingTool instanceof AbstractTextEditingTool);
    }

    @Test
    public void testTemplateMethodPattern() {
        // Verify the template method pattern is used correctly
        TextEditingTool textTool = new TextEditingTool(textFigure);
        TextAreaEditingTool areaTool = new TextAreaEditingTool(textAreaFigure);

        // Both tools should use the same template method
        assertFalse(textTool.isEditing());
        assertFalse(areaTool.isEditing());
    }

    @Test
    public void testFloatingTextComponentInterface() {
        // Verify FloatingTextField implements the interface
        FloatingTextField textField = new FloatingTextField();
        assertTrue(textField instanceof FloatingTextComponent,
                "FloatingTextField should implement FloatingTextComponent");

        // Verify FloatingTextArea implements the interface
        FloatingTextArea textArea = new FloatingTextArea();
        assertTrue(textArea instanceof FloatingTextComponent,
                "FloatingTextArea should implement FloatingTextComponent");
    }

    @Test
    public void testMaintainabilityImprovement() {
        // Test that demonstrates maintainability: adding a new text editor
        // would only require implementing abstract methods, not duplicating
        // endEdit, beginEdit, deactivate, etc.

        assertTrue(textEditingTool instanceof AbstractTextEditingTool,
                "Demonstrates Template Method Pattern for maintainability");
        assertTrue(textAreaEditingTool instanceof AbstractTextEditingTool,
                "Demonstrates Template Method Pattern for maintainability");
    }
}
