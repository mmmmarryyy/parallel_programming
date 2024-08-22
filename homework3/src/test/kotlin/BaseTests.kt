import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

class BaseTests {
    @Test()
    @Timeout(100, unit = TimeUnit.MILLISECONDS)
    internal fun testPipeline() {
        var ok = true
        val recieved = AtomicInteger(0)
        var freeFlowJobs: Array<Job<Int>> = arrayOf(
            Job{input: Channel<Int>, output: Channel<Int> ->
                output.send(1)
                delay(10)
                val currReceived = recieved.get()
                if (currReceived == 0) {
                    ok = false
                }
            },
            Job{ input: Channel<Int>, output: Channel<Int> ->
                for (skip in input) {
                    recieved.incrementAndGet()
                }
            }
        )

        val executor = PipelineExecutor<Int>()
        executor.execute(*freeFlowJobs)
        Assertions.assertTrue(ok && recieved.get() != 0, "no value free flow - dont collect them")
    }

    @Test
    @Timeout(3)
    internal fun testSigner() {
        val testExpected = "1173136728138862632818075107442090076184424490584241521304_1696913515191343735512658979631549563179965036907783101867_27225454331033649287118297354036464389062965355426795162684_29568666068035183841425683795340791879727309630931025356555_3994492081516972096677631278379039212655368881548151736_4958044192186797981418233587017209679042592862002427381542_4958044192186797981418233587017209679042592862002427381542"
        var testResult = "NOT_SET"
        val inputData = arrayOf(0, 1, 1, 2, 3, 5, 8)

        val hashSignJobs: Array<Job<String>> = arrayOf(
            Job{input: Channel<String>, output: Channel<String> ->
                for (value in inputData) {
                    output.send(value.toString())
                }
            },
            singleHash,
            multiHash,
            combineResults,
            Job{input: Channel<String>, output: Channel<String> ->
                testResult = input.receive()
            }
        )

        val executor = PipelineExecutor<String>()
        executor.execute(*hashSignJobs)
        Assertions.assertEquals(testExpected, testResult,"results not match")
    }

    @Test
    @Timeout(400, unit = TimeUnit.MILLISECONDS)
    internal fun testExtra() {
        val received = AtomicInteger(0)
        val freeFlowJobs: Array<Job<Int>> = arrayOf(
            Job{input: Channel<Int>, output: Channel<Int> ->
                output.send(1)
                output.send(3)
                output.send(4)
            },
            Job{ input: Channel<Int>, output: Channel<Int> ->
                for (value in input) {
                    output.send(value * 3)
                    delay(100)
                }
            },
            Job{ input: Channel<Int>, output: Channel<Int> ->
                for (value in input) {
                    received.addAndGet(value)
                }
            }
        )

        val executor = PipelineExecutor<Int>()
        executor.execute(*freeFlowJobs)
        Assertions.assertEquals((1+3+4)*3, received.get(),"f3 have not collected inputs")
    }
}