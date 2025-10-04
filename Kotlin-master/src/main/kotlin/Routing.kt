import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable
data class Task(val id: Int, val title: String, val description: String?)

@Serializable
data class ResponseMessage(val message: String)

fun Application.configureRouting() {
    val tasks = mutableListOf(
        Task(1, "Task 1", "Description 1"),
        Task(2, "Task 2", "Description 2")
    )

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/tasks") {
            val titleFilter = call.request.queryParameters["title"]
            val filtered = if (titleFilter != null) {
                tasks.filter { it.title.contains(titleFilter, ignoreCase = true) }
            } else {
                tasks
            }
            call.respond(filtered)
        }

        get("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ResponseMessage("Invalid id"))
                return@get
            }
            val task = tasks.find { it.id == id }
            if (task == null) {
                call.respond(HttpStatusCode.NotFound, ResponseMessage("Task not found"))
            } else {
                call.respond(task)
            }
        }

        post("/tasks") {
            val newTask = call.receive<Task>()
            tasks.add(newTask)
            call.respond(HttpStatusCode.Created, newTask)
        }

        delete("/tasks/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ResponseMessage("Invalid id"))
                return@delete
            }
            val removed = tasks.removeIf { it.id == id }
            if (removed) {
                call.respond(ResponseMessage("Task deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, ResponseMessage("Task not found"))
            }
        }
    }
}