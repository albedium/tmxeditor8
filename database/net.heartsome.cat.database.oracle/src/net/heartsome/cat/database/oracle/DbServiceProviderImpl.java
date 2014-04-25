package net.heartsome.cat.database.oracle;

import net.heartsome.cat.database.DBServiceProvider;
import net.heartsome.cat.database.SystemDBOperator;
import net.heartsome.cat.database.DBOperator;

public class DbServiceProviderImpl implements DBServiceProvider{

	public SystemDBOperator getOperateDBInstance() {
		return new OperateDBImpl();
	}

	public DBOperator getTmDatabaseInstance() {
		return new TMDatabaseImpl();
	}

}
