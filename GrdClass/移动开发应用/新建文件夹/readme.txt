�ϴο����ݻعˣ�
	�򵥱�ǩ��
	MYSQL���ݿ⣺
		SQL���ࣺ
			DDL��
			DML��
			DCL��
			DQL��
		MYSQL���ݵĴ洢�ṹ��
			�����ݿ����CRUD
				����һ�����ݿ⣺create database ���ݿ����� character set �ַ��� collate �ַ���У�Թ���;
				ɾ��һ�����ݿ⣺drop database ���ݿ�����;
				�޸�һ�����ݿ⣺alter database ���ݿ����� character set �ַ��� collate  У�Թ���;
				�鿴���ݿ⣺	show databases;   
								show create database ���ݿ�����; -- �鿴���ݿ�Ķ���
								select database();
				�л����ݿ⣺	use ���ݿ�����;
					*** ������䶼����DDL...
			�Ա����CRUD
				����һ����create table ����( �ֶ��� ����(����) Լ��,�ֶ��� ����(����) Լ��...);
				ɾ��һ����drop table ����;
				�鿴��	show tables;
							desc ����; -- �鿴��ṹ.
				�޸ı�	alter table ����  add �ֶ��� ����(����) Լ��;  -- �����
							alter table ����  modify �ֶ��� ����(����) Լ��; -- �޸������ͳ���Լ��
							alter table ����  drop �ֶ���;  -- ɾ����
							alter table ����  change ������  ������ ����(����) Լ��; -- �޸�����
							rename table �ɱ��� to �±���;
					*** �����������DDL...
			�Ա��еļ�¼����CRUD
				����в����¼:
					insert into ���� (����,����,����) values (ֵ1,ֵ2,ֵ3);
				�޸ı���ĳ����¼��
					update ����  set  �ֶ�=ֵ1 , �ֶ�= ֵ2 where ����;
				ɾ�����еļ�¼:
					delete from ���� where ����;
					truncate table ����  ��  delete from ����������ʲô?
				��ѯ���м�¼����*****��
					select * from ����;
					1.select distinct ���� from ����;
					2.���������: as   (as ����ʡ��)
						select name,(math+english+chinese) as total from exam;
					3.������
						select * from �� where ����;
						����:
							> < >= <= = <>
							in
							like  _  %
							and
							or
							not
							is null
==========================================================================================================
�������Ҫ����:
	���ݱ��¼�Ĳ�ѯ��䣺
		4.order by ���� order by �ֶ��� asc/desc;  Ĭ������. asc ����  desc ����
			***** ORDER BY �Ӿ�Ӧλ��SELECT���Ľ�β
		��ϰ��
			����ѧ�ɼ�����������
				select * from exam order by math;
			���ܷ����򰴴Ӹߵ��͵�˳�����
				select *,(math+english+chinese) from exam order by (math+english+chinese) desc;
			��ѧ���ɼ�������ѧ���н���������ѧ��ͬѧԱ����Ӣ�ｵ��
				select * from exam order by math desc,english desc;
			�������ѧ���ɼ��������
				select * from exam where name like '��%' order by (math+english+chinese) desc;
		5.�ۼ�����(���麯��) --- ���з���ͳ��.
			5.1count(); --- ͳ�Ƹ���.
				�﷨��select count(*)|count(����) from �� where ����;
			��ϰ��
				ͳ��һ���༶���ж���ѧ����
					select count(*) from exam;
				ͳ����ѧ�ɼ�����80��ѧ���ж��ٸ���
					select count(*) from exam where math > 80;
				ͳ���ִܷ���220�������ж��٣�
					select count(*) from exam where (math+english+chinese)>220;
			5.2 sum(); --- ���. sum(����)
			��ϰ��
				ͳ��һ���༶��ѧ�ܳɼ���
					select sum(math) from exam;
				ͳ��һ���༶���ġ�Ӣ���ѧ���Ƶ��ܳɼ�
					select sum(chinese),sum(english),sum(math) from exam;
				ͳ��һ���༶���ġ�Ӣ���ѧ�ĳɼ��ܺ�
					select sum(chinese)+sum(math)+sum(english) from exam;
					select sum(chinese+ifnull(math,0)+english) from exam;
					�����null   null+�κν����Ϊnull.   ifnull(math,0)
				ͳ��һ���༶���ĳɼ�ƽ����
					select sum(chinese)/count(*) from exam;
			5.3 avg(); --- ��ƽ��ֵ
			��ϰ��
				��һ���༶��ѧƽ���֣�
					select avg(math) from exam;
				��һ���༶�ܷ�ƽ���֣�
					select avg(ifnull(math,0)+chinese+english) from exam;
			5.4max/min --- ��ĳ�е����ֵ �� ��Сֵ
			��ϰ��
				��༶��߷ֺ���ͷ֣���ֵ��Χ��ͳ�����ر����ã�
				select max(ifnull(math,0)+chinese+english) from exam;
				select min(ifnull(math,0)+chinese+english) from exam;
				
		6.���飺 group by ����.
			��ϰ��
				�Զ���������Ʒ�������ʾÿһ����Ʒ���ܼ�
					select product,sum(price) from orders group by product;
				�Զ���������Ʒ�������ʾÿһ����Ʒ�ĸ���
					select product,count(*) from orders group by product;
				��ѯ�����˼�����Ʒ������ÿ���ܼ۴���100����Ʒ
					select product,sum(price) from orders  group by product having sum(price)>100;
					***** where���治�ܼӷ��麯��. ���麯�������� Ҫд�� having ����
		SQL���С�᣺
			S-F-W-G-H-O
			select ... from �� where ����  group by ... having ... order by ...
==================================================================================================================			
	������:
		create table emp(
			id int primary key auto_increment,
			name varchar(20),
			salary double,
			dept_id int
		);
		
		insert into emp values (null,'С��',3000,1);
		insert into emp values (null,'С��',4000,2);
		insert into emp values (null,'С��',5000,3);
		
		create table dept(
			did int primary key auto_increment,
			name varchar(20)
		);
		insert into dept values (null,'�з���');
		insert into dept values (null,'�г���');
		insert into dept values (null,'������');
		
		���� ��˾ ɾ���� �г��� С����û�����Լ��Ĳ��� ��ô�������Ӧ�ò�����.�������ǿ���ʹ������������Լ��.
		***** ���֮���Լ��  ���Լ�� ��
			��Ա�����dept_id�ֶ��ϼ����  ָ�� dept������� did;
			alter table emp add foreign key (dept_id) references dept(did);
			***** ��������Լ����  ��ô�ղ����  ������ɾ��������.  ������ɾ����������Ա��
========================================================================================================================
	���ݿ��ṹ��������*****��
	��ϵ�����ݿ⣺���ʵ��Ĺ�ϵ��
		���ֹ�ϵ��
			һ��һ:
				������Ӧ����Աid ��Ӧ �û� id
				Ψһ�����Ӧ��
			
			һ�Զ�:
				һ������ ���� ���� ���Ա�� ������ һ��Ա�� ֻ������ һ������
				һ���û� ���� �������  ������ һ������  ֻ������ һ���û�
				
			��Զ�:
				һ��ѧ�� ���� ѡ�� ���ſγ� ������ һ���γ� ���Ա���� ѧ��ѡ�� 
				һ������ ���� ���� �����Ʒ ������ һ����Ʒ ���Դ����� ���������
		
			һ���˽�������û�    һ����ֻ�ܽ�һ���û�
			
	���֮���������ϲ�ѯ��
		��ѯԱ���� �� ���ű����Ϣ.
		select * from emp,dept;  --- ��ѯ��������� �������¼�ĳ˻�.
		
		���Ӳ�ѯ��
			�����ӣ�
				select * from emp inner join dept on emp.dept_id = dept.did;
				select * from emp join dept on emp.dept_id = dept.did;
				
				select * from emp,dept where emp.dept_id = dept.did; --- ����� �� �����ӵ�Ч��һ��.
				select * from emp e,dept d where e.dept_id = d.did;
			�����ӣ�

=====================================================================================================================
	JDBC:
		JDBC:  Java DataBase Connectivity (Java���ݿ������.)
		
		Mysql:�����ݿ��������
			���������� �� ���ͨ�ŵĸ�ʽ.һ̨�����  ʶ���ӡ��  ��װ��ӡ������.
			mysql����������Ҫ��װ  ��Ϊ��jar��.
			
		SUN��˾ΪʲôJDBC����?
			Java����Ա ���� Mysql ��Ҫ�˽�Mysql�����ľ���ʵ��.
			Java����Ա ���� Oracle ��Ҫ�˽�Oracle�����ľ�ʵ��.
			***** ���� �ͼӴ��� ����Ա������ѹ��.
			SUN��˾ �� ���ݿ⳧��Э��  ��SUN��˾�Ƴ�һ�ױ�׼���ӿڣ������ݿ⳧�����ṩʵ��.
			
		�������ݿ�����Ľӿڣ�
			java.sql
			javax.sql
		DriverManager����������
		Connection�����Ӷ���
		Statement: ִ��sql���
			PrepareStatement: Ԥ����Ԥ���룩
			CallableStatement: ���ô洢����.
		ResultSet������ִ��sql�Ľ������select������.��
		
		//����JDBC���裺
			1.��������.(ע������)
			2.�������.
			3.ִ��SQL���
			4.�ͷ���Դ.
			
		DriverManager����Ҫ���������ã�
			һ��ע������
				registerDriver(); ���ע������.
				***** ������һ�㲻��ʹ��registerDriver��������������ԭ���ж�
					һ����������ᵼ������ע������.
						��com.mysql.jdbc.Driver����
							static {
								try {
									java.sql.DriverManager.registerDriver(new Driver());
								} catch (SQLException E) {
									throw new RuntimeException("Can't register driver!");
								}
							}
							��̬�����ʲôʱ��ִ��? ����ص�ʱ�� ��̬������ִ��.
							�ֶ�����registerDriver����ʱ�� ��ע��һ��.
							�ᵼ�� ����ע������
					�����ó��������ľ�����������.
					
				����������İ취���������������ؾͿ���.
				����.    Class.forName("com.mysql.jdbc.Driver");
			�����������
				Connection conn = DriverManager.getConnection(String url,String username,String password);
					URL:  jdbc:mysql://localhost:3306/���ݿ�����
					jdbc: ����Э��
					mysql:  ��Э��
					localhost�� ����
					3306:  MYSQL���ݿ�˿�
					***** ��д��  jdbc:mysql///���ݿ�����
		Connection:
			һ������ִ��SQL�Ķ���
				createStatement() ;  ���� Statement���� ִ��sql
				prepareStatement(String sql) ;  ���� PreparedStatement����  ���Զ�sql����Ԥ���� --- ��ֹSQLע��.
				prepareCall(String sql) ; ����CallableStatement ����  �����������ݿ��д洢����.
				
			������������ 
				void setAutoCommit(boolean autoCommit) 
				void rollback() 
				void commit() 
				
		Statement:ִ��SQL�Ķ���
			һ��ִ��SQL��
				ResultSet executeQuery(String sql)  --- ִ�� select ���ķ���
				int executeUpdate(String sql) 	--- ִ�� insert ��update��delete   ����ֵ int ���� sqlӰ�������.
				boolean execute(String sql) --- ִ�� select ��insert��update ��delete  ����ֵ boolean  ���select ����true ��������false
			����ִ��������  
				void addBatch(String sql) 
				int[] executeBatch() 
				void clearBatch() 
		
		ResultSet:����� ��ѯ��䷵�صĽ��.
			next();  --- ���������  ͬʱ����boolean
			getXXX(); --- ��ý������ĳ��ֵ.
			
			��������������˽⣩
			    Statement createStatement(int resultSetType, int resultSetConcurrency) 
				resultSetType:���������.
					TYPE_FORWARD_ONLY : ֻ�����¹���.
					TYPE_SCROLL_INSENSITIVE : �����������ع���
					TYPE_SCROLL_SENSITIVE : �����������ع���  �ڽ�����Ͻ��������޸�.

				resultSetConcurrency��������Ĳ�������.
					CONCUR_READ_ONLY : ֻ��
					CONCUR_UPDATABLE : �����޸�.
					
				������ϣ�
				TYPE_FORWARD_ONLY  CONCUR_READ_ONLY  �� �����ֻ������  ���Ҳ������޸�.(Ĭ�ϵ�)
				
				TYPE_SCROLL_INSENSITIVE  CONCUR_READ_ONLY�� �����������ع���  ���ǲ�֧���޸�
				
				TYPE_SCROLL_SENSITIVE CONCUR_UPDATABLE: �����������ع���  ����֧���޸�.
				next()  previous()  absolute() �Ƶ�ָ���� beforeFirst()�Ƶ�resultSet��ǰ�� 
				afterLast()�Ƶ�resultSet�����  updateRow()����������
======================================================================================================
	ʹ��JDBC ��ɶԱ��м�¼��ɾ�Ĳ�.
		������ɾ�Ĳ�  ����󲿷ִ��붼һ��.��װ�ɹ�����.
			������Դ���ͷţ�
				* ��Դ�������� ���� ���ͷ�.
				��׼�ͷ���Դ�Ĵ������£�
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
		
	DAOģʽ���־ò�������.
		ҵ��㣺Service			---- ת��  
		���ݷ��ʲ㣺 DAO		---- �������Դ�ĵ�������.
				
 
	ʹ��DAOģʽ ����һ����¼����.
		SQLע��©��.
		
		select * from user where username='' and password='';
		�û�����aa' or '1'='1  ���� ����
			select * from user where username='aa' or '1'='1' and password='sdfsdfdsf';
		�û�����aa' -- ���� ����
			select * from user where username='aa' -- ' and password='';
			
	PreparedStatement  ��ֹSQLע��.
 ===========================================================================================================
	�ܽ᣺
		sql��䣺
		���ݿ����ϵ.
		������ϲ�ѯ.
		
		JDBC:һ�׹淶��
			//����JDBC����
			һ����������
			�����������
			����ִ��sql
			�ġ��ͷ���Դ
			
			DriverManager
				1.ע������
					Class.forName("");
				2.�������
					getConnection(String url,String username,String password);
			Connection:
				1.����ִ��sql�Ķ���.
					conn.createStatement();
					conn.prepareStatement();
				2.��������
			Statement:
				1.ִ��sql
					executeUpdate();
					executeQuery();
					execute();
				2.ִ��������
					addBatch();
					executeBatch();
					clearBatch();
			ResultSet:
				�������select����ѯ���
				��������������˽⣩
			
			ʹ��JDBC��ɶ�һ�����¼��CRUD
				* �ظ�������з�װ��
					����������������ӡ��ͷ���Դ
			DAOģʽ��
				JavaEE��������ṹ.
			��DAOģʽCRUD�Ĳ���
			��DAOģʽ��ɵ�¼����:(����д����)
				SQL ע���©��.
				select * from user  where username = ? and password = ?
================================================================================================================				
			
 
 

				
		
 
 
 
 


			
				






















