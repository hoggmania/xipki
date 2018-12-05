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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.FileCompleter;
import org.xipki.ca.mgmt.api.CaMgmtException;
import org.xipki.shell.CmdFailure;
import org.xipki.util.IoUtil;

/**
 * TODO.
 * @author Lijun Liao
 * @since 2.0.0
 */

@Command(scope = "ca", name = "export-conf", description = "export configuration to zip file")
@Service
public class ExportConfAction extends CaAction {

  @Option(name = "--conf-file", required = true,
      description = "zip file that saves the exported configuration")
  @Completion(FileCompleter.class)
  private String confFile;

  @Option(name = "--ca", multiValued = true,
      description = "CAs whose configuration should be exported. Empty list means all CAs")
  @Completion(CaCompleters.CaNameCompleter.class)
  private List<String> caNames;

  @Override
  protected Object execute0() throws Exception {
    String msg = "configuration to file " + confFile;
    try {
      InputStream is = caManager.exportConf(caNames);
      save(new File(confFile), IoUtil.read(is));
      println("exported " + msg);
      return null;
    } catch (CaMgmtException ex) {
      throw new CmdFailure("could not export " + msg + ", error: " + ex.getMessage(), ex);
    }
  }

}
