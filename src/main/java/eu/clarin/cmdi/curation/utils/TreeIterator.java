package eu.clarin.cmdi.curation.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public abstract class TreeIterator<T> implements Iterator<TreeNode<T>>{
	
	protected Stack<TreeNode<T>> stack;
	
	public TreeIterator(TreeNode<T> treeRoot){
		stack = new Stack<TreeNode<T>>();
		initStack(treeRoot);
	}
	
	
	public boolean hasNext() {
		return !stack.isEmpty();
	}

	public TreeNode<T> next() {
		if(!hasNext())
			throw new NoSuchElementException();
		return stack.pop();
	}
	
	protected abstract void initStack(TreeNode<T> treeRoot);

}
