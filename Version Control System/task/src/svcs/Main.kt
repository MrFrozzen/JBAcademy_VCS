package svcs

import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import kotlin.io.path.Path
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.isDirectory


fun main(args: Array<String>) {
    try {
        Files.createDirectory(Path("vcs/"))
    } catch (e: Exception) {}

    val helpMessage = """
        These are SVCS commands:
        config     Get and set a username.
        add        Add a file to the index.
        log        Show commit logs.
        commit     Save changes.
        checkout   Restore a file.
    """.trimIndent()

    if (args.isNotEmpty()) {
        when (args[0]) {
            "config" -> config(args)
            "add" -> add(args)
            "log" -> printLog()
            "commit" -> commit(args)
            "checkout" -> checkout(args)
            "--help" -> println(helpMessage)
            else -> println("\'${args[0]}\' is not a SVCS command.")
        }
    } else println(helpMessage)
}


fun config(args: Array<String>) {
    val fileName = "vcs/config.txt"
    val file = File(fileName)
    var userName = if (args.size > 1) args[1] else ""

    if (file.exists()) {
        if (args.size == 1) {
            userName = file.readText()
            println("The username is $userName.")
        } else {
            file.writeText(userName)
            println("The username is $userName.")
        }

    } else {
        if (args.size == 1) {
            println("Please, tell me who you are.")
        } else {
            file.writeText(userName)
            println("The username is $userName.")
        }
    }
}


fun add(args: Array<String>) {
    val fileName = "vcs/index.txt"
    val indexFile = File(fileName)

    val fileNames = mutableSetOf<String>()

    val addedFile = if (args.size == 2) args[1] else ""

    if (indexFile.exists()) {
        fileNames.addAll(indexFile.readLines())
        if (args.size == 1) {
            println("Tracked files:\n${fileNames.joinToString("\n")}")
        } else {
            if (File(addedFile).exists()) {
                fileNames.add(addedFile)
                indexFile.writeText(fileNames.joinToString("\n"))
                println("The file \'$addedFile\' is tracked.")
            } else println("Can't find \'$addedFile\'.")
        }

    } else {
        if (args.size == 1) {
            println("Add a file to the index.")
        } else {
            if (File(addedFile).exists()) {
                indexFile.writeText(addedFile)
                println("The file \'$addedFile\' is tracked.")
            } else println("Can't find \'$addedFile\'.")
        }
    }
}


fun commit(args: Array<String>) {
    val msgDigest = MessageDigest.getInstance("SHA-1")
    val indexFile = File("vcs/index.txt")
    val fileNames = mutableListOf<String>()
    var hash = ""

    val commitMsg = if (args.size > 1) args[1] else ""

    if (indexFile.exists()) {
        fileNames.addAll(indexFile.readLines())
        for (i in fileNames.indices) {
            val bytes = File(fileNames[i]).readBytes()
            val fileHash = BigInteger(1, msgDigest.digest(bytes))
                .toString(16)

            hash = BigInteger(1,
                msgDigest.digest((fileHash + hash).toByteArray())
            ).toString(16)
        }
    }

    saveCommit(hash, commitMsg, fileNames)
}


fun saveCommit(hash: String,
               commitMsg: String,
               fileNames: MutableList<String>) {

    if (commitMsg.isEmpty()) {
        println("Message was not passed.")
        return
    }

    val commitDirPath = "vcs/commits/$hash"

    if (Path(commitDirPath).exists() && Path(commitDirPath).isDirectory()) {
        println("Nothing to commit.")
        return
    } else {
        val log = mutableListOf<String>()
        val logFile = File("vcs/log.txt")
        if (logFile.exists()) {
            log.addAll(logFile.readLines().toMutableList())
        }

        val configFile = File("vcs/config.txt")
        val author = if (configFile.exists()) {
            configFile.readLines().first()
        } else {
            println("Please, tell me who you are.")
            return
        }

        if (log.isNotEmpty()) log.add("")
        log.add("commit $hash")
        log.add("Author: $author")
        log.add(commitMsg)

        logFile.writeText(log.joinToString("\n"))

        for (i in fileNames.indices) {
            val file = File(fileNames[i])
            file.copyTo(File("$commitDirPath/${fileNames[i]}"))
        }

        println("Changes are committed.")
    }
}


fun printLog() {
    val logFile = File("vcs/log.txt")

    if (logFile.exists()) {
        val commits = getCommitsFromLog()

        if (commits.isNotEmpty()) {
            for (i in commits.indices) {
                println(commits[i].joinToString("\n"))
                if (i < commits.lastIndex) println()
            }
        } else println("No commits yet."); return

    } else println("No commits yet.")
}


fun getCommitsFromLog() : MutableList<MutableList<String>> {
    val logFile = File("vcs/log.txt")
    val commits = mutableListOf<MutableList<String>>()
    val log = logFile.readLines().toMutableList()

    for (i in log.indices step 4) {
        val commit = mutableListOf<String>()
        for (j in 0..2) {
            commit.add(log[i + j])
        }
        commits.add(0, commit)
    }

    return commits
}


fun checkout(args: Array<String>) {
    if (args.size == 1) {
        println("Commit id was not passed.")
    } else {
        val commitId = args[1]
        val commits = getCommitsFromLog().joinToString(" ")
        if (commits.contains(commitId)) {
            val commitDir = File("vcs/commits/$commitId")
            val indexFile = File("vcs/index.txt")
            val fileNames = indexFile.readLines()

            fileNames.forEach {
                File(it).writeText(File("$commitDir/$it").readText())
            }

            println("Switched to commit $commitId.")
        } else println("Commit does not exist.")
    }
}