上次课内容回顾：
	简单标签：
	MYSQL数据库：
		SQL分类：
			DDL：
			DML：
			DCL：
			DQL：
		MYSQL数据的存储结构：
			对数据库进行CRUD
				创建一个数据库：create database 数据库名称 character set 字符集 collate 字符集校对规则;
				删除一个数据库：drop database 数据库名称;
				修改一个数据库：alter database 数据库名称 character set 字符集 collate  校对规则;
				查看数据库：	show databases;   
								show create database 数据库名称; -- 查看数据库的定义
								select database();
				切换数据库：	use 数据库名称;
					*** 以上语句都属于DDL...
			对表进行CRUD
				创建一个表：create table 表名( 字段名 类型(长度) 约束,字段名 类型(长度) 约束...);
				删除一个表：drop table 表名;
				查看表：	show tables;
							desc 表名; -- 查看表结构.
				修改表：	alter table 表名  add 字段名 类型(长度) 约束;  -- 添加列
							alter table 表名  modify 字段名 类型(长度) 约束; -- 修改列类型长度约束
							alter table 表名  drop 字段名;  -- 删除列
							alter table 表名  change 旧列名  新列名 类型(长度) 约束; -- 修改列名
							rename table 旧表名 to 新表名;
					*** 以上语句属于DDL...
			对表中的记录进行CRUD
				向表中插入记录:
					insert into 表名 (列名,列名,列名) values (值1,值2,值3);
				修改表中某条记录：
					update 表名  set  字段=值1 , 字段= 值2 where 条件;
				删除表中的记录:
					delete from 表名 where 条件;
					truncate table 表名  和  delete from 表名区别是什么?
				查询表中记录：（*****）
					select * from 表名;
					1.select distinct 列名 from 表名;
					2.给列起别名: as   (as 可以省略)
						select name,(math+english+chinese) as total from exam;
					3.加条件
						select * from 表 where 条件;
						条件:
							> < >= <= = <>
							in
							like  _  %
							and
							or
							not
							is null
==========================================================================================================
今天的主要内容:
	数据表记录的查询语句：
		4.order by 排序： order by 字段名 asc/desc;  默认升序. asc 升序  desc 降序
			***** ORDER BY 子句应位于SELECT语句的结尾
		练习：
			对数学成绩排序后输出。
				select * from exam order by math;
			对总分排序按从高到低的顺序输出
				select *,(math+english+chinese) from exam order by (math+english+chinese) desc;
			对学生成绩按照数学进行降序排序，数学相同学员按照英语降序
				select * from exam order by math desc,english desc;
			对姓李的学生成绩排序输出
				select * from exam where name like '王%' order by (math+english+chinese) desc;
		5.聚集函数(分组函数) --- 进行分组统计.
			5.1count(); --- 统计个数.
				语法：select count(*)|count(列名) from 表 where 条件;
			练习：
				统计一个班级共有多少学生？
					select count(*) from exam;
				统计数学成绩大于80的学生有多少个？
					select count(*) from exam where math > 80;
				统计总分大于220的人数有多少？
					select count(*) from exam where (math+english+chinese)>220;
			5.2 sum(); --- 求和. sum(列名)
			练习：
				统计一个班级数学总成绩？
					select sum(math) from exam;
				统计一个班级语文、英语、数学各科的总成绩
					select sum(chinese),sum(english),sum(math) from exam;
				统计一个班级语文、英语、数学的成绩总和
					select sum(chinese)+sum(math)+sum(english) from exam;
					select sum(chinese+ifnull(math,0)+english) from exam;
					如果有null   null+任何结果都为null.   ifnull(math,0)
				统计一个班级语文成绩平均分
					select sum(chinese)/count(*) from exam;
			5.3 avg(); --- 求平均值
			练习：
				求一个班级数学平均分？
					select avg(math) from exam;
				求一个班级总分平均分？
					select avg(ifnull(math,0)+chinese+english) from exam;
			5.4max/min --- 求某列的最大值 和 最小值
			练习：
				求班级最高分和最低分（数值范围在统计中特别有用）
				select max(ifnull(math,0)+chinese+english) from exam;
				select min(ifnull(math,0)+chinese+english) from exam;
				
		6.分组： group by 列名.
			练习：
				对订单表中商品归类后，显示每一类商品的总价
					select product,sum(price) from orders group by product;
				对订单表中商品归类后，显示每一类商品的个数
					select product,count(*) from orders group by product;
				查询购买了几类商品，并且每类总价大于100的商品
					select product,sum(price) from orders  group by product having sum(price)>100;
					***** where后面不能加分组函数. 带组函数的条件 要写在 having 后面
		SQL语句小结：
			S-F-W-G-H-O
			select ... from 表 where 条件  group by ... having ... order by ...
==================================================================================================================			
	多表操作:
		create table emp(
			id int primary key auto_increment,
			name varchar(20),
			salary double,
			dept_id int
		);
		
		insert into emp values (null,'小张',3000,1);
		insert into emp values (null,'小王',4000,2);
		insert into emp values (null,'小李',5000,3);
		
		create table dept(
			did int primary key auto_increment,
			name varchar(20)
		);
		insert into dept values (null,'研发部');
		insert into dept values (null,'市场部');
		insert into dept values (null,'人力部');
		
		假设 公司 删除了 市场部 小王就没有了自己的部门 那么这种情况应该不可以.所以我们可以使用外键对其进行约束.
		***** 多表之间的约束  外键约束 ：
			在员工表的dept_id字段上加外键  指向 dept表的主键 did;
			alter table emp add foreign key (dept_id) references dept(did);
			***** 如果有外键约束了  那么刚才情况  不可以删除部门了.  必须先删除掉部门中员工
========================================================================================================================
	数据库表结构分析：（*****）
	关系型数据库：存的实体的关系：
		三种关系：
			一对一:
				主键对应：人员id 对应 用户 id
				唯一外键对应：
			
			一对多:
				一个部门 可以 存在 多个员工 反过来 一个员工 只能属于 一个部门
				一个用户 产生 多个订单  反过来 一个订单  只能属于 一个用户
				
			多对多:
				一个学生 可以 选择 多门课程 反过来 一个课程 可以被多个 学生选择 
				一个订单 可以 包含 多个商品 反过来 一个商品 可以存在在 多个订单中
		
			一个人建立多个用户    一个人只能建一个用户
			
	多表之间数据联合查询：
		查询员工表 和 部门表的信息.
		select * from emp,dept;  --- 查询出来结果是 两个表记录的乘积.
		
		连接查询：
			内连接：
				select * from emp inner join dept on emp.dept_id = dept.did;
				select * from emp join dept on emp.dept_id = dept.did;
				
				select * from emp,dept where emp.dept_id = dept.did; --- 结果集 与 内连接的效果一致.
				select * from emp e,dept d where e.dept_id = d.did;
			外连接：

=====================================================================================================================
	JDBC:
		JDBC:  Java DataBase Connectivity (Java数据库的连接.)
		
		Mysql:　数据库服务器。
			驱动：程序 和 软件通信的格式.一台计算机  识别打印机  安装打印机驱动.
			mysql驱动：不需要安装  因为是jar包.
			
		SUN公司为什么JDBC技术?
			Java程序员 连接 Mysql 需要了解Mysql驱动的具体实现.
			Java程序员 连接 Oracle 需要了解Oracle驱动的具实现.
			***** 这样 就加大了 程序员的自身压力.
			SUN公司 与 数据库厂商协商  由SUN公司推出一套标准（接口）有数据库厂商来提供实现.
			
		对于数据库操作的接口：
			java.sql
			javax.sql
		DriverManager：驱动管理
		Connection：连接对象
		Statement: 执行sql语句
			PrepareStatement: 预处理（预编译）
			CallableStatement: 调用存储过程.
		ResultSet：返回执行sql的结果集（select语句才有.）
		
		//开发JDBC步骤：
			1.加载驱动.(注册驱动)
			2.获得连接.
			3.执行SQL语句
			4.释放资源.
			
		DriverManager：主要有两个作用：
			一：注册驱动
				registerDriver(); 完成注册驱动.
				***** 开发中一般不会使用registerDriver方法加载驱动：原因有二
					一、这个方法会导致驱动注册两次.
						在com.mysql.jdbc.Driver类中
							static {
								try {
									java.sql.DriverManager.registerDriver(new Driver());
								} catch (SQLException E) {
									throw new RuntimeException("Can't register driver!");
								}
							}
							静态代码块什么时候执行? 类加载的时候 静态代码块就执行.
							手动调用registerDriver方法时候 又注册一次.
							会导致 驱动注册两次
					二、让程序依赖的具体驱动程序.
					
				解决这个问题的办法：就是让这个类加载就可以.
				反射.    Class.forName("com.mysql.jdbc.Driver");
			二：获得连接
				Connection conn = DriverManager.getConnection(String url,String username,String password);
					URL:  jdbc:mysql://localhost:3306/数据库名称
					jdbc: 采用协议
					mysql:  子协议
					localhost： 主机
					3306:  MYSQL数据库端口
					***** 简写：  jdbc:mysql///数据库名称
		Connection:
			一：创建执行SQL的对象
				createStatement() ;  返回 Statement对象 执行sql
				prepareStatement(String sql) ;  返回 PreparedStatement对象  可以对sql进行预编译 --- 防止SQL注入.
				prepareCall(String sql) ; 返回CallableStatement 对象  用来调用数据库中存储过程.
				
			二：处理事务 
				void setAutoCommit(boolean autoCommit) 
				void rollback() 
				void commit() 
				
		Statement:执行SQL的对象
			一：执行SQL。
				ResultSet executeQuery(String sql)  --- 执行 select 语句的方法
				int executeUpdate(String sql) 	--- 执行 insert 、update、delete   返回值 int 代表 sql影响的行数.
				boolean execute(String sql) --- 执行 select 、insert、update 、delete  返回值 boolean  如果select 返回true 其他就是false
			二：执行批处理  
				void addBatch(String sql) 
				int[] executeBatch() 
				void clearBatch() 
		
		ResultSet:结果集 查询语句返回的结果.
			next();  --- 结果集向下  同时返回boolean
			getXXX(); --- 获得结果集中某个值.
			
			滚动结果集：（了解）
			    Statement createStatement(int resultSetType, int resultSetConcurrency) 
				resultSetType:结果集类型.
					TYPE_FORWARD_ONLY : 只能向下滚动.
					TYPE_SCROLL_INSENSITIVE : 结果集可以向回滚动
					TYPE_SCROLL_SENSITIVE : 结果集可以向回滚动  在结果集上进行数据修改.

				resultSetConcurrency：结果集的并发策略.
					CONCUR_READ_ONLY : 只读
					CONCUR_UPDATABLE : 可以修改.
					
				三种组合：
				TYPE_FORWARD_ONLY  CONCUR_READ_ONLY  ： 结果集只能向下  而且不可以修改.(默认的)
				
				TYPE_SCROLL_INSENSITIVE  CONCUR_READ_ONLY： 结果集可以向回滚动  但是不支持修改
				
				TYPE_SCROLL_SENSITIVE CONCUR_UPDATABLE: 结果集可以向回滚动  可以支持修改.
				next()  previous()  absolute() 移到指定行 beforeFirst()移到resultSet最前面 
				afterLast()移到resultSet最后面  updateRow()更新行数据
======================================================================================================
	使用JDBC 完成对表中记录增删改查.
		发现增删改查  里面大部分代码都一致.封装成工具类.
			关于资源的释放：
				* 资源尽量做到 晚创建 早释放.
				标准释放资源的代码如下：
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException sqlEx) { // ignore }

					rs = null;
				}

				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException sqlEx) { // ignore }

					stmt = null;
				}
		
	DAO模式：持久层解决方案.
		业务层：Service			---- 转账  
		数据访问层： DAO		---- 针对数据源的单个操作.
				
 
	使用DAO模式 ：做一个登录案例.
		SQL注入漏洞.
		
		select * from user where username='' and password='';
		用户名：aa' or '1'='1  密码 随意
			select * from user where username='aa' or '1'='1' and password='sdfsdfdsf';
		用户名：aa' -- 密码 随意
			select * from user where username='aa' -- ' and password='';
			
	PreparedStatement  防止SQL注入.
 ===========================================================================================================
	总结：
		sql语句：
		数据库多表关系.
		多表联合查询.
		
		JDBC:一套规范：
			//开发JDBC步骤
			一、加载驱动
			二、获得连接
			三、执行sql
			四、释放资源
			
			DriverManager
				1.注册驱动
					Class.forName("");
				2.获得连接
					getConnection(String url,String username,String password);
			Connection:
				1.创建执行sql的对象.
					conn.createStatement();
					conn.prepareStatement();
				2.管理事务
			Statement:
				1.执行sql
					executeUpdate();
					executeQuery();
					execute();
				2.执行批处理
					addBatch();
					executeBatch();
					clearBatch();
			ResultSet:
				代表的是select语句查询结果
				滚动结果集：（了解）
			
			使用JDBC完成对一个表记录的CRUD
				* 重复代码进行封装：
					加载驱动、获得连接、释放资源
			DAO模式：
				JavaEE经典三层结构.
			用DAO模式CRUD的操作
			用DAO模式完成登录案例:(代码写出来)
				SQL 注入的漏洞.
				select * from user  where username = ? and password = ?
================================================================================================================				
			
 
 

				
		
 
 
 
 


			
				






















