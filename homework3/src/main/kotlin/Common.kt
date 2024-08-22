/***********************
 DO NOT TOUCH IT FILE!!!
 **********************/

import kotlinx.coroutines.delay
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.CRC32
import kotlin.time.Duration

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

val dataSignerOverheat = AtomicInteger(0)

suspend fun overheatLock() {
    while (true) {
        if (!dataSignerOverheat.compareAndSet(0, 1)) {
            println("OverheatLock happend")
            delay(Duration.parse("1s"))
        } else {
            break
        }
    }
}

suspend fun overheatUnlock() {
    while (true) {
        if (!dataSignerOverheat.compareAndSet(1, 0)) {
            println("OverheatUnlock happend")
            delay(Duration.parse("1s"))
        } else {
            break
        }
    }
}

suspend fun dataSignerMd5(data: String): String {
    overheatLock()
    val dataHash = data.md5()
    delay(10)
    overheatUnlock()
    return dataHash
}

suspend fun dataSignerCrc32(data: String): String {
    val crc32 = CRC32()
    crc32.update(data.toByteArray())
    val dataHash = crc32.value.toString()
    delay(Duration.parse("1s"))
    return dataHash
}