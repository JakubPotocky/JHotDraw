/*
 * @(#)AbstractTextEditingTool.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.TextHolderFigure;
import java.awt.*;
import java.awt.event.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.text.*;
import org.jhotdraw.util.ResourceBundleUtil;

/**
 * Abstract base class for tools to edit existing text figures.
 *
 * This class implements the Template Method pattern to extract common behavior
 * from TextEditingTool and TextAreaEditingTool, eliminating code duplication
 * while allowing subclasses to customize editor-specific functionality.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public abstract class AbstractTextEditingTool extends AbstractTool implements ActionListener {

    private static final long serialVersionUID = 1L;
    protected TextHolderFigure typingTarget;

    /**
     * Creates a new instance.
     */
    public AbstractTextEditingTool(TextHolderFigure typingTarget) {
        this.typingTarget = typingTarget;
    }

    @Override
    public void deactivate(DrawingEditor editor) {
        endEdit();
        super.deactivate(editor);
    }

    /**
     * If the pressed figure is a TextHolderFigure it can be edited.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (typingTarget != null) {
            beginEdit(typingTarget);
            updateCursor(getView(), e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
    }

    /**
     * Template method for initializing text editing.
     * Handles switching between editors and prepares for editing.
     */
    protected void beginEdit(TextHolderFigure textHolder) {
        FloatingTextComponent editor = getFloatingEditor();
        if (editor == null) {
            editor = createEditor();
            editor.addActionListener(this);
        }
        if (textHolder != typingTarget && typingTarget != null) {
            endEdit();
        }
        editor.createOverlay(getView(), textHolder);
        initializeEditor(textHolder);
        editor.requestFocus();
        typingTarget = textHolder;
    }

    /**
     * Common logic for ending text editing.
     * Creates undo/redo record and cleans up resources.
     */
    protected void endEdit() {
        if (typingTarget != null) {
            typingTarget.willChange();
            final TextHolderFigure editedFigure = typingTarget;
            final String oldText = typingTarget.getText();
            final String newText = getFloatingEditor().getText();

            if (newText.length() > 0) {
                typingTarget.willChange();
                typingTarget.setText(newText);
                typingTarget.changed();
            }

            UndoableEdit edit = new AbstractUndoableEdit() {
                private static final long serialVersionUID = 1L;

                @Override
                public String getPresentationName() {
                    ResourceBundleUtil labels = ResourceBundleUtil.getBundle("org.jhotdraw.draw.Labels");
                    return labels.getString("attribute.text.text");
                }

                @Override
                public void undo() {
                    super.undo();
                    editedFigure.willChange();
                    editedFigure.setText(oldText);
                    editedFigure.changed();
                }

                @Override
                public void redo() {
                    super.redo();
                    editedFigure.willChange();
                    editedFigure.setText(newText);
                    editedFigure.changed();
                }
            };

            getDrawing().fireUndoableEditHappened(edit);
            typingTarget.changed();
            typingTarget = null;
            getFloatingEditor().endOverlay();
        }
    }

    @Override
    public void keyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            fireToolDone();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        endEdit();
        fireToolDone();
    }

    public boolean isEditing() {
        return typingTarget != null;
    }

    @Override
    public void updateCursor(DrawingView view, Point p) {
        if (view.isEnabled()) {
            view.setCursor(Cursor.getPredefinedCursor(isEditing() ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
        } else {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Creates and returns a new floating editor component.
     * Subclasses must implement to provide the appropriate editor type.
     *
     * @return A new floating editor (FloatingTextField or FloatingTextArea)
     */
    protected abstract FloatingTextComponent createEditor();

    /**
     * Initializes the editor with editor-specific settings.
     * Called after createOverlay() to perform any additional setup.
     *
     * @param textHolder The figure being edited
     */
    protected abstract void initializeEditor(TextHolderFigure textHolder);

    /**
     * Returns the current floating editor component.
     *
     * @return The active floating editor
     */
    protected abstract FloatingTextComponent getFloatingEditor();
}
