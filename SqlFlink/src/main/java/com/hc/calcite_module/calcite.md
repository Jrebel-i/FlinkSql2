calcite 方解石
将sql转换为sqlNode,也就是抽象语法树

```sql
insert into MyResult
select t.id as id,s1.name as name,s2.msg as msg
from MyTable t left join MySide s1 on t1.id=s1.id
left join MyOuter s2 on t1.id=s2.id
```
抽象语法树
![](https://jrebe-note-pic.oss-cn-shenzhen.aliyuncs.com/img/flink/KafkaCheckpoint/抽象语法树图解.png)

