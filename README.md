# EMV TLV Parser

A simple Android and JVM tool for parsing EMV (Europay, Mastercard, Visa) data encoded in BER-TLV (Tag-Length-Value) format. The tool decodes the hex data into a human-readable, hierarchical table, showing the tag, length, value, and interpretation.

This repository provides two versions:
1.  **Android App**: A simple Jetpack Compose UI for on-the-go parsing.
2.  **JVM Tests**: A pure Kotlin `EmvParser` object with a JUnit 5 test suite, which can be run as a CLI tool or integrated into other JVM projects.

---

## Features

* **BER-TLV Parsing**: Correctly parses multi-byte tags and multi-byte (long form) lengths.
* **Hierarchical View**: Displays nested (constructed) tags with indentation for clarity.
* **EMV Tag Dictionary**: Interprets common EMV tags (e.g., AID, PAN, Amount).
* **Error Handling**: Validates input and reports malformed or truncated data.
* **Data Masking**: Automatically masks sensitive card data (PAN) in the output.

---

## Assumptions and Notes

### 1. Data Masking (Security)

This tool is designed to be safe for use with production-level data. It automatically identifies and masks sensitive cardholder information.

* **PAN (Tag `5A` or `57`)**: The Primary Account Number is masked, showing only the first 6 and last 4 digits (e.g., `456789...1234`).
* **Track 2 (Tag `57`)**: The PAN within Track 2 data is similarly masked.

**All logs and screenshots in this README follow this masking rule.**

### 2. Parser Logic

* **Tag Dictionary**: The parser includes a dictionary of common EMV tags. Any tag *not* in this dictionary will be marked as `"Unknown Tag"`.
* **Data Interpretation**: Interpretation is limited to simple decoding (e.g., BCD to decimal for amounts, hex to ASCII for labels). It does *not* perform cryptographic operations (e.g., validating an ARQC) or complex, proprietary parsing of Issuer Application Data (Tag `9F10`).
* **Strictness**: The parser is strict. Any malformed TLV (incorrect length, truncated value) will result in an error rather than a partial parse.

---

## Build and Run Instructions

### Android App

1.  **Open Project**: Open the project in Android Studio.
2.  **Build**: Run `./gradlew build` from the terminal or use **Build > Make Project** from the IDE menu.
3.  **Run**: Select the `app` run configuration and run it on an emulator or a connected Android device.

### JVM / CLI (via Unit Tests)

The core parsing logic is contained in `EmvParser.kt` and can be executed on any JVM using the provided JUnit 5 test suite.

1.  **Navigate**: Open a terminal in the project's root directory.
2.  **Run Tests**: Execute the Gradle test task. This will run all tests (including success and failure cases) and verify the parser's logic.

    ```sh
    ./gradlew test
    ```

3.  **View Report**: The test results will be printed to the console. A full HTML report is available at `app/build/reports/tests/test/index.html`.

---

## Example Inputs and Outputs

### Example 1: Nested TLV (Successful Parse)

This input shows a standard FCI (File Control Information) response with nested tags.

**Input Hex:**

```sh
   6F188407A0000000031010A50D500B5649534120435245444954
```



 **Android App Output:**

The UI correctly indents the children (`84`, `A5`) of the parent tag (`6F`) and the grandchild (`50`) of its parent (`A5`).

![WhatsApp Image 2025-10-27 at 09 23 53_550fe8e9](https://github.com/user-attachments/assets/8c0de8a6-2f0c-4ef4-8594-893ee280cd92)


**Log / Table Output:**

Tag | L(Hex) | Value (Hex) | Interpretation
6F | 18 | 8407A0000000031010A50D500B... | File Control Information (FCI) Template 84 | 07 | A0000000031010 | Dedicated File (DF) Name (AID): A0000000031010 A5 | 0D | 500B5649534120435245444954 | File Control Information (FCI) Proprietary Template 50| 0B | 5649534120435245444954 | Application Label: VISA CREDIT


### Example 2: Data Masking (PAN)

This input includes a PAN (Tag `5A`), which the parser automatically masks in the interpretation.

**Input Hex:**
5A084567890123451234


**Android App Output:**

![WhatsApp Image 2025-10-27 at 09 35 18_037d1411](https://github.com/user-attachments/assets/51b3b2db-c7cf-4f75-8316-7381bffe7a34)


**Log / Table Output:**
Tag | L(Hex) | Value (Hex) | Interpretation
5A | 08 | 4567890123451234 | Application Primary Account Number (PAN): 456789...1234


### Example 3: Malformed Data (Error)

This input has an invalid length. The tag `9F02` (Amount) declares a length of `06` bytes but only provides `02` bytes (`0000`).

**Input Hex:**
9F02060000


**Android App Output:**

The app catches the error and displays it to the user instead of showing a table.

![WhatsApp Image 2025-10-27 at 09 28 07_b70bbcb6](https://github.com/user-attachments/assets/fa512630-bbb4-4bba-ad3c-735882a97581)



**Log / Test Output (from JUnit):**
java.lang.IllegalArgumentException: Data is truncated or malformed. at dev.chris.emvtlvparser.singleton.EmvParser.parseTlv(EmvParser.kt:64) ... Caused by: java.lang.IndexOutOfBoundsException: Value length 6 exceeds data size at index 3 at dev.chris.emvtlvparser.singleton.EmvParser.parseTlvRecursive(EmvParser.kt:119) ...
