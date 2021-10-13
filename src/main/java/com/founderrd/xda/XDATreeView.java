/**
 * Title:	XDATreeView
 * Description:	以树的方式实现一个XDAView
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.util.*;

public class XDATreeView extends XDAView {
    private final Node root;

    public XDATreeView() {
        root = new Node("", true, null);
    }

    public void update(String path, byte operator) {
        if (operator == XDADefine.OPERATOR_DELETE)
            delete(path);
        else
            insert(path);
    }

    public NodeInfo[] getAllChildrenInfo(String path) {
        Node node = getNode(path);
        if (node == null)
            return null;

        return node.getAllChildrenInfo(path);
    }

    public boolean isExisted(String path) {
        String[] stringArray = path.split("\\|/");
        if (stringArray == null)
            return false;

        int i = 0;
        Node father = root;
        if (stringArray[i].equals(root.getName()))
            ++i;

        while (i < stringArray.length) {
            Node currentNode = father.findChild(stringArray[i]);
            if (currentNode == null)
                return false;
            father = currentNode;
            ++i;
        }

        return true;
    }

    public Vector<String> getAllFileFullPath() {
        Vector<String> allDecendsFullPath = new Vector<>();
        root.getAllDecendFileFullPath(allDecendsFullPath, "");
        return allDecendsFullPath;
    }

    private void insert(String path) {
        String[] stringArray = path.split("[\\|/]");
        if (stringArray == null)
            return;
        int i = 0;
        Node father = root;
        if (stringArray[i].equals(root.getName()))
            ++i;

        while (i < stringArray.length - 1) {
            Node currentNode = father.findChild(stringArray[i]);
            if (currentNode == null)
                currentNode = father.insertChild(stringArray[i], true);
            if (currentNode == null)
                return;
            father = currentNode;
            ++i;
        }

        father.insertChild(stringArray[i], false);
    }

    private void delete(String path) {
        String[] stringArray = path.split("\\|/");
        if (stringArray == null)
            return;
        int i = 0;
        Node father = root;
        if (stringArray[i].equals(root.getName()))
            ++i;

        while (i < stringArray.length - 1) {
            Node currentNode = father.findChild(stringArray[i]);
            if (currentNode == null)
                return;
            father = currentNode;
            ++i;
        }

        father.deleteOneChild(stringArray[i]);
    }

    private Node getNode(String path) {
        if (path == null || path.equals(""))
            return root;

        String[] stringArray = path.split("\\|/");
        if (stringArray == null)
            return root;

        int i = 0;
        Node father = root;
        if (stringArray[i].equals(root.getName()))
            ++i;

        while (i < stringArray.length) {
            Node currentNode = father.findChild(stringArray[i]);
            if (currentNode == null)
                return null;
            father = currentNode;
            ++i;
        }

        return father;
    }
}

class Node {
    private final String name;
    private final Node father;
    private final boolean isFolder;
    private final TreeMap<String, Node> children;

    Node(String name, boolean isFolder, Node father) {
        this.name = name;
        this.father = father;
        this.isFolder = isFolder;

        if (isFolder) {
            this.children = new TreeMap<>(new StringCompare());
        } else
            this.children = null;
    }

    String getName() {
        return name;
    }

    Node getFather() {
        return father;
    }

    boolean isFolder() {
        return isFolder;
    }

    String getFullPath() {
        return father.name.concat("\\").concat(name);
    }

    Node insertChild(String childName, boolean isFolder) {
        if (!this.isFolder)
            return null;

        Node newNode = new Node(childName, isFolder, this);
        children.put(newNode.name, newNode);
        return newNode;
    }

    void deleteOneChild(String childName) {
        if (children != null)
            children.remove(childName);
    }

    void deleteAllChildren() {
        if (children == null)
            return;

        Collection<Node> col = children.values();
        for (Node oneNode : col) {
            if (oneNode.isFolder) {
                oneNode.deleteAllChildren();
                deleteOneChild(oneNode.getName());
            } else
                deleteOneChild(oneNode.getName());
        }
    }

    Node findChild(String name) {
        if (children == null)
            return null;

        return children.get(name);
    }

    int childrenCount() {
        if (isFolder)
            return children.size();

        return 0;
    }

    void getAllDecendFileFullPath(Vector<String> allDecendsFullPath, String pre) {
        Collection<Node> col = children.values();
        Iterator<Node> iter = col.iterator();

        String currentPath = pre.concat(getName()).concat("\\");
        while (iter.hasNext()) {
            Node oneNode = iter.next();
            if (oneNode.isFolder)
                oneNode.getAllDecendFileFullPath(allDecendsFullPath,
                        currentPath);
            else
                allDecendsFullPath.add(currentPath.concat(oneNode.getName()));
        }
    }

    NodeInfo[] getAllChildrenInfo(String prePath) {
        if (!isFolder)
            return null;

        if (!prePath.equals("")) {
            if (prePath.charAt(prePath.length() - 1) != '\\'
                    || prePath.charAt(prePath.length() - 1) != '/')
                prePath.concat("\\");
        }

        NodeInfo[] nodeInfo = new NodeInfo[childrenCount()];
        Collection<Node> col = children.values();
        Iterator<Node> iter = col.iterator();
        int i = 0;
        while (iter.hasNext()) {
            Node oneNode = iter.next();
            nodeInfo[i] = new NodeInfo(prePath.concat(oneNode.getName()),
                    oneNode.isFolder());
            ++i;
        }

        return nodeInfo;
    }

    protected void finalize() {
        deleteAllChildren();
    }
}

class StringCompare implements Comparator<String> {
    public int compare(String arg0, String arg1) {
        return arg0.compareTo(arg1);
    }
}

class NodeInfo {
    public final String path;
    public final boolean isFolder;

    NodeInfo(String path, boolean isFolder) {
        this.path = path;
        this.isFolder = isFolder;
    }
}