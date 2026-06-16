/*
 * @(#)FloatingTextComponent.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.text;

import org.jhotdraw.draw.figure.TextHolderFigure;
import java.awt.event.ActionListener;
import org.jhotdraw.draw.DrawingView;

/**
 * Common interface for floating text components (FloatingTextField and FloatingTextArea).
 * Allows AbstractTextEditingTool to work with both single-line and multi-line editors
 * without knowing the specific implementation.
 */
public interface FloatingTextComponent {

    /**
     * Creates the overlay for editing a text figure.
     *
     * @param view the DrawingView where the overlay will be displayed
     * @param figure the TextHolderFigure being edited
     */
    void createOverlay(DrawingView view, TextHolderFigure figure);

    /**
     * Requests focus for the text editing component.
     */
    void requestFocus();

    /**
     * Returns the text content from the editor.
     *
     * @return the edited text
     */
    String getText();

    /**
     * Removes the overlay from the view.
     */
    void endOverlay();

    /**
     * Adds an action listener to the component.
     *
     * @param listener the action listener to add
     */
    void addActionListener(ActionListener listener);

    /**
     * Removes an action listener from the component.
     *
     * @param listener the action listener to remove
     */
    void removeActionListener(ActionListener listener);
}
