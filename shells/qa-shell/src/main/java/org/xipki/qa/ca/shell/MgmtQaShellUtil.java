/*
 *
 * Copyright (c) 2013 - 2018 Lijun Liao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xipki.qa.ca.shell;

import java.util.Arrays;
import java.util.Collection;

import org.xipki.ca.server.mgmt.api.CaManager;
import org.xipki.security.util.X509Util;
import org.xipki.shell.CmdFailure;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

public class MgmtQaShellUtil {

  private MgmtQaShellUtil() {
  }

  public static void assertTypeEquals(String desc, String ex, String is) throws CmdFailure {
    String tmpEx = ex;
    if (CaManager.NULL.equals(tmpEx)) {
      tmpEx = null;
    }

    boolean bo = (tmpEx == null) ? (is == null) : tmpEx.equalsIgnoreCase(is);
    if (!bo) {
      throw new CmdFailure(desc + ": is '" + is + "', but expected '" + tmpEx + "'");
    }
  }

  public static void assertEquals(String desc, String ex, String is) throws CmdFailure {
    String tmpEx = ex;
    if (CaManager.NULL.equals(tmpEx)) {
      tmpEx = null;
    }

    boolean bo = (tmpEx == null) ? (is == null) : tmpEx.equals(is);
    if (!bo) {
      throw new CmdFailure(desc + ": is '" + is + "', but expected '" + tmpEx + "'");
    }
  }

  public static void assertEquals(String desc, Collection<?> ex, Collection<?> is)
      throws CmdFailure {
    boolean bo = (ex == null) ? (is == null) : ex.equals(is);
    if (!bo) {
      throw new CmdFailure(desc + ": is '" + is + "', but expected '" + ex + "'");
    }
  }

  public static boolean certEquals(byte[] certBytes1, byte[] certBytes2) {
    if (certBytes1 == null && certBytes2 == null) {
      return true;
    } else if (certBytes1 != null && certBytes2 != null) {
      try {
        byte[] encoded1 = X509Util.parseBcCert(certBytes1).getEncoded();
        byte[] encoded2 = X509Util.parseBcCert(certBytes2).getEncoded();
        return Arrays.equals(encoded1, encoded2);
      } catch (Exception ex) {
        return false;
      }
    } else {
      return false;
    }
  }

}