/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.jwpl.wikimachine.hashing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StringHashCodeJBossTest
{

    private final StringHashCodeJBoss hasher = new StringHashCodeJBoss();

    /**
     * The original implementation, kept as oracle.
     */
    private static Long legacyHashCode(String string)
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataOut = new DataOutputStream(
                    new DigestOutputStream(new ByteArrayOutputStream(0x200), messageDigest));
            dataOut.writeUTF(string);
            dataOut.flush();
        }
        catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }

        byte[] digest = messageDigest.digest();
        long hash = 0;
        int i = digest.length > 8 ? 8 : digest.length;
        while (i-- > 0) {
            hash += (long) (digest[i] & 0xff) << 8 * i;
        }
        return hash;
    }

    static Stream<String> titles()
    {
        return Stream.of("", "A", "Main_Page", "Category:Living_people", "Äpfel_und_Öl_ß",
                "é߿ࠀ￿", "東京都", "Emoji_😀_🌍",
                "\uD800", "\uDC00x", "\u0000", "a\u0000b\u0000", "\u007f\u0080",
                "x".repeat(600), "ä".repeat(400), "中".repeat(300),
                "a".repeat(0xFFFF), "ä".repeat(0xFFFF / 2) + "a",
                "中".repeat(0xFFFF / 3));
    }

    @ParameterizedTest
    @MethodSource("titles")
    void matchesLegacyImplementation(String title)
    {
        assertEquals(legacyHashCode(title), hasher.hashCode(title));
    }

    @Test
    void matchesLegacyImplementationOnRandomStrings()
    {
        Random random = new Random(616);
        for (int n = 0; n < 2000; n++) {
            int length = random.nextInt(n % 100 == 0 ? 3000 : 80);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                switch (random.nextInt(4)) {
                case 0:
                    sb.append((char) random.nextInt(0x80));
                    break;
                case 1:
                    sb.append((char) (0x80 + random.nextInt(0x780)));
                    break;
                case 2:
                    sb.append((char) random.nextInt(0x10000));
                    break;
                default:
                    sb.appendCodePoint(0x10000 + random.nextInt(0x100000));
                    break;
                }
            }
            String s = sb.toString();
            assertEquals(legacyHashCode(s), hasher.hashCode(s));
        }
    }

    @Test
    void rejectsStringsExceedingMaximumEncodedLength()
    {
        for (String tooLong : new String[] { "a".repeat(0x10000), "\u0000".repeat(0x8000),
                "中".repeat(0xFFFF / 3 + 1) }) {
            RuntimeException legacy = assertThrows(RuntimeException.class,
                    () -> legacyHashCode(tooLong));
            assertInstanceOf(UTFDataFormatException.class, legacy.getCause());

            RuntimeException actual = assertThrows(RuntimeException.class,
                    () -> hasher.hashCode(tooLong));
            assertInstanceOf(UTFDataFormatException.class, actual.getCause());
        }
        // The per-thread state must remain usable after a rejected input.
        assertEquals(legacyHashCode("Main_Page"), hasher.hashCode("Main_Page"));
    }
}
