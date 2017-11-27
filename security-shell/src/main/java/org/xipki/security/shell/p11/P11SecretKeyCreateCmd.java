/*
 *
 * Copyright (c) 2013 - 2017 Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 *
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * THE AUTHOR LIJUN LIAO. LIJUN LIAO DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
 * OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the XiPKI software without
 * disclosing the source code of your own applications.
 *
 * For more information, please contact Lijun Liao at this
 * address: lijun.liao@gmail.com
 */

package org.xipki.security.shell.p11;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Enumeration;

import javax.crypto.SecretKey;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.xipki.common.util.IoUtil;
import org.xipki.console.karaf.IllegalCmdParamException;
import org.xipki.console.karaf.completer.FilePathCompleter;
import org.xipki.security.pkcs11.P11ObjectIdentifier;
import org.xipki.security.pkcs11.P11Slot;
import org.xipki.security.shell.completer.SecretKeyTypeCompleter;

import iaik.pkcs.pkcs11.wrapper.PKCS11Constants;

/**
 * @author Lijun Liao
 * @since 2.2.0
 */

@Command(scope = "xipki-tk", name = "create-secretkey",
        description = "create secret key with given value in PKCS#11 device")
@Service
// CHECKSTYLE:SKIP
public class P11SecretKeyCreateCmd extends P11KeyGenCommandSupport {

    @Option(name = "--key-type",
            required = true,
            description = "keytype, current only AES, DES3 and GENERIC are supported\n"
                    + "(required)")
    @Completion(SecretKeyTypeCompleter.class)
    private String keyType;

    @Option(name = "--keystore",
            required = true,
            description = "JCEKS keystore from which the key is imported\n"
                    + "(required)")
    @Completion(FilePathCompleter.class)
    private String keyOutFile;

    @Option(name = "--password",
            description = "password of the keystore file")
    private String password;

    @Override
    protected Object execute0() throws Exception {
        long p11KeyType;
        if ("AES".equalsIgnoreCase(keyType)) {
            p11KeyType = PKCS11Constants.CKK_AES;

        } else if ("DES3".equalsIgnoreCase(keyType)) {
            p11KeyType = PKCS11Constants.CKK_DES3;
        } else if ("GENERIC".equalsIgnoreCase(keyType)) {
            p11KeyType = PKCS11Constants.CKK_GENERIC_SECRET;
        } else {
            throw new IllegalCmdParamException("invalid keyType " + keyType);
        }

        KeyStore ks = KeyStore.getInstance("JCEKS");
        InputStream ksStream = new FileInputStream(IoUtil.expandFilepath(keyOutFile));
        char[] pwd = getPassword();
        try {
            ks.load(ksStream, pwd);
        } finally {
            ksStream.close();
        }

        byte[] keyValue = null;
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!ks.isKeyEntry(alias)) {
                continue;
            }

            Key key = ks.getKey(alias, pwd);
            if (key instanceof SecretKey) {
                keyValue = ((SecretKey) key).getEncoded();
                break;
            }
        }

        if (keyValue == null) {
            throw new IllegalCmdParamException("keystore does not contain secret key");
        }

        P11Slot slot = getSlot();
        P11ObjectIdentifier objId = slot.createSecretKey(p11KeyType, keyValue, label,
                getControl());
        finalize("Create Secret Key", objId);
        return null;
    }

    @Override
    protected boolean getDefaultExtractable() {
        return true;
    }

    protected char[] getPassword() throws IOException {
        char[] pwdInChar = readPasswordIfNotSet(password);
        if (pwdInChar != null) {
            password = new String(pwdInChar);
        }
        return pwdInChar;
    }

}