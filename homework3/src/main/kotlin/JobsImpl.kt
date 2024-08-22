/***********************
 Implement the functions/jobs for Pipeline:
 SingleHash[job] = crc32(data)+"~"+crc32(md5(data))
 MultiHash[job] = join(crc32(th+data), ""); th = 0..5
 CombineResults[job] = join(sort(results), "_")
 crc32[fun] = implement with use DataSignerCrc32
 md5[fun] = implement with use DataSignerMd5
 ***********************/

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

val singleHash = Job { input: Channel<String>, output: Channel<String> ->
    val elems = mutableListOf<String>()
    for (data in input) {
        elems.add(data)
    }
    val crc32Data = MutableList(elems.size) { "" }
    val md5Data = MutableList(elems.size) { "" }
    val crc32md5Data = MutableList(elems.size) { "" }

    coroutineScope {
        val firstLaunch = launch {
            for ((index, elem) in elems.withIndex()) {
                launch {
                    crc32Data[index] = dataSignerCrc32(elem)
                }
            }
        }

        for ((index, elem) in elems.withIndex()) {
            md5Data[index] = dataSignerMd5(elem)
        }

        val secondLaunch = launch {
            for (index in elems.indices) {
                launch {
                    crc32md5Data[index] = dataSignerCrc32(md5Data[index])
                }
            }
        }

        firstLaunch.join()
        secondLaunch.join()

        for (index in 0 until elems.size) {
            launch {
                val answer = "${crc32Data[index]}~${crc32md5Data[index]}"
                output.send(answer)
            }
        }
    }
}

val multiHash = Job { input: Channel<String>, output: Channel<String> ->
    val elems = mutableListOf<String>()
    for (data in input) {
        elems.add(data)
    }

    val outputAnswers = MutableList(elems.size) { MutableList(6) {""} }

    coroutineScope {
        for ((elemsIndex, elem) in elems.withIndex()) {
            for (index in 0..5) {
                launch {
                    outputAnswers[elemsIndex][index] = dataSignerCrc32("$index$elem")
                }
            }
        }
    }

    for (index in 0 until elems.size) {
        val answer = outputAnswers[index].joinToString(separator = "")
        output.send(answer)
    }
}

val combineResults = Job { input: Channel<String>, output: Channel<String> ->
    val results = mutableListOf<String>()

    for (result in input) {
        results.add(result)
    }

    val answer = results.sorted().joinToString(separator = "_")
    output.send(answer)
}