/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.db.model

/**
 * Compact representation of an instance of any class.  The fields are stored in nibbles with an optional nibble immediate parameter
 * and an optional parameter2 of a size determined by the field type, which are lined up at the end of the field definitions.
 * If the field list ends with an odd nibble, the byte is complemented with 15 ("none")
 *
 * Field Types (first nibble):
 * 0    NULL
 * 1    False
 * 2    True
 * 3    Char (immediate: 0: '\0'; 1: '⍽'; 2: '\n'; 3: '\r'; 4: '\f'; 5: '\t'; 6: '\a'; ...; 15: 32-bit parameter2)
 * 4    Byte (immediate: 0−7: 0, 1, 2, 3, 4, 5, 8, 10; 8−14: −1, −2, 16, 32, 64, 127, -128; 15: 1-byte signed parameter2)
 * 5    UByte (immediate: 0−9: 0, 1, 2, 3, 4, 5, 8, 10, 12, 100; 10−14: 16, 32, 64, 128, 255; 15: 1-byte unsigned parameter2)
 * 6    Short (immediate: <like Byte>>; 15: 2-byte signed parameter2)
 * 7    UShort (immediate: <like UByte>; 15: 2-byte unsigned parameter2)
 * 8    Int (immediate: <like Byte>; 15: 4-byte signed parameter2)
 * 9    UInt (immediate: <like UByte>; 15: 4-byte unsigned parameter2)
 * 10   Long (immediate: <like Byte>; 15: 8-byte signed parameter2)
 * 11   ULong (immediate: <like UByte>; 15: 8-byte unsigned parameter2)
 * 12   Object (parameter2: another compact record describing the object)
 * 13   String
 * 14   Array (immediate: #dimensions; 15: parameter2 starts with 64-bit #dimensions) (parameter2: Object as compact record)
 * 15   None/Invalid
 */
class CompactRecord(val value: Any?) 
