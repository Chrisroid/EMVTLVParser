package dev.chris.emvtlvparser.singleton

import dev.chris.emvtlvparser.model.TlvItem
import java.nio.charset.StandardCharsets

object EmvParser {

    private val emvTagDictionary = mapOf(
        "4F" to "Application Identifier (AID)",
        "50" to "Application Label",
        "57" to "Track 2 Equivalent Data",
        "5A" to "Application Primary Account Number (PAN)",
        "5F20" to "Cardholder Name",
        "5F24" to "Application Expiration Date (YYMMDD)",
        "5F25" to "Application Effective Date (YYMMDD)",
        "5F28" to "Issuer Country Code",
        "5F2A" to "Transaction Currency Code",
        "5F34" to "Application PAN Sequence Number",
        "82" to "Application Interchange Profile (AIP)",
        "84" to "Dedicated File (DF) Name (AID)",
        "8A" to "Authorisation Response Code (ARC)",
        "8E" to "Cardholder Verification Method (CVM) List",
        "95" to "Terminal Verification Results (TVR)",
        "9A" to "Transaction Date (YYMMDD)",
        "9C" to "Transaction Type",
        "9F02" to "Amount, Authorised (Numeric)",
        "9F03" to "Amount, Other (Numeric)",
        "9F06" to "Application Identifier (AID) - Terminal",
        "9F08" to "Application Version Number",
        "9F09" to "Application Version Number - Terminal",
        "9F10" to "Issuer Application Data (IAD)",
        "9F1A" to "Terminal Country Code",
        "9F1E" to "Interface Device (IFD) Serial Number",
        "9F26" to "Application Cryptogram (AC)",
        "9F27" to "Cryptogram Information Data (CID)",
        "9F33" to "Terminal Capabilities",
        "9F34" to "Cardholder Verification Method (CVM) Results",
        "9F35" to "Terminal Type",
        "9F36" to "Application Transaction Counter (ATC)",
        "9F37" to "Unpredictable Number (UN)",
        "9F41" to "Transaction Sequence Counter",
        "A5" to "File Control Information (FCI) Proprietary Template",
        "BF0C" to "File Control Information (FCI) Issuer Discretionary Data",
        "C7" to "CDOL 1 Related Data",
        "6F" to "File Control Information (FCI) Template",
        "70" to "Application Elementary File (AEF) Template",
        "77" to "Response Message Template Format 2"
    )

    /**
     * Public entry point for parsing a TLV hex string.
     */
    fun parseTlv(hexData: String): List<TlvItem> {
        val cleanHex = hexData.replace(Regex("\\s+"), "")
        if (cleanHex.length % 2 != 0) {
            throw IllegalArgumentException("Hex string must have an even number of characters.")
        }
        val data = cleanHex.hexToByteArray()
        return try {
            parseTlvRecursive(data, 0)
        } catch (e: IndexOutOfBoundsException) {
            // --- THIS IS WHERE THE ERROR IS NOW CAUGHT ---
            throw IllegalArgumentException("Data is truncated or malformed.", e)
        }
    }

    /**
     * Recursively parses a byte array of TLV data.
     * This function is now stricter and throws IndexOutOfBoundsException on any malformed data.
     */
    private fun parseTlvRecursive(data: ByteArray, level: Int): MutableList<TlvItem> {
        val items = mutableListOf<TlvItem>()
        var index = 0

        while (index < data.size) {
            // --- 1. Parse Tag ---
            var tagBytes = 1
            if (index >= data.size) break // Gracefully exit if just padding

            val b1 = data[index]
            val isConstructed = (b1.toInt() and 0x20) != 0 // Check bit 6

            val tag: String
            if ((b1.toInt() and 0x1F) == 0x1F) { // Multi-byte tag
                tagBytes = 2
                while (index + tagBytes - 1 < data.size && data[index + tagBytes - 1].toInt() and 0x80 != 0) {
                    tagBytes++
                }
            }
            if (index + tagBytes > data.size) throw IndexOutOfBoundsException("Malformed tag at index $index")
            tag = data.copyOfRange(index, index + tagBytes).toHexString()

            index += tagBytes

            // --- 2. Parse Length ---
            var lengthBytes = 1
            var length: Int
            val rawLength: String

            if (index >= data.size) throw IndexOutOfBoundsException("Malformed length at index $index")
            val l1 = data[index].toInt() and 0xFF

            if (l1 and 0x80 != 0) { // Long form
                val numLengthBytes = l1 and 0x7F
                lengthBytes += numLengthBytes
                if (index + lengthBytes > data.size) throw IndexOutOfBoundsException("Malformed length at index $index")
                rawLength = data.copyOfRange(index, index + lengthBytes).toHexString()
                length = data.copyOfRange(index + 1, index + lengthBytes)
                    .fold(0) { acc, byte -> (acc shl 8) or (byte.toInt() and 0xFF) }
            } else { // Short form
                rawLength = data.copyOfRange(index, index + 1).toHexString()
                length = l1
            }

            index += lengthBytes

            // --- 3. Parse Value ---
            if (index + length > data.size) throw IndexOutOfBoundsException("Value length $length exceeds data size at index $index")

            val value: ByteArray = data.copyOfRange(index, index + length)
            val valueHex: String = value.toHexString()

            // --- 4. Interpret & Recurse ---
            val interpretation = interpretValue(tag, value)

            items.add(TlvItem(tag, rawLength, length, valueHex, interpretation, level))

            if (isConstructed) {
                // If constructed, parse the value as nested TLV
                items.addAll(parseTlvRecursive(value, level + 1))
            }

            index += length
        }
        return items
    }

    /**
     * Provides a human-readable interpretation for a given tag and its value.
     */
    private fun interpretValue(tag: String, valueBytes: ByteArray): String {
        val baseInterpretation = emvTagDictionary[tag] ?: "Unknown Tag"
        val valueHex = valueBytes.toHexString()

        return when (tag) {
            "57", "5A" -> {
                val pan = valueHex.takeWhile { it != 'F' && it != 'f' }
                val maskedPan = maskPan(pan)
                "$baseInterpretation: $maskedPan"
            }
            "84" -> {
                "$baseInterpretation: $valueHex"
            }
            "9F02" -> {
                val amount = valueHex.toLongOrNull() ?: 0L
                "$baseInterpretation: ${"%.2f".format(amount / 100.0)}"
            }
            "50" -> "$baseInterpretation: ${valueBytes.toString(StandardCharsets.US_ASCII).trim()}"
            "5F20" -> "$baseInterpretation: ${valueBytes.toString(StandardCharsets.US_ASCII).trim()}"
            else -> baseInterpretation
        }
    }


    private fun String.hexToByteArray(): ByteArray {
        check(length % 2 == 0) { "Hex string must have even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02X".format(it) }

    /**
     * Masks a Primary Account Number (PAN) string.
     * Shows the first 6 and last 4 digits.
     * e.g., "4567890123451234" -> "456789...1234"
     */
    private fun maskPan(pan: String): String {
        // Only mask if the PAN is long enough to be meaningful
        return if (pan.length > 10) {
            "${pan.take(6)}...${pan.takeLast(4)}"
        } else {
            // Can't apply 6...4 mask, just show first 6
            "${pan.take(6)}..."
        }
    }
}