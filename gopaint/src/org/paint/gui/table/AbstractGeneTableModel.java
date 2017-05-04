/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.gui.table;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.paint.gui.PaintTable;


/**
 * An abstract implementation of the TreeTableModel interface, handling
 * the list of listeners.
 *
 */
public abstract class AbstractGeneTableModel extends AbstractTableModel implements PaintTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Object            root;
	protected EventListenerList listenerList = new EventListenerList();

	public AbstractGeneTableModel(Object root){
		this.root = root;
	}

	public AbstractGeneTableModel() {
	}

	//
	// Default implmentations for methods in the TreeModel interface.
	//
	public Object getRoot(){
		return root;
	}

	public void valueForPathChanged(TreePath path, Object newValue) {}

	public void addTreeModelListener(TreeModelListener l){
		listenerList.add(TreeModelListener.class, l);
	}

	public void removeTreeModelListener(TreeModelListener l){
		listenerList.remove(TreeModelListener.class, l);
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children){

		// Guaranteed to return a non-null array
		Object[]        listeners = listenerList.getListenerList();
		TreeModelEvent  e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2){
			if (listeners[i] == TreeModelListener.class){

				// Lazily create the event:
				if (e == null){
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children){

		// Guaranteed to return a non-null array
		Object[]        listeners = listenerList.getListenerList();
		TreeModelEvent  e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2){
			if (listeners[i] == TreeModelListener.class){

				// Lazily create the event:
				if (e == null){
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children){

		// Guaranteed to return a non-null array
		Object[]        listeners = listenerList.getListenerList();
		TreeModelEvent  e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2){
			if (listeners[i] == TreeModelListener.class){

				// Lazily create the event:
				if (e == null){
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	/*
	 * Notify all listeners that have registered interest for
	 * notification on this event type.  The event instance
	 * is lazily created using the parameters passed into
	 * the fire method.
	 * @see EventListenerList
	 */
	protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children){

		// Guaranteed to return a non-null array
		Object[]        listeners = listenerList.getListenerList();
		TreeModelEvent  e = null;

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2){
			if (listeners[i] == TreeModelListener.class){

				// Lazily create the event:
				if (e == null){
					e = new TreeModelEvent(source, path, childIndices, children);
				}

				((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
			}
		}
	}

	public void setValueAt(Object aValue, Object node, int column) {}

}

