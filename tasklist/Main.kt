package tasklist

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    var priority: String,
    var date: String,
    var time: String,
    var lines: MutableList<String>
)

fun main() {

    val jsonFile = File("tasklist.json")

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val type = Types.newParameterizedType(List::class.java, Task::class.java)
    val adapter: JsonAdapter<List<Task>> = moshi.adapter(type)

    val tasks = mutableListOf<Task>()

    // ---------- LOAD ----------
    if (jsonFile.exists()) {
        adapter.fromJson(jsonFile.readText())?.let {
            tasks.addAll(it)
        }
    }

    fun printMenu() {
        println("Input an action (add, print, edit, delete, end):")
    }

    fun readPriority(): String {
        while (true) {
            println("Input the task priority (C, H, N, L):")
            val p = readln().trim().uppercase()
            if (p in listOf("C", "H", "N", "L")) return p
        }
    }

    fun readDate(): String {
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val input = readln().trim()
            try {
                LocalDate.parse(input)
                return input
            } catch (e: Exception) {
                println("The input date is invalid")
            }
        }
    }

    fun readTime(): String {
        while (true) {
            println("Input the time (hh:mm):")
            val input = readln().trim()
            try {
                LocalTime.parse(input)
                return input
            } catch (e: Exception) {
                println("The input time is invalid")
            }
        }
    }

    fun readTaskLines(): MutableList<String> {
        println("Input a new task (enter a blank line to end):")
        val result = mutableListOf<String>()
        while (true) {
            val line = readln()
            if (line.isBlank()) break
            result.add(line)
        }
        if (result.isEmpty()) println("The task is blank")
        return result
    }

    fun wrap(text: String): List<String> =
        text.chunked(44).map { it.padEnd(44) }

    fun printTasks() {
        if (tasks.isEmpty()) {
            println("No tasks have been input")
            return
        }

        val sep = "+----+------------+-------+---+--------------------------------------------+"
        println(sep)
        println("| ID |    Date    | Time  | P |                    Task                    |")
        println(sep)

        for ((i, task) in tasks.withIndex()) {
            val wrapped = task.lines.flatMap { wrap(it) }
            val space = if (i + 1 < 10) "  " else " "

            println("| ${i + 1}$space| ${task.date} | ${task.time} | ${task.priority} |${wrapped[0]}|")
            for (j in 1 until wrapped.size) {
                println("|    |            |       |   |${wrapped[j]}|")
            }
            println(sep)
        }
    }

    fun editTask() {
        if (tasks.isEmpty()) {
            println("No tasks have been input")
            return
        }

        printTasks()
        println("Input the task number (1-${tasks.size}):")

        val index = readln().toIntOrNull()?.minus(1) ?: run {
            println("Invalid task number")
            return
        }

        if (index !in tasks.indices) {
            println("Invalid task number")
            return
        }

        println("Input a field to edit (priority, date, time, task):")
        when (readln()) {
            "priority" -> tasks[index].priority = readPriority()
            "date" -> tasks[index].date = readDate()
            "time" -> tasks[index].time = readTime()
            "task" -> tasks[index].lines = readTaskLines()
            else -> {
                println("Invalid field")
                return
            }
        }
        println("The task is changed")
    }

    fun deleteTask() {
        if (tasks.isEmpty()) {
            println("No tasks have been input")
            return
        }

        printTasks()
        println("Input the task number (1-${tasks.size}):")

        val index = readln().toIntOrNull()?.minus(1) ?: run {
            println("Invalid task number")
            return
        }

        if (index !in tasks.indices) {
            println("Invalid task number")
            return
        }

        tasks.removeAt(index)
        println("The task is deleted")
    }

    // ---------- MAIN LOOP ----------
    while (true) {
        printMenu()
        when (readln().trim()) {
            "add" -> {
                val p = readPriority()
                val d = readDate()
                val t = readTime()
                val lines = readTaskLines()
                if (lines.isNotEmpty()) {
                    tasks.add(Task(p, d, t, lines))
                }
            }
            "print" -> printTasks()
            "edit" -> editTask()
            "delete" -> deleteTask()
            "end" -> {
                jsonFile.writeText(adapter.toJson(tasks))
                println("Tasklist exiting!")
                return
            }
            else -> println("The input action is invalid")
        }
    }
}
