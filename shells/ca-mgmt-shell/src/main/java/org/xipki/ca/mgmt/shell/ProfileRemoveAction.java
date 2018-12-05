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

package org.xipki.ca.mgmt.shell;

import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.xipki.ca.mgmt.api.CaMgmtException;
import org.xipki.shell.CmdFailure;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "ca", name = "profile-rm", description = "remove certificate profile")
@Service
public class ProfileRemoveAction extends CaAction {

  @Argument(index = 0, name = "name", required = true, description = "certificate profile name")
  @Completion(CaCompleters.ProfileNameCompleter.class)
  private String name;

  @Option(name = "--force", aliases = "-f", description = "without prompt")
  private Boolean force = Boolean.FALSE;

  @Override
  protected Object execute0() throws Exception {
    String msg = "certificate profile " + name;
    if (force || confirm("Do you want to remove " + msg, 3)) {
      try {
        caManager.removeCertprofile(name);
        println("removed " + msg);
      } catch (CaMgmtException ex) {
        throw new CmdFailure("could not remove " + msg + ", error: " + ex.getMessage(), ex);
      }
    }
    return null;
  }

}
