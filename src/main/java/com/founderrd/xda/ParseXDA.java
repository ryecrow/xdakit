/**
 * Title:	ParseXDA
 * Description:	命令行方式提供测试接口
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;

public class ParseXDA {
    public static void main(String[] args) {
        Command c = new Command();
        c.launch();
    }
}

class Command {
    static final String PROMPT = "-";
    static final String MISSING_PARAMETER = "Missing parameter!";
    static final String SUCCESS = "SUCCESS!";

    private final Cmd nullCmd = new Cmd();
    private final byte[] ecsC = new byte[]{(byte) 0x02, (byte) 0xff};
    private final byte[] ecsNC = new byte[]{(byte) 0xff};
    private final boolean isCompress;
    private final XDAInterface xda;
    private final HashMap<String, Cmd> cmdMap;
    private String currentPackge;

    Command() {
        this.currentPackge = "None!";
        this.xda = new XDAContainer();
        this.cmdMap = new HashMap<>();
        this.regCmds();
        this.isCompress = true;
    }

    void launch() {
        System.out.println("Start...");
        System.out.println("Input command, please.");
        printPrompt();

        while (true) {
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    System.in));
            try {
                parse(input.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void printPrompt() {
        System.out.print(PROMPT);
    }

    void printHelp() {

    }

    void printCurrentPackage() {
        System.out.println(currentPackge);
    }

    void parse(String input) {
        String[] command = input.split("[ \t]");
        Cmd cmd = seachCmd(command[0]);
        if (cmd != null) {
            String result;
            try {
                result = cmd.execute(command, this.xda);
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                printPrompt();
                return;
            } catch (XDAException e) {
                System.out.println(e.getInfo());
                e.printStackTrace();
                printPrompt();
                return;
            }
            if (result != null)
                System.out.println(result);
        }

        printPrompt();
    }

    private void regCmds() {
        cmdMap.put("create", new Create());
        cmdMap.put("open", new Open());
        cmdMap.put("close", new Close());
        cmdMap.put("save", new Save());
        cmdMap.put("saveas", new SavaAs());
        cmdMap.put("addfile", new AddFile());
        cmdMap.put("replacefile", new ReplaceFile());
        cmdMap.put("removefile", new RemoveFile());
        cmdMap.put("adddir", new AddDir());
        cmdMap.put("removedir", new RemoveDir());
        cmdMap.put("extractfile", new ExtractFile());
        cmdMap.put("extractdir", new ExtractDir());
        cmdMap.put("getmajorversion", new GetMajorVersion());
        cmdMap.put("getminorversion", new GetMinorVersion());
        cmdMap.put("exit", new Exit());
        cmdMap.put("which", new Which());
        cmdMap.put("help", new Help());
    }

    private Cmd seachCmd(String cmd) {
        if (cmd.equals(""))
            return null;
        Cmd result = cmdMap.get(cmd.toLowerCase());
        if (result == null)
            return this.nullCmd;

        return result;
    }

    class Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException, IOException,
                NoSuchAlgorithmException {
            return "Invalid Command!";
        }

        void printHelp() {

        }

        final void syntax() {
            System.out.println("Usage:");
            usage();
            System.out.println();
            System.out.println("Where:");
            where();
            System.out.println();
        }

        protected void usage() {

        }

        protected void where() {

        }
    }

    class Create extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws FileNotFoundException, XDAException {
            if (command.length < 2) {
                return MISSING_PARAMETER;
            } else if (command.length == 2) {
                xdaPack.create(command[1]);
            } else {
                byte b;
                try {
                    b = Byte.parseByte(command[2]);
                } catch (NumberFormatException e) {
                    return "Invalid parameter!";
                }

                xdaPack.create(command[1], b);
            }

            currentPackge = (new File(command[1])).getAbsolutePath();
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("Create			新建空XDA文档");
        }

        protected void usage() {
            System.out.println("create <路径名> [位参数 ]");
        }

        protected void where() {
            System.out.println("路径名: 必选参数。生成文件的路径名。");
            System.out.println("位参数: 可选参数。有效值为0,2,4,8。");
        }
    }

    class Open extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws IOException, XDAException {
            if (command.length < 2) {
                return MISSING_PARAMETER;
            } else {
                xdaPack.open(command[1]);
            }

            currentPackge = (new File(command[1])).getAbsolutePath();
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("Open			打开文件");
        }

        protected void usage() {
            System.out.println("Open <路径名>");
        }

        protected void where() {
            System.out.println("路径名: 必选参数。打开文件的路径名。");
        }
    }

    class Close extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws IOException {
            xdaPack.close();
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("Close			关闭当前文件");
        }

        protected void usage() {
            System.out.println("Close");
        }

        protected void where() {
            System.out.println("没有参数。");
        }
    }

    class Save extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws NoSuchAlgorithmException, XDAException, IOException {
            if (command.length == 1) {
                xdaPack.save();
            } else if (command.length == 3) {
                xdaPack.save(Boolean.parseBoolean(command[1]), Boolean.parseBoolean(command[2]));
            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("Save			保存这次修改");
        }

        protected void usage() {
            System.out.println("Save [<NameTable是否加密> <ItemList是否加密>]");
        }

        protected void where() {
            System.out.println("NameTable是否加密: true or false");
            System.out.println("ItemList是否加密: true or false");
        }
    }

    class SavaAs extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws NoSuchAlgorithmException, XDAException, IOException {
            if (command.length == 2) {
                xdaPack.saveAs(command[1]);
            } else if (command.length == 5) {
                xdaPack.saveAs(command[1], Byte.parseByte(command[2]),
                        Boolean.parseBoolean(command[3]), Boolean.parseBoolean(command[4]));
            } else {
                return MISSING_PARAMETER;
            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("SavaAs			另存为");
        }

        protected void usage() {
            System.out.println("SavaAs <路径名> [<位参数> <NameTable是否加密> <ItemList是否加密>]");
        }

        protected void where() {
            System.out.println("路径名: 另存为文件的路径名");
            System.out.println("位参数: 可选参数。可以为0,2,4,8");
            System.out.println("NameTable是否加密: true or false");
            System.out.println("ItemList是否加密: true or false");
        }
    }

    class AddFile extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            byte[] ecs;
            if (isCompress)
                ecs = ecsC;
            else
                ecs = ecsNC;
            if (command.length < 3)
                return MISSING_PARAMETER;
            else {
                xdaPack.addFile(command[1], command[2], ecs);

            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("AddFile			添加文件");
        }

        protected void usage() {
            System.out.println("AddFile <包内路径> <文件路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
            System.out.println("文件路径: 必选参数。被操作文件的实际路径");
        }
    }

    class ReplaceFile extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            byte[] ecs;
            if (isCompress)
                ecs = ecsC;
            else
                ecs = ecsNC;
            if (command.length < 3)
                return MISSING_PARAMETER;
            else {
                xdaPack.replaceFile(command[1], command[2], ecs);

            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("ReplaceFile		替换文件");
        }

        protected void usage() {
            System.out.println("ReplaceFile <包内路径> <文件路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
            System.out.println("文件路径: 必选参数。被操作文件的实际路径");
        }
    }

    class RemoveFile extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            if (command.length < 2)
                return MISSING_PARAMETER;
            else {
                xdaPack.removeFile(command[1]);

            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("RemoveFile		移除文件");
        }

        protected void usage() {
            System.out.println("RemoveFile <包内路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
        }
    }

    class AddDir extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            byte[] ecs;
            if (isCompress)
                ecs = ecsC;
            else
                ecs = ecsNC;
            if (command.length == 2) {
                xdaPack.addDir("", command[1], ecs);
            } else if (command.length == 3) {
                xdaPack.addDir(command[1], command[2], ecs);
            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("AddDir			添加一个文件夹得内容");
        }

        protected void usage() {
            System.out.println("AddDir <包内路径> <文件夹路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
            System.out.println("文件夹路径: 必选参数。被操作文件夹的实际路径");
        }
    }

    class RemoveDir extends Cmd {
        String execute(String[] command, XDAInterface xdaPack) {
            if (command.length < 2)
                return MISSING_PARAMETER;
            else {
                xdaPack.removeDir(command[1]);

            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("RemoveDir		删除一个文件夹得内容");
        }

        protected void usage() {
            System.out.println("RemoveDir <包内路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
        }
    }

    class ExtractFile extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws IOException, XDAException {
            if (command.length < 3)
                return MISSING_PARAMETER;
            else {
                xdaPack.extractFile(command[1], command[2]);
            }

            return SUCCESS;
        }

        void printHelp() {
            System.out.println("ExtractFile		提取一个文件内容");
        }

        protected void usage() {
            System.out.println("ExtractFile <包内路径> <文件路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
            System.out.println("文件路径: 必选参数。存放提取内容的文件路径。");
        }
    }

    class ExtractDir extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws IOException, XDAException {
            if (command.length == 2) {
                xdaPack.extractDir(command[1]);
            } else if (command.length == 3) {
                xdaPack.extractDir(command[1], command[2]);
            } else {
                return MISSING_PARAMETER;
            }
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("ExtractDir		提取一个文件夹内容");
        }

        protected void usage() {
            System.out.println("ExtractDir <包内路径> <文件夹路径>");
        }

        protected void where() {
            System.out.println("包内路径: 必选参数。从根开始。");
            System.out.println("文件夹路径: 必选参数。存放提取内容的文件夹路径。");
        }
    }

    class GetMajorVersion extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            System.out.println(xdaPack.getMajorVersion());
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("GetMajorVersion		获取主版本号");
        }

        protected void usage() {
            System.out.println("GetMajorVersion");
        }

        protected void where() {
            System.out.println("没有参数。");
        }
    }

    class GetMinorVersion extends Cmd {
        String execute(String[] command, XDAInterface xdaPack)
                throws XDAException {
            System.out.println(xdaPack.getMinorVersion());
            return SUCCESS;
        }

        void printHelp() {
            System.out.println("GetMinorVersion		获取次本号");
        }

        protected void usage() {
            System.out.println("GetMinorVersion");
        }

        protected void where() {
            System.out.println("没有参数。");
        }
    }

    class Help extends Cmd {
        String execute(String[] command, XDAInterface xdaPack) {
            if (command.length == 1) {
                printAllCmd();
            } else {
                Cmd cmd = cmdMap.get(command[1]);
                if (cmd == null)
                    printAllCmd();
                else
                    cmd.syntax();
            }

            return null;
        }

        void printHelp() {
            System.out.println("Help			帮助");
        }

        protected void usage() {
            System.out.println("Help [命令名]");
        }

        protected void where() {
            System.out.println("命令名: 可选参数。待查询使用方法的命令名。如果没有此参数则为查看所有命令。");
        }

        private void printAllCmd() {
            Collection<Cmd> col = cmdMap.values();
            for (Cmd c : col) {
                c.printHelp();
            }
        }
    }

    class Exit extends Cmd {
        String execute(String[] command, XDAInterface xdaPack) {
            System.exit(0);
            return null;
        }

        void printHelp() {
            System.out.println("Exit			退出命令行");
        }

        protected void usage() {
            System.out.println("Exit");
        }

        protected void where() {
            System.out.println("没有参数。");
        }
    }

    class Which extends Cmd {
        String execute(String[] command, XDAInterface xdaPack) {
            System.out.println(currentPackge);
            return null;
        }

        void printHelp() {
            System.out.println("Which			显示当前包路径");
        }

        protected void usage() {
            System.out.println("Which");
        }

        protected void where() {
            System.out.println("没有参数。");
        }
    }
}
