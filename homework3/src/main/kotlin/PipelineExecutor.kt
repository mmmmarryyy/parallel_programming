import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel


/***********************
Implement the PipelineExecutor class with method execute.
The execute method takes jobs as a parameter and creates channels for them and starts processing
 ***********************/

class PipelineExecutor<T> {
    fun execute(vararg jobs: Job<T>) = runBlocking {
        val channels = List(jobs.size + 1) { Channel<T>() }
        for ((index, job) in jobs.withIndex()) {
            launch {
                job.run(channels[index], channels[index + 1])
                channels[index + 1].close()
                channels[index].close()
            }
        }
    }
}