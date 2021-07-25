package commnet.model.datastructures;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import commnet.model.beans.Event;

public class EventBinaryTree {

	Node root;

	public void add(List<Event> values){
		for (Event event : values) {
			add(event);
		}
	}
	
	public void add(Event value) {
		root = add(root, value);
	}
	
	private Node add(Node parent, Event child) {

		if (parent == null) {
			return new Node(child);
		} 
		
		if (parent.value.getCreatedAt().getTime() > child.getCreatedAt().getTime()) {
			parent.left = add(parent.left, child);
		} else {
			parent.right = add(parent.right, child);
		}

		return parent;
	}

	public boolean isEmpty() {
		return root == null;
	}

	public int getSize() {
		return getSizeRecursive(root);
	}

	private int getSizeRecursive(Node current) {
		return current == null ? 0 : getSizeRecursive(current.left) + 1
				+ getSizeRecursive(current.right);
	}

	public boolean containsNode(Event value) {
		return containsNodeRecursive(root, value);
	}

	private boolean containsNodeRecursive(Node current, Event value) {
		if (current == null) {
			return false;
		}

		if (value.getIdDB() == current.value.getIdDB()) {
			return true;
		}

		return current.value.getCreatedAt().getTime() > value.getCreatedAt()
				.getTime() ? containsNodeRecursive(current.left, value)
				: containsNodeRecursive(current.right, value);
	}

	public void delete(Event value) {
		deleteRecursive(root, value);
	}

	private Node deleteRecursive(Node current, Event value) {
		if (current == null) {
			return null;
		}

		if (value.getIdDB() == current.value.getIdDB()) {
			// Case 1: no children
			if (current.left == null && current.right == null) {
				return null;
			}

			// Case 2: only 1 child
			if (current.right == null) {
				return current.left;
			}

			if (current.left == null) {
				return current.right;
			}

			// Case 3: 2 children
			Event smallestValue = findSmallestValue(current.right);
			current.value = smallestValue;
			current.right = deleteRecursive(current.right, smallestValue);
			return current;
		}
		if (value.getCreatedAt().getTime() < current.value.getCreatedAt()
				.getTime()) {
			current.left = deleteRecursive(current.left, value);
			return current;
		}

		current.right = deleteRecursive(current.right, value);
		return current;
	}

	private Event findSmallestValue(Node root) {
		return root.left == null ? root.value : findSmallestValue(root.left);
	}

	// References work faster than returns
	public Optional<List<Event>> values(Date msBase, Date msMerge) {
		ArrayList<Event> list = new ArrayList<Event>();
		values(root, list, msBase, msMerge);
		return Optional.of(list);
	}

	private void values(Node node, ArrayList<Event> list, Date msBase, Date msMerge) {
		if (node == null)
			return;

		long cmplo = msBase.getTime() - node.value.getCreatedAt().getTime();
		long cmphi = msMerge.getTime() - node.value.getCreatedAt().getTime();

		if (cmplo < 0) {
			values(node.left, list, msBase, msMerge);
		}
		if (cmplo <= 0 && cmphi >= 0) {
			list.add(node.value);
		}
		if (cmphi > 0) {
			values(node.right, list, msBase, msMerge);
		}
	}
	

	public String traverseInOrder(Node node) {
		StringBuilder sb = new StringBuilder();
		if (node != null) {
			sb.append(traverseInOrder(node.left));
			sb.append(" " + node.value.getCreatedAt() + " ");
			sb.append(traverseInOrder(node.right));
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return traverseInOrder(root);
	}

	class Node {
		Event value;
		Node left;
		Node right;

		Node(Event value) {
			this.value = value;
			right = null;
			left = null;
		}
	}

}
