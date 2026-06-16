/*
 * @(#)TextAreaEditingTool.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.TextHolderFigure;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.jhotdraw.draw.*;
import org.jhotdraw.draw.text.*;
import org.jhotdraw.geom.Insets2D;

/**
 * A tool to edit existing figures that implement the TextHolderFigure
 * interface, such as TextAreaFigure.
 * <p>
 * To edit an existing text figure using the TextAreaEditingTool, the user does the
 * following mouse gesture on a DrawingView:
 * </p>
 * <ol>
 * <li>Press the mouse button over a Figure on the DrawingView.</li>
 * </ol>
 * <p>
 * The TextAreaEditingTool then uses Figure.findFigureInside to find a Figure that
 * implements the TextHolderFigure interface and that is editable. Then it overlays
 * a text area over the drawing where the user can enter the text for the Figure.
 * </p>
 * <hr>
 * <b>Design Patterns</b>
 *
 * <p>
 * <em>Framework</em><br>
 * The text creation and editing tools and the {@code TextHolderFigure}
 * interface define together the contracts of a smaller framework inside of the
 * JHotDraw framework for structured drawing editors.<br>
 * Contract: {@link TextHolderFigure}, {@link TextCreationTool},
 * {@link TextAreaCreationTool}, {@link TextEditingTool},
 * {@link TextAreaEditingTool}, {@link FloatingTextField},
 * {@link FloatingTextArea}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 *
 * @see TextHolderFigure
 * @see FloatingTextArea
 */
public class TextAreaEditingTool extends AbstractTextEditingTool {

    private static final long serialVersionUID = 1L;
    private FloatingTextComponent editor;

    /**
     * Creates a new instance.
     */
    public TextAreaEditingTool(TextHolderFigure typingTarget) {
        super(typingTarget);
    }

    @Override
    public void draw(Graphics2D g) {
    }

    @Override
    protected FloatingTextComponent createEditor() {
        editor = new FloatingTextArea();
        return editor;
    }

    @Override
    protected void initializeEditor(TextHolderFigure textHolder) {
        FloatingTextArea textArea = (FloatingTextArea) editor;
        textArea.setBounds(getFieldBounds(textHolder), textHolder.getText());
    }

    @Override
    protected FloatingTextComponent getFloatingEditor() {
        return editor;
    }

    private Rectangle2D.Double getFieldBounds(TextHolderFigure figure) {
        Rectangle2D.Double r = figure.getDrawingArea();
        Insets2D.Double insets = figure.getInsets();
        insets.subtractTo(r);
        r.x -= 1;
        r.y -= 2;
        r.width += 18;
        r.height += 4;
        return r;
    }
}
