package org.obicere.cc.executor.language;

/**
 * @author Obicere
 */
public interface CodeFormatter {

    public int newlineEntered(final String code, final StringBuilder add, final int caret, final int row, final int column);

}