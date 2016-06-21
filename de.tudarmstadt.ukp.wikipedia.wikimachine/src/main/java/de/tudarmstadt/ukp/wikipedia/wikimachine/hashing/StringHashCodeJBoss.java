/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tudarmstadt.ukp.wikipedia.wikimachine.hashing;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringHashCodeJBoss
    implements IStringHashCode
{

    public StringHashCodeJBoss()
    {
        // use for instantiate as generic
    }

    @Override
    public Long hashCode(String string)
    {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataOut = new DataOutputStream(
                    new DigestOutputStream(new ByteArrayOutputStream(0x200),
                            messageDigest));
            dataOut.writeUTF(string);
            dataOut.flush();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte digest[] = messageDigest.digest();
        long hash = 0;
        int i = digest.length > 8 ? 8 : digest.length;
        while (i-- > 0) {
            hash += (long) (digest[i] & 0xff) << 8 * i;
        }
        return hash;
    }

}
