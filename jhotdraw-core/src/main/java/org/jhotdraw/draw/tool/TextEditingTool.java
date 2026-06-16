/*
 * @(#)TextEditingTool.java
 *
 * Copyright (c) 2009-2010 The authors and contributors of JHotDraw.
 * You may not use, copy or modify this file, except in compliance with the
 * accompanying license terms.
 */
package org.jhotdraw.draw.tool;

import org.jhotdraw.draw.figure.TextHolderFigure;
import org.jhotdraw.draw.text.*;

/**
 * A tool to edit figures which implement the {@code TextHolderFigure} interface,
 * such as {@code TextFigure}.
 *
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
 *
 * <p>
 * <em>Prototype</em><br>
 * The text creation tools create new figures by cloning a prototype
 * {@code TextHolderFigure} object.<br>
 * Prototype: {@link TextHolderFigure}; Client: {@link TextCreationTool},
 * {@link TextAreaCreationTool}.
 * <hr>
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class TextEditingTool extends AbstractTextEditingTool {

    private static final long serialVersionUID = 1L;
    private FloatingTextComponent editor;

    /**
     * Creates a new instance.
     */
    public TextEditingTool(TextHolderFigure typingTarget) {
        super(typingTarget);
    }

    @Override
    protected FloatingTextComponent createEditor() {
        editor = new FloatingTextField();
        return editor;
    }

    @Override
    protected void initializeEditor(TextHolderFigure textHolder) {
    }

    @Override
    protected FloatingTextComponent getFloatingEditor() {
        return editor;
    }
}
