package com.udom.securecloud.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FileNameValidator Tests")
class FileNameValidatorTest {

    private FileNameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FileNameValidator();
    }

    @Test
    @DisplayName("Valid file names should pass validation")
    void testValidFileNames() {
        assertTrue(validator.isValid("document.pdf", null));
        assertTrue(validator.isValid("image.jpg", null));
        assertTrue(validator.isValid("archive.zip", null));
        assertTrue(validator.isValid("file-name_123.txt", null));
        assertTrue(validator.isValid("my document (1).docx", null));
    }

    @Test
    @DisplayName("Path traversal attempts should fail")
    void testPathTraversalAttempts() {
        assertFalse(validator.isValid("../../../etc/passwd", null));
        assertFalse(validator.isValid("..\\..\\windows\\system32", null));
        assertFalse(validator.isValid("file/../../../etc/passwd", null));
    }

    @Test
    @DisplayName("Absolute paths should fail")
    void testAbsolutePaths() {
        assertFalse(validator.isValid("/etc/passwd", null));
        assertFalse(validator.isValid("C:\\Windows\\System32", null));
        assertFalse(validator.isValid("/home/user/file.txt", null));
    }

    @Test
    @DisplayName("Null bytes should fail")
    void testNullBytes() {
        assertFalse(validator.isValid("file\0.txt", null));
        assertFalse(validator.isValid("test\0\0.pdf", null));
    }

    @Test
    @DisplayName("Reserved names should fail")
    void testReservedNames() {
        assertFalse(validator.isValid("CON", null));
        assertFalse(validator.isValid("PRN", null));
        assertFalse(validator.isValid("AUX", null));
        assertFalse(validator.isValid("NUL", null));
        assertFalse(validator.isValid("COM1", null));
        assertFalse(validator.isValid("LPT1", null));
    }

    @Test
    @DisplayName("Empty file names should fail")
    void testEmptyFileNames() {
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid("   ", null));
    }

    @Test
    @DisplayName("File names exceeding max length should fail")
    void testMaxLengthExceeded() {
        String longName = "a".repeat(256) + ".txt";
        assertFalse(validator.isValid(longName, null));
    }

    @Test
    @DisplayName("Null file names should fail")
    void testNullFileName() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Special characters should be handled correctly")
    void testSpecialCharacters() {
        assertTrue(validator.isValid("file-name.txt", null));
        assertTrue(validator.isValid("file_name.txt", null));
        assertTrue(validator.isValid("file (1).txt", null));
        assertFalse(validator.isValid("file<name>.txt", null));
        assertFalse(validator.isValid("file|name.txt", null));
        assertFalse(validator.isValid("file*name.txt", null));
    }

    @Test
    @DisplayName("Unicode characters should be handled")
    void testUnicodeCharacters() {
        assertTrue(validator.isValid("文件.txt", null));
        assertTrue(validator.isValid("файл.pdf", null));
        assertTrue(validator.isValid("αρχείο.doc", null));
    }
}
