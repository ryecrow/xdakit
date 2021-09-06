/**
 * Title:	ParseXDA
 * Description:	命令行方式提供测试接口
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDAException
import java.io.*
import java.security.NoSuchAlgorithmException
import java.util.*

object ParseXDA {
    @JvmStatic
    fun main(args: Array<String>) {
        val c = Command()
        c.launch()
    }
}

internal class Command {
    private val nullCmd = Cmd()
    private val ecsC = byteArrayOf(0x02.toByte(), 0xff.toByte())
    private val ecsNC = byteArrayOf(0xff.toByte())
    private val isCompress: Boolean
    private var currentPackge = "None!"
    private val xda: XDAInterface
    private val cmdMap: HashMap<String, Cmd>
    fun launch() {
        println("Start...")
        println("Input command, please.")
        printPrompt()
        while (true) {
            val input = BufferedReader(
                InputStreamReader(
                    System.`in`
                )
            )
            try {
                parse(input.readLine())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun printPrompt() {
        print(PROMPT)
    }

    fun printHelp() {}
    fun printCurrentPackage() {
        println(currentPackge)
    }

    fun parse(input: String) {
        val command = input.split("[ \t]").toTypedArray()
        val cmd = seachCmd(command[0])
        if (cmd != null) {
            val result: String?
            result = try {
                cmd.execute(command, xda)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                printPrompt()
                return
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                printPrompt()
                return
            } catch (e: XDAException) {
                println(e.info)
                e.printStackTrace()
                printPrompt()
                return
            } catch (e: IOException) {
                e.printStackTrace()
                printPrompt()
                return
            }
            result?.let { println(it) }
        }
        printPrompt()
    }

    private fun regCmds() {
        cmdMap["create"] = Create()
        cmdMap["open"] = Open()
        cmdMap["close"] = Close()
        cmdMap["save"] = Save()
        cmdMap["saveas"] = SavaAs()
        cmdMap["addfile"] = AddFile()
        cmdMap["replacefile"] = ReplaceFile()
        cmdMap["removefile"] = RemoveFile()
        cmdMap["adddir"] = AddDir()
        cmdMap["removedir"] = RemoveDir()
        cmdMap["extractfile"] = ExtractFile()
        cmdMap["extractdir"] = ExtractDir()
        cmdMap["getmajorversion"] = GetMajorVersion()
        cmdMap["getminorversion"] = GetMinorVersion()
        cmdMap["exit"] = Exit()
        cmdMap["which"] = Which()
        cmdMap["help"] = Help()
    }

    private fun seachCmd(cmd: String): Cmd? {
        return if (cmd == "") null else cmdMap[cmd.lowercase(Locale.getDefault())] ?: return nullCmd
    }

    internal open inner class Cmd {
        @Throws(FileNotFoundException::class, XDAException::class, IOException::class, NoSuchAlgorithmException::class)
        open fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            return "Invalid Command!"
        }

        open fun printHelp() {}
        fun syntax() {
            println("Usage:")
            usage()
            println()
            println("Where:")
            where()
            println()
        }

        protected open fun usage() {}
        protected open fun where() {}
    }

    internal inner class Create : Cmd() {
        @Throws(FileNotFoundException::class, XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size < 2) {
                return MISSING_PARAMETER
            } else if (command.size == 2) {
                xdaPack.create(command[1])
            } else {
                val b: Byte = try {
                    command[2].toByte()
                } catch (e: NumberFormatException) {
                    return "Invalid parameter!"
                }
                xdaPack.create(command[1], b)
            }
            currentPackge = File(command[1]).absolutePath
            return SUCCESS
        }

        override fun printHelp() {
            println("Create			新建空XDA文档")
        }

        override fun usage() {
            println("create <路径名> [位参数 ]")
        }

        override fun where() {
            println("路径名: 必选参数。生成文件的路径名。")
            println("位参数: 可选参数。有效值为0,2,4,8。")
        }
    }

    internal inner class Open : Cmd() {
        @Throws(IOException::class, XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size < 2) {
                return MISSING_PARAMETER
            } else {
                xdaPack.open(command[1])
            }
            currentPackge = File(command[1]).absolutePath
            return SUCCESS
        }

        override fun printHelp() {
            println("Open			打开文件")
        }

        override fun usage() {
            println("Open <路径名>")
        }

        override fun where() {
            println("路径名: 必选参数。打开文件的路径名。")
        }
    }

    internal inner class Close : Cmd() {
        @Throws(IOException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            xdaPack.close()
            return SUCCESS
        }

        override fun printHelp() {
            println("Close			关闭当前文件")
        }

        override fun usage() {
            println("Close")
        }

        override fun where() {
            println("没有参数。")
        }
    }

    internal inner class Save : Cmd() {
        @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size == 1) {
                xdaPack.save()
            } else if (command.size == 3) {
                xdaPack.save(
                    command[1].toBoolean(),
                    command[2].toBoolean()
                )
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("Save			保存这次修改")
        }

        override fun usage() {
            println("Save [<NameTable是否加密> <ItemList是否加密>]")
        }

        override fun where() {
            println("NameTable是否加密: true or false")
            println("ItemList是否加密: true or false")
        }
    }

    internal inner class SavaAs : Cmd() {
        @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size == 2) {
                xdaPack.saveAs(command[1])
            } else if (command.size == 5) {
                xdaPack.saveAs(
                    command[1], command[2].toByte(),
                    command[3].toBoolean(),
                    command[4].toBoolean()
                )
            } else {
                return MISSING_PARAMETER
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("SavaAs			另存为")
        }

        override fun usage() {
            println("SavaAs <路径名> [<位参数> <NameTable是否加密> <ItemList是否加密>]")
        }

        override fun where() {
            println("路径名: 另存为文件的路径名")
            println("位参数: 可选参数。可以为0,2,4,8")
            println("NameTable是否加密: true or false")
            println("ItemList是否加密: true or false")
        }
    }

    internal inner class AddFile : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            var ecs: ByteArray? = null
            ecs = if (isCompress) ecsC else ecsNC
            if (command.size < 3) return MISSING_PARAMETER else {
                xdaPack.addFile(command[1], command[2], ecs)
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("AddFile			添加文件")
        }

        override fun usage() {
            println("AddFile <包内路径> <文件路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
            println("文件路径: 必选参数。被操作文件的实际路径")
        }
    }

    internal inner class ReplaceFile : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            var ecs: ByteArray? = null
            ecs = if (isCompress) ecsC else ecsNC
            if (command.size < 3) return MISSING_PARAMETER else {
                xdaPack.replaceFile(command[1], command[2], ecs)
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("ReplaceFile		替换文件")
        }

        override fun usage() {
            println("ReplaceFile <包内路径> <文件路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
            println("文件路径: 必选参数。被操作文件的实际路径")
        }
    }

    internal inner class RemoveFile : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size < 2) return MISSING_PARAMETER else {
                xdaPack.removeFile(command[1])
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("RemoveFile		移除文件")
        }

        override fun usage() {
            println("RemoveFile <包内路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
        }
    }

    internal inner class AddDir : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            var ecs: ByteArray? = null
            ecs = if (isCompress) ecsC else ecsNC
            if (command.size == 2) {
                xdaPack.addDir("", command[1], ecs)
            } else if (command.size == 3) {
                xdaPack.addDir(command[1], command[2], ecs)
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("AddDir			添加一个文件夹得内容")
        }

        override fun usage() {
            println("AddDir <包内路径> <文件夹路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
            println("文件夹路径: 必选参数。被操作文件夹的实际路径")
        }
    }

    internal inner class RemoveDir : Cmd() {
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size < 2) return MISSING_PARAMETER else {
                xdaPack.removeDir(command[1])
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("RemoveDir		删除一个文件夹得内容")
        }

        override fun usage() {
            println("RemoveDir <包内路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
        }
    }

    internal inner class ExtractFile : Cmd() {
        @Throws(FileNotFoundException::class, IOException::class, XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size < 3) return MISSING_PARAMETER else {
                xdaPack.extractFile(command[1], command[2])
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("ExtractFile		提取一个文件内容")
        }

        override fun usage() {
            println("ExtractFile <包内路径> <文件路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
            println("文件路径: 必选参数。存放提取内容的文件路径。")
        }
    }

    internal inner class ExtractDir : Cmd() {
        @Throws(IOException::class, XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size == 2) {
                xdaPack.extractDir(command[1])
            } else if (command.size == 3) {
                xdaPack.extractDir(command[1], command[2])
            } else {
                return MISSING_PARAMETER
            }
            return SUCCESS
        }

        override fun printHelp() {
            println("ExtractDir		提取一个文件夹内容")
        }

        override fun usage() {
            println("ExtractDir <包内路径> <文件夹路径>")
        }

        override fun where() {
            println("包内路径: 必选参数。从根开始。")
            println("文件夹路径: 必选参数。存放提取内容的文件夹路径。")
        }
    }

    internal inner class GetMajorVersion : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            println(xdaPack.majorVersion)
            return SUCCESS
        }

        override fun printHelp() {
            println("GetMajorVersion		获取主版本号")
        }

        override fun usage() {
            println("GetMajorVersion")
        }

        override fun where() {
            println("没有参数。")
        }
    }

    internal inner class GetMinorVersion : Cmd() {
        @Throws(XDAException::class)
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            println(xdaPack.minorVersion)
            return SUCCESS
        }

        override fun printHelp() {
            println("GetMinorVersion		获取次本号")
        }

        override fun usage() {
            println("GetMinorVersion")
        }

        override fun where() {
            println("没有参数。")
        }
    }

    internal inner class Help : Cmd() {
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            if (command.size == 1) {
                printAllCmd()
            } else {
                val cmd = cmdMap[command[1]]
                if (cmd == null) printAllCmd() else cmd.syntax()
            }
            return null
        }

        override fun printHelp() {
            println("Help			帮助")
        }

        override fun usage() {
            println("Help [命令名]")
        }

        override fun where() {
            println("命令名: 可选参数。待查询使用方法的命令名。如果没有此参数则为查看所有命令。")
        }

        private fun printAllCmd() {
            val col: Collection<Cmd> = cmdMap.values
            val it = col.iterator()
            while (it.hasNext()) {
                val c = it.next()
                c.printHelp()
            }
        }
    }

    internal inner class Exit : Cmd() {
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            System.exit(0)
            return null
        }

        override fun printHelp() {
            println("Exit			退出命令行")
        }

        override fun usage() {
            println("Exit")
        }

        override fun where() {
            println("没有参数。")
        }
    }

    internal inner class Which : Cmd() {
        override fun execute(command: Array<String>, xdaPack: XDAInterface): String? {
            println(currentPackge)
            return null
        }

        override fun printHelp() {
            println("Which			显示当前包路径")
        }

        override fun usage() {
            println("Which")
        }

        override fun where() {
            println("没有参数。")
        }
    }

    companion object {
        const val PROMPT = "-"
        const val MISSING_PARAMETER = "Missing parameter!"
        const val SUCCESS = "SUCCESS!"
    }

    init {
        xda = XDAContainer()
        cmdMap = HashMap()
        regCmds()
        isCompress = true
    }
}