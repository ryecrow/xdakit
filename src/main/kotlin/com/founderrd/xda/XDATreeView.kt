/**
 * Title:	XDATreeView
 * Description:	以树的方式实现一个XDAView
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.util.*

class XDATreeView : XDAView() {
    override fun update(path: String, operator: Byte) {
        if (operator == XDADefine.OPERATOR_DELETE) delete(path) else insert(path)
    }

    fun getAllChildrenInfo(path: String): Array<NodeInfo?>? {
        val node = getNode(path) ?: return null
        return node.getAllChildrenInfo(path)
    }

    fun isExisted(path: String): Boolean {
        val stringArray = path.split("\\|/").toTypedArray() ?: return false
        var i = 0
        var father = root
        if (stringArray[i] == root.name) ++i
        while (i < stringArray.size) {
            val currentNode = father.findChild(stringArray[i]) ?: return false
            father = currentNode
            ++i
        }
        return true
    }

    val allFileFullPath: Vector<String>
        get() {
            val allDecendsFullPath = Vector<String>()
            root.getAllDecendFileFullPath(allDecendsFullPath, "")
            return allDecendsFullPath
        }

    private fun insert(path: String) {
        val stringArray = path.split("[\\|/]").toTypedArray() ?: return
        var i = 0
        var father = root
        if (stringArray[i] == root.name) ++i
        while (i < stringArray.size - 1) {
            var currentNode = father.findChild(stringArray[i])
            if (currentNode == null) currentNode = father.insertChild(stringArray[i], true)
            if (currentNode == null) return
            father = currentNode
            ++i
        }
        father.insertChild(stringArray[i], false)
    }

    private fun delete(path: String) {
        val stringArray = path.split("\\|/").toTypedArray() ?: return
        var i = 0
        var father = root
        if (stringArray[i] == root.name) ++i
        while (i < stringArray.size - 1) {
            val currentNode = father.findChild(stringArray[i]) ?: return
            father = currentNode
            ++i
        }
        father.deleteOneChild(stringArray[i])
    }

    private fun getNode(path: String?): Node? {
        if (path == null || path == String("")) return root
        val stringArray = path.split("\\|/").toTypedArray() ?: return root
        var i = 0
        var father = root
        if (stringArray[i] == root.name) ++i
        while (i < stringArray.size) {
            val currentNode = father.findChild(stringArray[i]) ?: return null
            father = currentNode
            ++i
        }
        return father
    }

    private val root: Node

    init {
        root = Node("", true, null)
    }
}

internal class Node(val name: String, val isFolder: Boolean, val father: Node?) {
    private var children: TreeMap<String, Node>? = null
    val fullPath: String
        get() = father!!.name + "\\" + name

    fun insertChild(childName: String, isFolder: Boolean): Node? {
        if (!this.isFolder) return null
        val newNode = Node(childName, isFolder, this)
        children!![newNode.name] = newNode
        return newNode
    }

    fun deleteOneChild(childName: String) {
        children?.remove(childName)
    }

    fun deleteAllChildren() {
        if (children == null) return
        val col: Collection<Node> = children.values
        val iter = col.iterator()
        while (iter.hasNext()) {
            val oneNode = iter.next()
            if (oneNode.isFolder) {
                oneNode.deleteAllChildren()
                deleteOneChild(oneNode.name)
            } else deleteOneChild(oneNode.name)
        }
    }

    fun findChild(name: String): Node? {
        return children?.get(name)
    }

    fun childrenCount(): Int {
        return if (isFolder) children!!.size else 0
    }

    fun getAllDecendFileFullPath(allDecendsFullPath: Vector<String>, pre: String) {
        val col: Collection<Node> = children!!.values
        val iter = col.iterator()
        val currentPath = pre + name + "\\"
        while (iter.hasNext()) {
            val oneNode = iter.next()
            if (oneNode.isFolder) oneNode.getAllDecendFileFullPath(
                allDecendsFullPath,
                currentPath
            ) else allDecendsFullPath.add(currentPath + oneNode.name)
        }
    }

    fun getAllChildrenInfo(prePath: String): Array<NodeInfo?>? {
        if (!isFolder) return null
        if (prePath != "") {
            if (prePath[prePath.length - 1] != '\\'
                || prePath[prePath.length - 1] != '/'
            ) prePath + "\\"
        }
        val nodeInfo = arrayOfNulls<NodeInfo>(childrenCount())
        val col: Collection<Node> = children!!.values
        val iter = col.iterator()
        var i = 0
        while (iter.hasNext()) {
            val oneNode = iter.next()
            nodeInfo[i] = NodeInfo(
                prePath + oneNode.name,
                oneNode.isFolder
            )
            ++i
        }
        return nodeInfo
    }

    protected fun finalize() {
        deleteAllChildren()
    }

    init {
        if (isFolder) {
            children = TreeMap(StringCompare())
        } else children = null
    }
}

internal class StringCompare : Comparator<String> {
    override fun compare(arg0: String, arg1: String): Int {
        return arg0.compareTo(arg1)
    }
}

class NodeInfo(var path: String, var isFolder: Boolean)