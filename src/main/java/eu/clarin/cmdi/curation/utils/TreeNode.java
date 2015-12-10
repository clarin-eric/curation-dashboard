package eu.clarin.cmdi.curation.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class TreeNode<T>{

	private TreeNode<T> parent;
	private Collection<TreeNode<T>> children = null;
	
	private T data;
	
	
	public TreeNode(T data, boolean isLeaf){
		this.parent = null;		 
		this.data = data;	
	}
	
	
	
	public TreeNode(T data){//it is not leaf by default
		this(data, false);	
	}
	
	
	public TreeNode<T> addChild(T child){
		TreeNode<T> childNode = new TreeNode<T>(child);		
		childNode.parent = this;
		if(children == null)
			children = new LinkedList<TreeNode<T>>();
		this.children.add(childNode);
		return childNode;
	}
	
	@Override
	public String toString() {
		String s = data.toString();
		if(children != null){
			s += "[" + children.stream().map(node -> node.data.toString()).collect(Collectors.joining(",")) + "]";
		}
		return s;
	}
	

	public TreeNode<? extends T> getParent() {
		return parent;
	}


	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}


	public Collection<? extends TreeNode<T>> getChildren() {
		return children;
	}


	public void setChildren(Collection<TreeNode<T>> childern) {
		this.children = childern;
	}


	public T getData() {
		return data;
	}


	public void setData(T data) {
		this.data = data;
	}

}
