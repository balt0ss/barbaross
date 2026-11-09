package dev.barbaross.ipc

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildProtocolTest {
    @Test
    fun currentVersionIsCompatible() {
        assertTrue(BuildProtocol.isCompatible(BuildProtocol.VERSION))
    }

    @Test
    fun olderThanMinimumIsRejected() {
        assertFalse(BuildProtocol.isCompatible(BuildProtocol.MIN_SUPPORTED_VERSION - 1))
    }

    @Test
    fun newerRuntimeThanIdeIsRejected() {
        assertFalse(BuildProtocol.isCompatible(BuildProtocol.VERSION + 1))
    }
}
