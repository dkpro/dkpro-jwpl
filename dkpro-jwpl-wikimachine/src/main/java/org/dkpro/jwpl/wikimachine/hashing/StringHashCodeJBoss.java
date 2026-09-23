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

import java.io.UTFDataFormatException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A JBoss specific {@link IStringHashCode} implementation.
 * <p>
 * The hash is derived from the SHA-1 digest of the string encoded as by
 * {@link java.io.DataOutputStream#writeUTF(String)}, i.e. a 2-byte big-endian length prefix
 * followed by the modified UTF-8 representation of the string. The digest and the encoding
 * buffer are kept per thread.
 */
public class StringHashCodeJBoss
    implements IStringHashCode
{

    private static final int MAX_ENCODED_LENGTH = 0xFFFF;

    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    /**
     * Instantiates a {@link StringHashCodeJBoss} object.
     */
    public StringHashCodeJBoss()
    {
        // use for instantiate as generic
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException wrapping a {@link UTFDataFormatException} if the modified UTF-8
     *                          encoding of {@code string} exceeds 65535 bytes.
     */
    @Override
    public Long hashCode(String string)
    {
        State state = STATE.get();
        int length = encodeModifiedUtf8(string, state);

        MessageDigest messageDigest = state.messageDigest;
        messageDigest.reset();
        messageDigest.update(state.buffer, 0, length);
        byte[] digest = messageDigest.digest();

        long hash = 0;
        int i = digest.length > 8 ? 8 : digest.length;
        while (i-- > 0) {
            hash += (long) (digest[i] & 0xff) << 8 * i;
        }
        return hash;
    }

    /**
     * Writes {@code string} into the buffer of {@code state} exactly as
     * {@link java.io.DataOutputStream#writeUTF(String)} would, growing the buffer if needed.
     *
     * @return The number of bytes written, including the 2-byte length prefix.
     */
    private static int encodeModifiedUtf8(String string, State state)
    {
        final int strlen = string.length();
        int utflen = strlen;
        for (int i = 0; i < strlen; i++) {
            char c = string.charAt(i);
            if (c >= 0x80 || c == 0) {
                utflen += (c >= 0x800) ? 2 : 1;
            }
        }
        if (utflen > MAX_ENCODED_LENGTH) {
            throw new RuntimeException(
                    new UTFDataFormatException("encoded string too long: " + utflen + " bytes"));
        }

        int total = utflen + 2;
        if (state.buffer.length < total) {
            state.buffer = new byte[Math.max(total, state.buffer.length * 2)];
        }
        byte[] bytes = state.buffer;
        int count = 0;
        bytes[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytes[count++] = (byte) (utflen & 0xFF);

        for (int i = 0; i < strlen; i++) {
            char c = string.charAt(i);
            if (c != 0 && c < 0x80) {
                bytes[count++] = (byte) c;
            }
            else if (c >= 0x800) {
                bytes[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[count++] = (byte) (0x80 | (c & 0x3F));
            }
            else {
                bytes[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[count++] = (byte) (0x80 | (c & 0x3F));
            }
        }
        return count;
    }

    /**
     * Per-thread digest and reusable encoding buffer.
     */
    private static final class State
    {
        private final MessageDigest messageDigest;

        private byte[] buffer = new byte[0x200];

        State()
        {
            try {
                messageDigest = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
