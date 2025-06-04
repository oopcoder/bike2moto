package cn.oopcoder.b2m.table;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import java.awt.event.MouseAdapter;

/**
 * @see MouseAdapter
 */

public abstract class TableColumnModelAdapter implements TableColumnModelListener {
    @Override
    public void columnAdded(TableColumnModelEvent e) {
        // Do nothing

    }

    @Override
    public void columnRemoved(TableColumnModelEvent e) {
        // Do nothing

    }

    @Override
    public void columnMoved(TableColumnModelEvent e) {
        // Do nothing

    }

    @Override
    public void columnMarginChanged(ChangeEvent e) {
        // Do nothing

    }

    @Override
    public void columnSelectionChanged(ListSelectionEvent e) {
        // Do nothing

    }
}
