package eu.clarin.cmdi.curation.utils;

public class PostOrder<T> extends TreeIterator<T>{

	public PostOrder(TreeNode<T> treeNode) {
		super(treeNode);
	}


	@Override
	protected void initStack(TreeNode<T> treeRoot) {
//		TreeNode<T> next = treeRoot;
//		if(next == null)
//			return;
//		stack.push(next);
//		if(next.getChildern() != null)
//			for(TreeNode<T> child: next.getChildern())
//				initStack(child);
	}
	

}
