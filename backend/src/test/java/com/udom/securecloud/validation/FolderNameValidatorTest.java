package com.udom.securecloud.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FolderNameValidator Tests")
class FolderNameValidatorTest {

    private FolderNameValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FolderNameValidator();
    }

    @Test
    @DisplayName("Valid folder names should pass validation")
    void testValidFolderNames() {
        assertTrue(validator.isValid("Documents", null));
        assertTrue(validator.isValid("My Folder", null));
        assertTrue(validator.isValid("folder-name", null));
        assertTrue(validator.isValid("folder_name", null));
        assertTrue(validator.isValid("Folder 123", null));
    }

    @Test
    @DisplayName("Path traversal attempts should fail")
    void testPathTraversalAttempts() {
        assertFalse(validator.isValid("../../../etc", null));
        assertFalse(validator.isValid("..\\..\\windows", null));
        assertFalse(validator.isValid("folder/../../../etc", null));
    }

    @Test
    @DisplayName("Absolute paths should fail")
    void testAbsolutePaths() {
        assertFalse(validator.isValid("/etc", null));
        assertFalse(validator.isValid("C:\\Windows", null));
        assertFalse(validator.isValid("/home/user", null));
    }

    @Test
    @DisplayName("Null bytes should fail")
    void testNullBytes() {
        assertFalse(validator.isValid("folder\0", null));
        assertFalse(validator.isValid("test\0\0", null));
    }

    @Test
    @DisplayName("Reserved names should fail")
    void testReservedNames() {
        assertFalse(validator.isValid("CON", null));
        assertFalse(validator.isValid("PRN", null));
        assertFalse(validator.isValid("AUX", null));
        assertFalse(validator.isValid("NUL", null));
    }

    @Test
    @DisplayName("Empty folder names should fail")
    void testEmptyFolderNames() {
        assertFalse(validator.isValid("", null));
        assertFalse(validator.isValid("   ", null));
    }

    @Test
    @DisplayName("Folder names exceeding max length should fail")
    void testMaxLengthExceeded() {
        String longName = "a".repeat(256);
        assertFalse(validator.isValid(longName, null));
    }

    @Test
    @DisplayName("Null folder names should fail")
    void testNullFolderName() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    @DisplayName("Dot folders should fail")
    void testDotFolders() {
        assertFalse(validator.isValid(".", null));
        assertFalse(validator.isValid("..", null));
        assertFalse(validator.isValid("...", null));
    }

    @Test
    @DisplayName("Special characters should be handled correctly")
    void testSpecialCharacters() {
        assertTrue(validator.isValid("folder-name", null));
        assertTrue(validator.isValid("folder_name", null));
        assertFalse(validator.isValid("folder<name>", null));
        assertFalse(validator.isValid("folder|name", null));
        assertFalse(validator.isValid("folder*name", null));
    }
}
