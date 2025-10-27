package dev.chris.emvtlvparser

import dev.chris.emvtlvparser.singleton.EmvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EmvParserTests {


    @Test
    fun `Correct Parsing - Simple TLV`() {
        val tlv = "9F0206000000001234"
        val items = EmvParser.parseTlv(tlv)

        // Use the real JUnit assertions
        assertEquals(1, items.size)
        assertEquals("9F02", items[0].tag)
        assertEquals("06", items[0].rawLength)
        assertEquals(6, items[0].length)
        assertEquals("000000001234", items[0].value)
        assertEquals("Amount, Authorised (Numeric): 12.34", items[0].interpretation)
    }

    @Test
    fun `Correct Parsing - Nested TLV (User Example)`() {
        val tlv = "6F188407A0000000031010A50D500B5649534120435245444954"
        val items = EmvParser.parseTlv(tlv)

        assertEquals(4, items.size)
        assertEquals("6F", items[0].tag)
        assertEquals(0, items[0].level)
        assertEquals("84", items[1].tag)
        assertEquals(1, items[1].level)
        assertEquals("A0000000031010", items[1].value)
        assertEquals("Dedicated File (DF) Name (AID): A0000000031010", items[1].interpretation)
        assertEquals("A5", items[2].tag)
        assertEquals(1, items[2].level)
        assertEquals("50", items[3].tag)
        assertEquals(2, items[3].level)
        assertEquals("5649534120435245444954", items[3].value)
    }

    @Test
    fun `Correct Parsing - Multi-byte Length (Long Form)`() {
        val value_128_bytes = "00".repeat(128)
        val tlv = "5A8180$value_128_bytes"
        val items = EmvParser.parseTlv(tlv)

        assertEquals(1, items.size) // This will now pass
        assertEquals("5A", items[0].tag)
        assertEquals("8180", items[0].rawLength)
        assertEquals(128, items[0].length)
        assertEquals(value_128_bytes, items[0].value)
    }

    @Test
    fun `Invalid TLV - Truncated Value`() {
        val tlv = "9F02060000"

        // Use assertThrows to verify the expected exception
        assertThrows<IllegalArgumentException> {
            EmvParser.parseTlv(tlv)
        }
    }

    @Test
    fun `Invalid TLV - Truncated Length`() {
        val tlv = "9F0281"

        assertThrows<IllegalArgumentException> {
            EmvParser.parseTlv(tlv)
        }
    }

    @Test
    fun `Invalid TLV - Malformed Hex (Odd characters)`() {
        val tlv = "9F020"

        assertThrows<IllegalArgumentException> {
            EmvParser.parseTlv(tlv)
        }
    }

    @Test
    fun `Unknown Tag Handling`() {
        val tlv = "C10101"
        val items = EmvParser.parseTlv(tlv)

        assertEquals(1, items.size)
        assertEquals("C1", items[0].tag)
        assertEquals("Unknown Tag", items[0].interpretation)
    }
}
