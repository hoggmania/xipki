/*
 * Copyright (c) 2014 xipki.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *
 */

package org.xipki.ca.server.mgmt.shell;

import java.util.Set;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.xipki.ca.server.mgmt.CmpRequestorEntry;

@Command(scope = "ca", name = "requestor-list", description="List requestors")
public class RequestorListCommand extends CaCommand
{

    @Option(name = "-name",
            description = "Requestor name",
            required = false, multiValued = false)
    protected String name;

    @Option(name = "-v", aliases="--verbose",
            required = false, description = "Show CA information verbosely")
    protected Boolean          verbose;

    @Override
    protected Object doExecute()
    throws Exception
    {
        StringBuilder sb = new StringBuilder();

        if(name == null)
        {
            Set<String> names = caManager.getCmpRequestorNames();
            int n = names.size();

            sb.append(n + " CMP requestors are configured:\n");
            for(String paramName : names)
            {
                sb.append("\t").append(paramName).append("\n");
            }
        }
        else
        {
            CmpRequestorEntry entry = caManager.getCmpRequestor(name);
            if(entry != null)
            {
                sb.append(entry.toString(verbose == null ? false :verbose.booleanValue()));
            }
        }

        System.out.println(sb.toString());

        return null;
    }
}
