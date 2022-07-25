package svcs

fun main(args: Array<String>) {
    if (args.isEmpty()) help() else {
        when (args[0]) {
            "--help" -> help()
            "config" -> println("Get and set a username.")
            "add" -> println("Add a file to the index.")
            "log" -> println("Show commit logs.")
            "commit" -> println("Save changes.")
            "checkout" -> println("Restore a file.")
            else -> println("'${args[0]}' is not a SVCS command.")
        }
    }
}

fun help(){
    println("""
            These are SVCS commands:
            config     Get and set a username.
            add        Add a file to the index.
            log        Show commit logs.
            commit     Save changes.
            checkout   Restore a file.
        """.trimIndent())
}