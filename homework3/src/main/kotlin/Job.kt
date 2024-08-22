/***********************
 DO NOT TOUCH IT FILE!!!
 ***********************/

import kotlinx.coroutines.channels.*

fun interface Job<T> {
    suspend fun run(input: Channel<T>, output: Channel<T>)
}