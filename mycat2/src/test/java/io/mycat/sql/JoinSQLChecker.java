package io.mycat.sql;

import io.mycat.dao.TestUtil;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class JoinSQLChecker  extends BaseChecker {
    public JoinSQLChecker(Statement statement) {
        super(statement);
    }

    @Override
    public void run() {
        long max = 999999999;
        long min = 1;
        Ok ok;
        //分布式引擎基础测试
        //清除所有数据
        check("delete from db1.travelrecord");
        check("select * from db1.travelrecord", "");

        ok = executeUpdate("INSERT INTO `db1`.`travelrecord` (id,`user_id`) VALUES (" +max+ ",999)");
        ok = executeUpdate("INSERT INTO `db1`.`travelrecord` (id,`user_id`) VALUES (" +min+ ",999)");

        check("delete from db1.company");
        check("select * from db1.company", "");

        ok = executeUpdate("INSERT INTO `db1`.`company` (id,`companyname`,`addressid`) VALUES (1,'Intel',1)");
        ok = executeUpdate("INSERT INTO `db1`.`company` (id,`companyname`,`addressid`) VALUES (2,'IBM',2)");
        ok = executeUpdate("INSERT INTO `db1`.`company` (id,`companyname`,`addressid`) VALUES (3,'Dell',3)");

//        check("select id from db1.travelrecord union select id from db1.company", "(1,1)(999999999,999999999)");暂时不支持
        check("select * from db1.travelrecord as t,db1.company as c  where t.id = c.id", "(1,999,null,null,null,null,1,Intel,1)");
        check("select * from db1.travelrecord as t INNER JOIN db1.company as c  on  t.id = c.id", "(1,999,null,null,null,null,1,Intel,1)");
        check("select * from db1.travelrecord as t LEFT  JOIN db1.company as c  on  t.id = c.id", "(999999999,999,null,null,null,null,null,null,null)(1,999,null,null,null,null,1,Intel,1)");
        check("select * from db1.travelrecord as t RIGHT   JOIN db1.company as c  on  t.id = c.id", "(1,999,null,null,null,null,1,Intel,1)(null,null,null,null,null,null,2,IBM,2)(null,null,null,null,null,null,3,Dell,3)");
        check("select * from db1.travelrecord as t FULL    JOIN db1.company as c  on  t.id = c.id", "(999999999,999,null,null,null,null,null,null,null)(1,999,null,null,null,null,1,Intel,1)(null,null,null,null,null,null,2,IBM,2)(null,null,null,null,null,null,3,Dell,3)");

        //三表
        check("select * from (db1.travelrecord as t LEFT  JOIN db1.company as c  on  t.id = c.id)  LEFT  JOIN db1.company as c2 on t.id = c2.id",
                "(999999999,999,null,null,null,null,null,null,null,null,null,null)(1,999,null,null,null,null,1,Intel,1,1,Intel,1)");
    }

    @SneakyThrows
    public static void main(String[] args) {
        List<String> initList = Arrays.asList("set xa = off");
        try (Connection mySQLConnection = TestUtil.getMySQLConnection()) {
            Statement statement = mySQLConnection.createStatement();
            for (String u : initList) {
                statement.execute(u);
            }
            BaseChecker checker = new JoinSQLChecker(statement);
            checker.run();
        }
    }
}