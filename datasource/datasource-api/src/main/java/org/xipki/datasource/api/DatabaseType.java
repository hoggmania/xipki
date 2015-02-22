/*
 *
 * This file is part of the XiPKI project.
 * Copyright (c) 2014 - 2015 Lijun Liao
 * Author: Lijun Liao
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

package org.xipki.datasource.api;

/**
 * @author Lijun Liao
 */

public enum DatabaseType
{
    H2,
    DB2,
    HSQL,
    MYSQL,
    ORACLE,
    POSTGRES,
    UNKNOWN;

    public static DatabaseType getDataSourceForDriver(String driverClass)
    {
        return getDatabaseType(driverClass);
    }

    public static DatabaseType getDataSourceForDataSource(String dataSourceClass)
    {
        return getDatabaseType(dataSourceClass);
    }

    private static DatabaseType getDatabaseType(String className)
    {
        if(className.contains("db2."))
        {
            return DatabaseType.DB2;
        }
        if(className.contains("h2."))
        {
            return DatabaseType.H2;
        }
        else if(className.contains("hsqldb."))
        {
            return DatabaseType.HSQL;
        }
        else if(className.contains("mysql."))
        {
            return DatabaseType.MYSQL;
        }
        else if(className.contains("oracle."))
        {
            return DatabaseType.ORACLE;
        }
        else if(className.contains("postgres.") || className.contains("postgresql."))
        {
            return DatabaseType.POSTGRES;
        }
        else
        {
            return DatabaseType.UNKNOWN;
        }
    }

}
